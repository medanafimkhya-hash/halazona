package com.example.myvsouf;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IpAddressTestActivity extends AppCompatActivity {
    
    private TextView tvIpAddress;
    private TextView tvInternetProvider;
    private TextView tvLocation;
    private TextView tvPincode;
    private TextView tvTimezone;
    
    private OkHttpClient client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_address_test);
        
        initializeViews();
        setupClickListeners();
        loadIpInfo();
    }
    
    private void initializeViews() {
        tvIpAddress = findViewById(R.id.tvIpAddress);
        tvInternetProvider = findViewById(R.id.tvInternetProvider);
        tvLocation = findViewById(R.id.tvLocation);
        tvPincode = findViewById(R.id.tvPincode);
        tvTimezone = findViewById(R.id.tvTimezone);
        
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    private void setupClickListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        ImageView btnRefreshIp = findViewById(R.id.btnRefreshIp);
        btnRefreshIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadIpInfo();
            }
        });
        
        FloatingActionButton fabRefresh = findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadIpInfo();
            }
        });
    }
    
    private void loadIpInfo() {
        new GetIpInfoTask().execute();
    }
    
    private class GetIpInfoTask extends AsyncTask<Void, Void, IpInfo> {
        
        @Override
        protected IpInfo doInBackground(Void... voids) {
            try {
                // Use ipapi.co for IP geolocation (free service)
                Request request = new Request.Builder()
                        .url("https://ipapi.co/json/")
                        .build();
                
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return null;
                }
                
                String responseBody = response.body().string();
                return parseIpInfo(responseBody);
                
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(IpInfo ipInfo) {
            if (ipInfo != null) {
                updateUI(ipInfo);
            } else {
                // Show default/sample data if API fails
                showSampleData();
                Toast.makeText(IpAddressTestActivity.this, "Failed to load IP info, showing sample data", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private IpInfo parseIpInfo(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            IpInfo ipInfo = new IpInfo();
            ipInfo.ip = jsonObject.has("ip") ? jsonObject.get("ip").getAsString() : "Unknown";
            ipInfo.city = jsonObject.has("city") ? jsonObject.get("city").getAsString() : "Unknown";
            ipInfo.region = jsonObject.has("region") ? jsonObject.get("region").getAsString() : "Unknown";
            ipInfo.country = jsonObject.has("country_name") ? jsonObject.get("country_name").getAsString() : "Unknown";
            ipInfo.postal = jsonObject.has("postal") ? jsonObject.get("postal").getAsString() : "Unknown";
            ipInfo.timezone = jsonObject.has("timezone") ? jsonObject.get("timezone").getAsString() : "Unknown";
            ipInfo.org = jsonObject.has("org") ? jsonObject.get("org").getAsString() : "Unknown";
            
            return ipInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void updateUI(IpInfo ipInfo) {
        tvIpAddress.setText(ipInfo.ip);
        tvInternetProvider.setText(ipInfo.org);
        tvLocation.setText(ipInfo.city + ", " + ipInfo.region + ", " + ipInfo.country);
        tvPincode.setText(ipInfo.postal);
        tvTimezone.setText(ipInfo.timezone);
    }
    
    private void showSampleData() {
        // Show sample data as seen in the screenshot
        tvIpAddress.setText("49.32.246.119");
        tvInternetProvider.setText("Reliance Jio Infocomm Ltd");
        tvLocation.setText("Mumbai, Maharashtra, India");
        tvPincode.setText("400070");
        tvTimezone.setText("Asia/Kolkata");
    }
    
    private static class IpInfo {
        String ip;
        String city;
        String region;
        String country;
        String postal;
        String timezone;
        String org;
    }
}