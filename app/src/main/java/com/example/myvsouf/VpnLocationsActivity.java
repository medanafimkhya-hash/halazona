package com.example.myvsouf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VpnLocationsActivity extends AppCompatActivity {
    
    private RecyclerView recyclerViewServers;
    private LinearLayout loadingLayout;
    private VpnServerAdapter serverAdapter;
    private VpnGateApiService apiService;
    private List<VpnServer> serverList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_locations);
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadServers();
    }
    
    private void initializeViews() {
        recyclerViewServers = findViewById(R.id.recyclerViewServers);
        loadingLayout = findViewById(R.id.loadingLayout);
        
        apiService = new VpnGateApiService();
        serverList = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        serverAdapter = new VpnServerAdapter(serverList, new VpnServerAdapter.OnServerClickListener() {
            @Override
            public void onServerClick(VpnServer server) {
                // Return selected server to MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_server", server);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        
        recyclerViewServers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewServers.setAdapter(serverAdapter);
    }
    
    private void setupClickListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        ImageView btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadServers();
            }
        });
    }
    
    private void loadServers() {
        showLoading(true);
        
        apiService.fetchVpnServers(new VpnGateApiService.VpnServerCallback() {
            @Override
            public void onSuccess(List<VpnServer> servers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        serverList.clear();
                        serverList.addAll(servers);
                        serverAdapter.notifyDataSetChanged();
                        
                        // Update title with server count
                        setTitle("VPN Locations (" + servers.size() + ")");
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(VpnLocationsActivity.this, "Error loading servers: " + error, Toast.LENGTH_LONG).show();
                        
                        // Load sample data for demo
                        loadSampleServers();
                    }
                });
            }
        });
    }
    
    private void loadSampleServers() {
        serverList.clear();
        
        // Add sample servers based on the screenshots
        VpnServer vietnam1 = new VpnServer();
        vietnam1.countryLong = "Viet Nam";
        vietnam1.countryShort = "VN";
        vietnam1.speed = "107374182"; // ~12.7 Mbps
        vietnam1.vpnSessions = "36";
        vietnam1.ping = "45";
        serverList.add(vietnam1);
        
        VpnServer japan1 = new VpnServer();
        japan1.countryLong = "Japan";
        japan1.countryShort = "JP";
        japan1.speed = "2576980377"; // ~304.7 Mbps
        japan1.vpnSessions = "275";
        japan1.ping = "12";
        serverList.add(japan1);
        
        VpnServer usa1 = new VpnServer();
        usa1.countryLong = "United States";
        usa1.countryShort = "US";
        usa1.speed = "3654957056"; // ~431.7 Mbps
        usa1.vpnSessions = "149";
        usa1.ping = "8";
        serverList.add(usa1);
        
        VpnServer japan2 = new VpnServer();
        japan2.countryLong = "Japan";
        japan2.countryShort = "JP";
        japan2.speed = "5565317120"; // ~656.4 Mbps
        japan2.vpnSessions = "1";
        japan2.ping = "15";
        serverList.add(japan2);
        
        VpnServer japan3 = new VpnServer();
        japan3.countryLong = "Japan";
        japan3.countryShort = "JP";
        japan3.speed = "658505728"; // ~77.7 Mbps
        japan3.vpnSessions = "6";
        japan3.ping = "18";
        serverList.add(japan3);
        
        VpnServer japan4 = new VpnServer();
        japan4.countryLong = "Japan";
        japan4.countryShort = "JP";
        japan4.speed = "1271310336"; // ~150.2 Mbps
        japan4.vpnSessions = "2";
        japan4.ping = "22";
        serverList.add(japan4);
        
        VpnServer vietnam2 = new VpnServer();
        vietnam2.countryLong = "Viet Nam";
        vietnam2.countryShort = "VN";
        vietnam2.speed = "712746496"; // ~84.1 Mbps
        vietnam2.vpnSessions = "3";
        vietnam2.ping = "38";
        serverList.add(vietnam2);
        
        VpnServer korea = new VpnServer();
        korea.countryLong = "Korea Republic of";
        korea.countryShort = "KR";
        korea.speed = "4395630592"; // ~518.6 Mbps
        korea.vpnSessions = "84";
        korea.ping = "25";
        serverList.add(korea);
        
        VpnServer vietnam3 = new VpnServer();
        vietnam3.countryLong = "Viet Nam";
        vietnam3.countryShort = "VN";
        vietnam3.speed = "892679577"; // ~105.3 Mbps
        vietnam3.vpnSessions = "29";
        vietnam3.ping = "42";
        serverList.add(vietnam3);
        
        serverAdapter.notifyDataSetChanged();
        setTitle("VPN Locations (" + serverList.size() + ")");
    }
    
    private void showLoading(boolean show) {
        if (show) {
            loadingLayout.setVisibility(View.VISIBLE);
            recyclerViewServers.setVisibility(View.GONE);
        } else {
            loadingLayout.setVisibility(View.GONE);
            recyclerViewServers.setVisibility(View.VISIBLE);
        }
    }
}