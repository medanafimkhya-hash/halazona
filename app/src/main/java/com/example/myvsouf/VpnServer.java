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
        // Comprehensive mapping for VPN Gate supported countries
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
            case "UK": return "🇬🇧";
            case "DE": return "🇩🇪";
            case "FR": return "🇫🇷";
            case "IT": return "🇮🇹";
            case "ES": return "🇪🇸";
            case "NL": return "🇳🇱";
            case "RU": return "🇷🇺";
            case "BR": return "🇧🇷";
            case "MX": return "🇲🇽";
            case "AR": return "🇦🇷";
            case "CL": return "🇨🇱";
            case "PE": return "🇵🇪";
            case "CO": return "🇨🇴";
            case "VE": return "🇻🇪";
            case "UY": return "🇺🇾";
            case "EC": return "🇪🇨";
            case "BO": return "🇧🇴";
            case "PY": return "🇵🇾";
            case "ZA": return "🇿🇦";
            case "EG": return "🇪🇬";
            case "NG": return "🇳🇬";
            case "KE": return "🇰🇪";
            case "MA": return "🇲🇦";
            case "TN": return "🇹🇳";
            case "DZ": return "🇩🇿";
            case "GH": return "🇬🇭";
            case "UG": return "🇺🇬";
            case "TZ": return "🇹🇿";
            case "ZW": return "🇿🇼";
            case "BW": return "🇧🇼";
            case "ZM": return "🇿🇲";
            case "MW": return "🇲🇼";
            case "MZ": return "🇲🇿";
            case "MG": return "🇲🇬";
            case "MU": return "🇲🇺";
            case "SC": return "🇸🇨";
            case "RE": return "🇷🇪";
            case "YT": return "🇾🇹";
            case "KM": return "🇰🇲";
            case "DJ": return "🇩🇯";
            case "SO": return "🇸🇴";
            case "ET": return "🇪🇹";
            case "ER": return "🇪🇷";
            case "SD": return "🇸🇩";
            case "SS": return "🇸🇸";
            case "CF": return "🇨🇫";
            case "TD": return "🇹🇩";
            case "NE": return "🇳🇪";
            case "ML": return "🇲🇱";
            case "BF": return "🇧🇫";
            case "CI": return "🇨🇮";
            case "LR": return "🇱🇷";
            case "SL": return "🇸🇱";
            case "GN": return "🇬🇳";
            case "GW": return "🇬🇼";
            case "GM": return "🇬🇲";
            case "SN": return "🇸🇳";
            case "MR": return "🇲🇷";
            case "CV": return "🇨🇻";
            case "ST": return "🇸🇹";
            case "GQ": return "🇬🇶";
            case "GA": return "🇬🇦";
            case "CM": return "🇨🇲";
            case "CG": return "🇨🇬";
            case "CD": return "🇨🇩";
            case "AO": return "🇦🇴";
            case "NA": return "🇳🇦";
            case "LS": return "🇱🇸";
            case "SZ": return "🇸🇿";
            default: return "🌍";
        }
    }
    
    public boolean hasValidOpenVpnConfig() {
        return openVpnConfig != null && !openVpnConfig.isEmpty() && openVpnConfig.length() > 100;
    }
    
    public String getDecodedOpenVpnConfig() {
        if (openVpnConfig != null && !openVpnConfig.isEmpty()) {
            try {
                // VPN Gate provides base64 encoded OpenVPN config
                byte[] decoded = android.util.Base64.decode(openVpnConfig, android.util.Base64.DEFAULT);
                return new String(decoded, "UTF-8");
            } catch (Exception e) {
                android.util.Log.e("VpnServer", "Error decoding OpenVPN config", e);
                return null;
            }
        }
        return null;
    }
    
    public String getVpnServerHost() {
        String config = getDecodedOpenVpnConfig();
        if (config != null) {
            // Extract server host from OpenVPN config
            String[] lines = config.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("remote ")) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length >= 2) {
                        return parts[1];
                    }
                }
            }
        }
        return ip; // fallback to IP from CSV
    }
    
    public int getVpnServerPort() {
        String config = getDecodedOpenVpnConfig();
        if (config != null) {
            // Extract server port from OpenVPN config
            String[] lines = config.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("remote ")) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length >= 3) {
                        try {
                            return Integer.parseInt(parts[2]);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return 1194; // default OpenVPN port
    }
    
    @Override
    public String toString() {
        return countryLong + " (" + getFormattedSpeed() + ")";
    }
}