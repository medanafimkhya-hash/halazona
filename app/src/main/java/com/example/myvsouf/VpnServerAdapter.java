package com.example.myvsouf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VpnServerAdapter extends RecyclerView.Adapter<VpnServerAdapter.ServerViewHolder> {
    
    private List<VpnServer> servers;
    private OnServerClickListener clickListener;
    
    public interface OnServerClickListener {
        void onServerClick(VpnServer server);
    }
    
    public VpnServerAdapter(List<VpnServer> servers, OnServerClickListener clickListener) {
        this.servers = servers;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vpn_server, parent, false);
        return new ServerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        VpnServer server = servers.get(position);
        holder.bind(server, clickListener);
    }
    
    @Override
    public int getItemCount() {
        return servers.size();
    }
    
    static class ServerViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvCountryFlag;
        private TextView tvCountryName;
        private TextView tvSpeed;
        private TextView tvUserCount;
        
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvCountryFlag = itemView.findViewById(R.id.tvCountryFlag);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            tvSpeed = itemView.findViewById(R.id.tvSpeed);
            tvUserCount = itemView.findViewById(R.id.tvUserCount);
        }
        
        public void bind(VpnServer server, OnServerClickListener clickListener) {
            tvCountryFlag.setText(server.getCountryFlag());
            tvCountryName.setText(server.countryLong);
            tvSpeed.setText(server.getFormattedSpeed());
            tvUserCount.setText(String.valueOf(server.getUserCount()));
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onServerClick(server);
                    }
                }
            });
        }
    }
}