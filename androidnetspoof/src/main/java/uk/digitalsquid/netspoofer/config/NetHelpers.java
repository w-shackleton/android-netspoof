/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer.config;

import android.net.DhcpInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public final class NetHelpers implements LogConf {
    private NetHelpers() {}
    
    public static final NetworkInterface getIface(InetAddress iface) {
        try {
            return NetworkInterface.getByInetAddress(iface);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static final InetAddress inetFromInt(int ip) throws UnknownHostException {
        return InetAddress.getByAddress(new byte[] {
                (byte) ((ip >> 0 ) & 0xFF),
                (byte) ((ip >> 8 ) & 0xFF),
                (byte) ((ip >> 16) & 0xFF),
                (byte) ((ip >>>24) & 0xFF),
                        });
    }
    
    public static final long inetFromByte(byte[] ip) {
        return
            ((long)(ip[0]&0xFF) << 0 ) +
            ((long)(ip[1]&0xFF) << 8 ) +
            ((long)(ip[2]&0xFF) << 16) +
            ((long)(ip[3]&0xFF) << 24);
    }

    public static final InetAddress inetFromInt(long ip) throws UnknownHostException {
        return InetAddress.getByAddress(new byte[] {
                (byte) ((ip >> 0 ) & 0xFF),
                (byte) ((ip >> 8 ) & 0xFF),
                (byte) ((ip >> 16) & 0xFF),
                (byte) ((ip >>>24) & 0xFF),
        });
    }

    public static final InetAddress reverseInetFromInt(long ip) throws UnknownHostException {
        return InetAddress.getByAddress(new byte[] {
                (byte) ((ip >>>24) & 0xFF),
                (byte) ((ip >> 16) & 0xFF),
                (byte) ((ip >> 8 ) & 0xFF),
                (byte) ((ip >> 0 ) & 0xFF),
                        });
    }

    public static final String inetFromHex(String hex) {
        if(hex.length() == 8) {
            String[] s4 = new String[4];
            s4[3] = String.valueOf(Integer.parseInt(hex.substring(0, 2), 16));
            s4[2] = String.valueOf(Integer.parseInt(hex.substring(2, 4), 16));
            s4[1] = String.valueOf(Integer.parseInt(hex.substring(4, 6), 16));
            s4[0] = String.valueOf(Integer.parseInt(hex.substring(6, 8), 16));

            return s4[0] + "." + s4[1] + "." + s4[2] + "." + s4[3];
        }
        return null;
    }
    
    /**
     * 
     * @param ip
     * @return a long value, to avoid IP signed-ness
     */
    public static final long reverseInetFromByte(byte[] ip) {
        return
            ((long)(ip[0]&0xFF) << 24) +
            ((long)(ip[1]&0xFF) << 16) +
            ((long)(ip[2]&0xFF) << 8 ) +
            ((long)(ip[3]&0xFF) << 0 );
    }
    
    public static final class GatewayData implements Serializable {
        private static final long serialVersionUID = -2588873022535534899L;
        
        private final InetAddress gateway;
        private final String subnet;
        
        public GatewayData(InetAddress gateway, String subnet) {
            this.gateway = gateway;
            this.subnet = subnet;
        }

        public InetAddress getGateway() {
            return gateway;
        }

        public String getSubnet() {
            return subnet;
        }
    }
    
    /**
     * Represents an entry in the route table.
     * @author Will Shackleton <will@digitalsquid.co.uk>
     *
     */
    public static final class RouteEntry implements Serializable {

        private static final long serialVersionUID = 7930709441175690366L;
        
        private String destination;
        private String gateway;
        private String genmask;
        private String iface;
        public void setDestination(String destination) {
            this.destination = destination;
        }
        public String getDestination() {
            return destination;
        }
        public void setGateway(String gateway) {
            this.gateway = gateway;
        }
        public String getGateway() {
            return gateway;
        }
        public void setGenmask(String genmask) {
            this.genmask = genmask;
        }
        public String getGenmask() {
            return genmask;
        }
        public void setIface(String iface) {
            this.iface = iface;
        }
        public String getIface() {
            return iface;
        }
    }
    
    /**
     * 
     * @param iface
     * @param info a DhcpInfo to use on Lollipop devices
     * @return
     * @throws UnknownHostException
     */
    public static final GatewayData getDefaultGateway(NetworkInterface iface, DhcpInfo info) throws UnknownHostException {
        if(iface == null) throw new IllegalArgumentException("iface is null");
        
        try { FileFinder.initialise(); } catch (FileNotFoundException e1) { }
        
        final String ifacename = iface.getDisplayName();
        Log.d(TAG, "Checking for routes on iface " + ifacename);
        
        ArrayList<RouteEntry> routes = parseRoutes();
        
        String gateway = "", subnet = "";
        for(RouteEntry route : routes) {
            if(!route.getIface().equalsIgnoreCase(ifacename)) continue; // Ignore routes not on wifi
            if(route.getDestination().equals("0.0.0.0")) {
                gateway = route.getGateway();
            }
            if(route.getGateway().equals("0.0.0.0") && !route.getGenmask().equals("255.255.255.255")) {
                subnet = route.getGenmask();
            }
        }

        // Lollipop, use DhcpInfo
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && info != null) {
            gateway = inetFromInt(info.gateway & 0xFFFFFFFFL).getHostAddress();
            subnet = inetFromInt(info.netmask & 0xFFFFFFFFL).getHostAddress();
        }

        return new GatewayData(InetAddress.getByName(gateway), subnet);
    }

    public static final ArrayList<RouteEntry> parseRoutes() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/route"));

            ArrayList<RouteEntry> result = new ArrayList<RouteEntry>();

            String line;
            while((line = reader.readLine())!=null) {
                line = line.trim();
                StringTokenizer tkz = new StringTokenizer(line, "\t");
                ArrayList<String> tokens = new ArrayList<String>();
                while(tkz.hasMoreElements()) {
                    tokens.add(tkz.nextToken());
                }
                // If valid line and line represents a gateway
                if(tokens.size() >= 8) {
                    if (tokens.get(0).equalsIgnoreCase("iface")) {
                        continue;
                    }
                    String iface = tokens.get(0);
                    String destination = inetFromHex(tokens.get(1));
                    String gateway = inetFromHex(tokens.get(2));
                    String mask = inetFromHex(tokens.get(7));
                    if(destination != null && gateway != null && mask != null) {
                        RouteEntry elem = new RouteEntry();
                        elem.setGateway(gateway);
                        elem.setDestination(destination);
                        elem.setIface(iface);
                        elem.setGenmask(mask);
                        result.add(elem);
                    }
                }
            }
            reader.close();
            return result;
        }
        catch(IOException e)
        {
            Log.e(TAG, "Failed to read route info", e);
            return new ArrayList<RouteEntry>();
        }
    }
}
