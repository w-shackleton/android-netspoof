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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

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
	
	public static final InetAddress reverseInetFromInt(int ip) throws UnknownHostException {
		return reverseInetFromInt((long)ip);
	}
	public static final InetAddress reverseInetFromInt(long ip) throws UnknownHostException {
		return InetAddress.getByAddress(new byte[] {
				(byte) ((ip >>>24) & 0xFF),
				(byte) ((ip >> 16) & 0xFF),
				(byte) ((ip >> 8 ) & 0xFF),
				(byte) ((ip >> 0 ) & 0xFF),
						});
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
	static final class RouteEntry implements Serializable {

		private static final long serialVersionUID = 7930709441175690366L;
		
		private String destination;
		private String gateway;
		private String genmask;
		private String flags;
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
		public void setFlags(String flags) {
			this.flags = flags;
		}
		public String getFlags() {
			return flags;
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
	 * @return
	 * @throws UnknownHostException
	 */
	public static final GatewayData getDefaultGateway(NetworkInterface iface) throws UnknownHostException {
		if(iface == null) throw new IllegalArgumentException("iface is null");
		
		try { FileFinder.initialise(); } catch (FileNotFoundException e1) { }
		
		final String ifacename = iface.getDisplayName();
		Log.d(TAG, "Checking for routes on iface " + ifacename);
		
		ArrayList<RouteEntry> routes = getRoutes();
		
		String gateway = "", subnet = "";
		for(RouteEntry route : routes) {
			if(!route.getIface().equalsIgnoreCase(ifacename)) continue; // Ignore routes not on wifi
			if(route.getFlags().contains("G")) { // This is a route with a gateway
				gateway = route.getGateway();
			}
			if(route.getFlags().equals("U")) {
				subnet = route.getGenmask();
			}
		}
		if(gateway.equals("")) { // Try again
			Log.i(TAG, "Trying more agressive approach to finding gateway");
			for(RouteEntry route : routes) {
				if(!route.getIface().equalsIgnoreCase(ifacename)) continue; // Ignore routes not on wifi
				if(!route.getGateway().equals("0.0.0.0")) { // Just try the first proper gateway
					gateway = route.getGateway();
					break;
				}
			}
		}
		if(subnet.equals("")) { // Try again
			Log.i(TAG, "Trying more agressive approach to finding subnet");
			for(RouteEntry route : routes) {
				if(!route.getIface().equalsIgnoreCase(ifacename)) continue; // Ignore routes not on wifi
				if(!route.getGateway().equals("0.0.0.0")) { // Just try the first proper subnet mask
					subnet = route.getGenmask();
					break;
				}
			}
		}
		return new GatewayData(InetAddress.getByName(gateway), subnet);
	}
	
	public static final ArrayList<RouteEntry> getRoutes() throws UnknownHostException {
		try { FileFinder.initialise(); } catch (FileNotFoundException e1) { }
		
		List<String> routeArgs = new ArrayList<String>();
		// If not using BB, don't add it.
		if(!FileFinder.BUSYBOX.equals("")) routeArgs.add(FileFinder.BUSYBOX);
		routeArgs.add("route");
		routeArgs.add("-n");
		
		ArrayList<RouteEntry> routes = new ArrayList<NetHelpers.RouteEntry>();
		try {
			// Run route -n, get lines
			List<String> routeTable = IOHelpers.runProcessOutputToLines(routeArgs);
			
			int linePosition = 0; // Used to ignore the first 2 lines
			for(String line : routeTable) {
				if(linePosition++ < 2) continue;
				String line2 = line.replaceAll("\\s+", " "); // Single space only between each part
				Log.v(TAG, "Parsing routing line " + line2);
				StringTokenizer parts = new StringTokenizer(line2, " ");
				if(parts.countTokens() < 8) { // Should be 8 parts
					Log.w(TAG, "Found incorrectly formatted route line!");
					continue;
				}
				
				RouteEntry entry = new RouteEntry();
				entry.setDestination(parts.nextToken());
				entry.setGateway(parts.nextToken());
				entry.setGenmask(parts.nextToken());
				entry.setFlags(parts.nextToken());
				parts.nextToken(); // Unused parts
				parts.nextToken();
				parts.nextToken();
				entry.setIface(parts.nextToken());
				routes.add(entry);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new UnknownHostException("Error executing 'route' command.");
		}
		
		return routes;
	}
	
	/**
	 * Checks for a file's existence on an HTTP server.
	 * @param file
	 * @param isFail If the redirected url = isFail, returns false
	 * @return
	 */
	public static final boolean checkFileExistsOnWeb(String file, String isFail) {
		try {
			URL upgradeUrl = new URL(file);
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) upgradeUrl.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("HEAD");
			
			int code = conn.getResponseCode();
			URL url = conn.getURL();
			
			if(isFail == null) isFail = "";
			
			return code == HttpURLConnection.HTTP_OK && !url.toExternalForm().equals(isFail);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL", e);
		} catch (IOException e) {
			Log.d(TAG, "Failed to check for HTTP file, probably no internet.");
		}
		return false;
	}
}
