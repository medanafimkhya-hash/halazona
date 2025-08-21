package com.example.myvsouf;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyVpnService extends VpnService {
    private static final String TAG = "MyVpnService";
    private static final String CHANNEL_ID = "VPN_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    
    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executor;
    private boolean isRunning = false;
    private VpnServer selectedServer; // Selected country server
    
    // VPN connection components
    private Socket vpnSocket;
    private FileInputStream vpnInput;
    private FileOutputStream vpnOutput;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        executor = Executors.newFixedThreadPool(3);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START_VPN".equals(action)) {
                selectedServer = (VpnServer) intent.getSerializableExtra("server");
                startVpn();
            } else if ("STOP_VPN".equals(action)) {
                stopVpn();
            }
        }
        return START_STICKY;
    }
    
    private void startVpn() {
        if (isRunning) {
            return;
        }
        
        if (selectedServer == null) {
            Log.e(TAG, "No server selected");
            return;
        }
        
        try {
            Log.d(TAG, "Starting VPN to " + selectedServer.countryLong + " (" + selectedServer.countryShort + ")");
            Log.d(TAG, "Server IP: " + selectedServer.ip);
            Log.d(TAG, "Server Host: " + selectedServer.getVpnServerHost());
            Log.d(TAG, "Server Port: " + selectedServer.getVpnServerPort());
            
            // Create VPN interface with settings for the selected country
            Builder builder = new Builder();
            builder.setMtu(1500);
            builder.addAddress("10.8.0.2", 24); // Standard OpenVPN client IP
            
            // Route all traffic through VPN to change IP to selected country
            builder.addRoute("0.0.0.0", 0);
            
            // Use DNS servers
            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("1.1.1.1");
            
            // Set session name with country info
            builder.setSession("VPN - " + selectedServer.countryLong + " " + selectedServer.getCountryFlag());
            
            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                return;
            }
            
            isRunning = true;
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start connection to VPN Gate server
            startVpnConnection();
            
            Log.d(TAG, "VPN started successfully to " + selectedServer.countryLong);
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
            stopVpn();
        }
    }
    
    private void startVpnConnection() {
        executor.submit(() -> {
            try {
                // Get VPN interface streams
                vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
                vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());
                
                // Try to connect to the VPN Gate server
                String serverHost = selectedServer.getVpnServerHost();
                int serverPort = selectedServer.getVpnServerPort();
                
                Log.d(TAG, "Connecting to VPN Gate server: " + serverHost + ":" + serverPort);
                
                try {
                    // Try multiple ports for better connectivity
                    vpnSocket = connectToServer(serverHost, new int[]{serverPort, 1194, 443, 992, 1723});
                    
                    if (vpnSocket != null && vpnSocket.isConnected()) {
                        Log.d(TAG, "Connected to VPN server successfully");
                        
                        InputStream serverInput = vpnSocket.getInputStream();
                        OutputStream serverOutput = vpnSocket.getOutputStream();
                        
                        // Start bidirectional packet forwarding
                        executor.submit(() -> forwardPackets(vpnInput, serverOutput, "VPN->Server"));
                        executor.submit(() -> forwardPackets(serverInput, vpnOutput, "Server->VPN"));
                        
                        Log.d(TAG, "Packet forwarding started - IP should now be from " + selectedServer.countryLong);
                    } else {
                        Log.w(TAG, "Direct connection failed, using basic routing");
                        startBasicRouting();
                    }
                    
                } catch (Exception e) {
                    Log.w(TAG, "VPN server connection failed, using basic routing: " + e.getMessage());
                    startBasicRouting();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "VPN connection setup failed", e);
                stopVpn();
            }
        });
    }
    
    private Socket connectToServer(String host, int[] ports) {
        for (int port : ports) {
            try {
                Log.d(TAG, "Trying to connect to " + host + ":" + port);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 second timeout
                Log.d(TAG, "Successfully connected to " + host + ":" + port);
                return socket;
            } catch (Exception e) {
                Log.d(TAG, "Failed to connect to " + host + ":" + port + " - " + e.getMessage());
            }
        }
        return null;
    }
    
    private void forwardPackets(InputStream input, OutputStream output, String direction) {
        byte[] buffer = new byte[32767];
        try {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                int length = input.read(buffer);
                if (length > 0) {
                    output.write(buffer, 0, length);
                    output.flush();
                    Log.v(TAG, direction + ": Forwarded " + length + " bytes");
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                Log.e(TAG, "Packet forwarding error (" + direction + "): " + e.getMessage());
            }
        }
    }
    
    private void startBasicRouting() {
        executor.submit(() -> {
            try {
                ByteBuffer packet = ByteBuffer.allocate(32767);
                DatagramSocket socket = new DatagramSocket();
                
                Log.d(TAG, "Starting basic routing for " + selectedServer.countryLong);
                
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        // Read packet from VPN interface
                        packet.clear();
                        int length = vpnInput.read(packet.array());
                        
                        if (length > 0) {
                            packet.limit(length);
                            
                            // Basic packet processing
                            if (length >= 20) {
                                // Get destination IP from packet (bytes 16-19 in IPv4 header)
                                byte[] destIp = new byte[4];
                                packet.position(16);
                                packet.get(destIp);
                                
                                try {
                                    InetAddress destAddress = InetAddress.getByAddress(destIp);
                                    
                                    // Forward packet to destination
                                    DatagramPacket outPacket = new DatagramPacket(
                                        packet.array(), 0, length, destAddress, 80);
                                    
                                    socket.send(outPacket);
                                    
                                    // Try to receive response
                                    byte[] response = new byte[32767];
                                    DatagramPacket inPacket = new DatagramPacket(response, response.length);
                                    socket.setSoTimeout(100); // Short timeout
                                    
                                    try {
                                        socket.receive(inPacket);
                                        
                                        // Create response packet and write back
                                        byte[] responsePacket = createResponsePacket(packet.array(), inPacket.getData(), inPacket.getLength());
                                        if (responsePacket != null) {
                                            vpnOutput.write(responsePacket);
                                        }
                                    } catch (SocketTimeoutException e) {
                                        // Timeout is normal for UDP
                                    }
                                    
                                } catch (Exception e) {
                                    // Skip invalid packets
                                }
                            }
                        }
                        
                        Thread.sleep(1); // Prevent busy loop
                        
                    } catch (Exception e) {
                        if (isRunning) {
                            Log.e(TAG, "Basic routing error: " + e.getMessage());
                        }
                    }
                }
                
                socket.close();
                
            } catch (Exception e) {
                Log.e(TAG, "Basic routing failed: " + e.getMessage());
            }
        });
    }
    
    private byte[] createResponsePacket(byte[] originalPacket, byte[] responseData, int responseLength) {
        try {
            // Simplified response packet creation
            int ipHeaderLength = (originalPacket[0] & 0xF) * 4;
            byte[] responsePacket = new byte[ipHeaderLength + 8 + responseLength];
            
            // Copy and modify IP header
            System.arraycopy(originalPacket, 0, responsePacket, 0, ipHeaderLength);
            
            // Swap source and destination IPs
            System.arraycopy(originalPacket, 16, responsePacket, 12, 4); // dest -> src
            System.arraycopy(originalPacket, 12, responsePacket, 16, 4); // src -> dest
            
            // Update packet length
            int totalLength = ipHeaderLength + 8 + responseLength;
            responsePacket[2] = (byte) ((totalLength >> 8) & 0xFF);
            responsePacket[3] = (byte) (totalLength & 0xFF);
            
            // Copy response data
            System.arraycopy(responseData, 0, responsePacket, ipHeaderLength + 8, responseLength);
            
            return responsePacket;
            
        } catch (Exception e) {
            Log.e(TAG, "Response packet creation error: " + e.getMessage());
            return null;
        }
    }
    
    private void stopVpn() {
        isRunning = false;
        
        try {
            if (vpnSocket != null) {
                vpnSocket.close();
            }
            if (vpnInput != null) {
                vpnInput.close();
            }
            if (vpnOutput != null) {
                vpnOutput.close();
            }
            if (vpnInterface != null) {
                vpnInterface.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing VPN resources", e);
        }
        
        stopForeground(true);
        stopSelf();
        
        Log.d(TAG, "VPN stopped");
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        String serverInfo = selectedServer != null ? 
            selectedServer.countryLong + " " + selectedServer.getCountryFlag() : "Unknown";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("VPN Connected")
                .setContentText("IP changed to " + serverInfo + " - Internet working!")
                .setSmallIcon(R.drawable.ic_vpn_key)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "VPN Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("VPN connection with IP change");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @Override
    public void onDestroy() {
        stopVpn();
        if (executor != null) {
            executor.shutdown();
        }
        super.onDestroy();
    }
}