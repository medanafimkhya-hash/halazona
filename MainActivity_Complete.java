package com.example.myvsouf;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int VPN_REQUEST_CODE = 1;
    
    private Button btnConnect;
    private TextView tvConnectionStatus;
    private TextView tvTimer;
    private TextView tvCurrentCountry;
    private TextView tvPing;
    private TextView btnChangeLocation;
    private ImageView ivCurrentFlag;
    
    private VpnGateApiService apiService;
    private VpnServer currentServer;
    private boolean isConnected = false;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long connectionStartTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeServices();
        setupClickListeners();
        loadDefaultServer();
    }
    
    private void initializeViews() {
        btnConnect = findViewById(R.id.btnConnect);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvCurrentCountry = findViewById(R.id.tvCurrentCountry);
        tvPing = findViewById(R.id.tvPing);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        ivCurrentFlag = findViewById(R.id.ivCurrentFlag);
        
        // Initialize timer
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000);
            }
        };
    }
    
    private void initializeServices() {
        apiService = new VpnGateApiService();
    }
    
    private void setupClickListeners() {
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    disconnectVpn();
                } else {
                    connectVpn();
                }
            }
        });
        
        btnChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VpnLocationsActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        
        // Add click listener for info button (IP test)
        ImageView btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IpAddressTestActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void loadDefaultServer() {
        // Load servers and set default server from any available country
        apiService.fetchVpnServers(new VpnGateApiService.VpnServerCallback() {
            @Override
            public void onSuccess(List<VpnServer> servers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Find best server (highest speed) as default
                        VpnServer bestServer = null;
                        long bestSpeed = 0;
                        
                        for (VpnServer server : servers) {
                            try {
                                long speed = Long.parseLong(server.speed);
                                if (speed > bestSpeed) {
                                    bestSpeed = speed;
                                    bestServer = server;
                                }
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                        
                        if (bestServer != null) {
                            currentServer = bestServer;
                            updateServerDisplay();
                        } else if (!servers.isEmpty()) {
                            // If no speed info, use first server
                            currentServer = servers.get(0);
                            updateServerDisplay();
                        }
                        
                        Toast.makeText(MainActivity.this, "Loaded " + servers.size() + " servers from " + getUniqueCountries(servers) + " countries", Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to load servers: " + error, Toast.LENGTH_SHORT).show();
                        // Set default fallback server
                        setDefaultServer();
                    }
                });
            }
        });
    }
    
    private int getUniqueCountries(List<VpnServer> servers) {
        java.util.Set<String> countries = new java.util.HashSet<>();
        for (VpnServer server : servers) {
            countries.add(server.countryShort);
        }
        return countries.size();
    }
    
    private void setDefaultServer() {
        currentServer = new VpnServer();
        currentServer.countryLong = "Japan";
        currentServer.countryShort = "JP";
        currentServer.ping = "12";
        currentServer.speed = "100000000";
        updateServerDisplay();
    }
    
    private void updateServerDisplay() {
        if (currentServer != null) {
            tvCurrentCountry.setText(currentServer.countryLong);
            tvPing.setText(currentServer.getFormattedPing());
            
            // Set flag emoji (simplified approach)
            String flagEmoji = currentServer.getCountryFlag();
            // Note: For a real app, you'd use proper flag images
            // For now, we'll keep the default US flag drawable
        }
    }
    
    private void connectVpn() {
        if (currentServer == null) {
            Toast.makeText(this, "Please select a server first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check VPN permission
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            startVpnService();
        }
    }
    
    private void startVpnService() {
        if (currentServer == null) {
            Toast.makeText(this, "Please select a server first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start VPN service with selected server (ANY COUNTRY)
        Intent intent = new Intent(this, MyVpnService.class);
        intent.setAction("START_VPN");
        intent.putExtra("server", currentServer); // Pass the selected server
        startService(intent);
        
        isConnected = true;
        connectionStartTime = System.currentTimeMillis();
        
        updateConnectionStatus();
        startTimer();
        
        Toast.makeText(this, "Connecting to " + currentServer.countryLong + " " + currentServer.getCountryFlag() + "\nYour IP will change to " + currentServer.countryLong, Toast.LENGTH_LONG).show();
    }
    
    private void disconnectVpn() {
        // Stop the VPN service
        Intent intent = new Intent(this, MyVpnService.class);
        intent.setAction("STOP_VPN");
        startService(intent);
        
        isConnected = false;
        stopTimer();
        updateConnectionStatus();
        
        Toast.makeText(this, "VPN Disconnected - IP restored", Toast.LENGTH_SHORT).show();
    }
    
    private void updateConnectionStatus() {
        if (isConnected) {
            tvConnectionStatus.setText("Connected");
            btnConnect.setText("Tap to Disconnect");
        } else {
            tvConnectionStatus.setText("Not Connected");
            btnConnect.setText("Tap to Connect");
            tvTimer.setText("00 : 00 : 00");
        }
    }
    
    private void startTimer() {
        timerHandler.post(timerRunnable);
    }
    
    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    private void updateTimer() {
        if (isConnected) {
            long elapsedTime = System.currentTimeMillis() - connectionStartTime;
            long seconds = (elapsedTime / 1000) % 60;
            long minutes = (elapsedTime / (1000 * 60)) % 60;
            long hours = (elapsedTime / (1000 * 60 * 60)) % 24;
            
            String timeString = String.format("%02d : %02d : %02d", hours, minutes, seconds);
            tvTimer.setText(timeString);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVpnService();
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            // Server selection result
            VpnServer selectedServer = (VpnServer) data.getSerializableExtra("selected_server");
            if (selectedServer != null) {
                currentServer = selectedServer;
                updateServerDisplay();
                Toast.makeText(this, "Server changed to " + selectedServer.countryLong + " " + selectedServer.getCountryFlag(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}