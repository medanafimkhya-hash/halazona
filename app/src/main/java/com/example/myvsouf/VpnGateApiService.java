package com.example.myvsouf;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VpnGateApiService {
    private static final String TAG = "VpnGateApiService";
    private static final String API_URL = "https://www.vpngate.net/api/iphone/";
    
    private OkHttpClient client;
    private Gson gson;
    
    public interface VpnServerCallback {
        void onSuccess(List<VpnServer> servers);
        void onError(String error);
    }
    
    public VpnGateApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }
    
    public void fetchVpnServers(VpnServerCallback callback) {
        new FetchServersTask(callback).execute();
    }
    
    private class FetchServersTask extends AsyncTask<Void, Void, List<VpnServer>> {
        private VpnServerCallback callback;
        private String errorMessage;
        
        public FetchServersTask(VpnServerCallback callback) {
            this.callback = callback;
        }
        
        @Override
        protected List<VpnServer> doInBackground(Void... voids) {
            try {
                Request request = new Request.Builder()
                        .url(API_URL)
                        .build();
                
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    errorMessage = "HTTP Error: " + response.code();
                    return null;
                }
                
                String responseBody = response.body().string();
                Log.d(TAG, "Raw API Response: " + responseBody);
                
                // Parse CSV format from VPN Gate API
                return parseVpnServers(responseBody);
                
            } catch (IOException e) {
                Log.e(TAG, "Network error", e);
                errorMessage = "Network error: " + e.getMessage();
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Parsing error", e);
                errorMessage = "Parsing error: " + e.getMessage();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<VpnServer> servers) {
            if (servers != null && !servers.isEmpty()) {
                callback.onSuccess(servers);
            } else {
                callback.onError(errorMessage != null ? errorMessage : "Unknown error");
            }
        }
    }
    
    private List<VpnServer> parseVpnServers(String csvData) {
        List<VpnServer> servers = new ArrayList<>();
        
        try {
            String[] lines = csvData.split("\n");
            
            // Skip first 2 lines (header info)
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("*") || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 15) {
                    VpnServer server = new VpnServer();
                    server.hostName = parts[0];
                    server.ip = parts[1];
                    server.score = parts[2];
                    server.ping = parts[3];
                    server.speed = parts[4];
                    server.countryLong = parts[5];
                    server.countryShort = parts[6];
                    server.vpnSessions = parts[7];
                    server.uptime = parts[8];
                    server.totalUsers = parts[9];
                    server.totalTraffic = parts[10];
                    server.logType = parts[11];
                    server.operator = parts[12];
                    server.message = parts[13];
                    server.openVpnConfig = parts[14];
                    
                    // Only add servers with valid data and OpenVPN config
                    if (!server.ip.isEmpty() && !server.countryLong.isEmpty() 
                        && server.openVpnConfig != null && !server.openVpnConfig.isEmpty()
                        && server.hasValidOpenVpnConfig()) {
                        servers.add(server);
                        Log.d(TAG, "Added server: " + server.countryLong + " (" + server.countryShort + ") - " + server.ip);
                    }
                }
            }
            
            Log.d(TAG, "Total servers parsed: " + servers.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV data", e);
        }
        
        return servers;
    }
}