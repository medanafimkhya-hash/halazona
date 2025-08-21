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

import java.io.IOException;
import java.nio.ByteBuffer;

public class MyVpnService extends VpnService {
    private static final String TAG = "MyVpnService";
    private static final String CHANNEL_ID = "VPN_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START_VPN".equals(action)) {
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
        
        try {
            // Create VPN interface
            Builder builder = new Builder();
            builder.setMtu(1500);
            builder.addAddress("10.0.0.2", 32);
            builder.addRoute("0.0.0.0", 0);
            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("8.8.4.4");
            builder.setSession("Free VPN Demo");
            
            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                return;
            }
            
            isRunning = true;
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start VPN thread (simplified for demo)
            vpnThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runVpn();
                }
            });
            vpnThread.start();
            
            Log.d(TAG, "VPN started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
            stopVpn();
        }
    }
    
    private void stopVpn() {
        isRunning = false;
        
        if (vpnThread != null) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }
        
        stopForeground(true);
        stopSelf();
        
        Log.d(TAG, "VPN stopped");
    }
    
    private void runVpn() {
        // This is a simplified VPN implementation for demo purposes
        // In a real VPN app, you would handle packet routing, encryption, etc.
        
        try {
            // Create a simple packet handler
            ByteBuffer packet = ByteBuffer.allocate(32767);
            
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                // In a real implementation, you would:
                // 1. Read packets from vpnInterface
                // 2. Process/encrypt packets
                // 3. Forward to VPN server
                // 4. Receive response from server
                // 5. Write back to vpnInterface
                
                Thread.sleep(1000); // Simple delay for demo
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "VPN thread interrupted");
        } catch (Exception e) {
            Log.e(TAG, "Error in VPN thread", e);
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent disconnectIntent = new Intent(this, MyVpnService.class);
        disconnectIntent.setAction("STOP_VPN");
        PendingIntent disconnectPendingIntent = PendingIntent.getService(this, 0, 
                disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Free VPN Demo")
                .setContentText("VPN is connected")
                .setSmallIcon(R.drawable.ic_vpn_key)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_close, "Disconnect", disconnectPendingIntent)
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
            channel.setDescription("VPN connection status");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }
}