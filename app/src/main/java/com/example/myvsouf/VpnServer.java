package com.example.myvsouf;

import java.io.Serializable;

public class VpnServer implements Serializable {
    public String hostName;
    public String ip;
    public String score;
    public String ping;
    public String speed;
    public String countryLong;
    public String countryShort;
    public String vpnSessions;
    public String uptime;
    public String totalUsers;
    public String totalTraffic;
    public String logType;
    public String operator;
    public String message;
    public String openVpnConfig;
    
    public VpnServer() {
        // Default constructor
    }
    
    public String getFormattedSpeed() {
        try {
            long speedBps = Long.parseLong(speed);
            if (speedBps == 0) return "0 Mbps";
            
            double speedMbps = speedBps / (1024.0 * 1024.0 * 8.0); // Convert to Mbps
            return String.format("%.1f Mbps", speedMbps);
        } catch (NumberFormatException e) {
            return "0 Mbps";
        }
    }
    
    public String getFormattedPing() {
        try {
            int pingMs = Integer.parseInt(ping);
            return pingMs + " ms";
        } catch (NumberFormatException e) {
            return "0 ms";
        }
    }
    
    public int getUserCount() {
        try {
            return Integer.parseInt(vpnSessions);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public String getCountryFlag() {
        // Simple mapping for common countries based on country code
        switch (countryShort.toUpperCase()) {
            case "US": return "🇺🇸";
            case "JP": return "🇯🇵";
            case "KR": return "🇰🇷";
            case "VN": return "🇻🇳";
            case "TH": return "🇹🇭";
            case "MY": return "🇲🇾";
            case "SG": return "🇸🇬";
            case "ID": return "🇮🇩";
            case "PH": return "🇵🇭";
            case "TW": return "🇹🇼";
            case "HK": return "🇭🇰";
            case "CN": return "🇨🇳";
            case "IN": return "🇮🇳";
            case "AU": return "🇦🇺";
            case "NZ": return "🇳🇿";
            case "CA": return "🇨🇦";
            case "GB": return "🇬🇧";
            case "DE": return "🇩🇪";
            case "FR": return "🇫🇷";
            case "IT": return "🇮🇹";
            case "ES": return "🇪🇸";
            case "NL": return "🇳🇱";
            case "RU": return "🇷🇺";
            case "BR": return "🇧🇷";
            case "MX": return "🇲🇽";
            default: return "🌍";
        }
    }
    
    @Override
    public String toString() {
        return countryLong + " (" + getFormattedSpeed() + ")";
    }
}