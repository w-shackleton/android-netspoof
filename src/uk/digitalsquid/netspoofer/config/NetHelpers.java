/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
 * Copyright (C) 2011 Will Shackleton
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
	 * 
	 * @param iface
	 * @return
	 * @throws UnknownHostException
	 */
	public static final GatewayData getDefaultGateway(NetworkInterface iface) throws UnknownHostException {
		if(iface == null) throw new IllegalArgumentException("iface is null");
		
		try { FileFinder.initialise(); } catch (FileNotFoundException e1) { }
		
		String ifacename = iface.getDisplayName();
		List<String> routeArgs = new ArrayList<String>();
		// If not using BB, don't add it.
		if(!FileFinder.BUSYBOX.equals("")) routeArgs.add(FileFinder.BUSYBOX);
		routeArgs.add("route");
		routeArgs.add("-n");
		
		String importantRouteLine = "";
		String localnetRouteLine = "";
		try {
			List<String> routeTable = IOHelpers.runProcessOutputToLines(routeArgs);
			for(String line : routeTable) {
				if(line.contains(ifacename)) { // Important info
					importantRouteLine = line;
					if(localnetRouteLine.equals("")) {
						localnetRouteLine = line;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new UnknownHostException("Error executing 'route' command.");
		}
		
		if(importantRouteLine.equals("")) throw new UnknownHostException("Empty route table line received");
		importantRouteLine = Pattern.compile("\\s+").matcher(importantRouteLine).replaceAll(" ");
		Log.v(TAG, "Found gateway line " + importantRouteLine);
		StringTokenizer tkz = new StringTokenizer(importantRouteLine, " ");
		String gateway;
		try {
			tkz.nextToken();
			gateway = tkz.nextToken();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
			throw new UnknownHostException("Not enough elements in route line " + importantRouteLine);
		}
		
		if(localnetRouteLine.equals("")) throw new UnknownHostException("Empty route table line received");
		importantRouteLine = Pattern.compile("\\s+").matcher(localnetRouteLine).replaceAll(" ");
		Log.v(TAG, "Found gateway line " + importantRouteLine);
		tkz = new StringTokenizer(importantRouteLine, " ");
		String subnet;
		try {
			tkz.nextToken();
			tkz.nextToken();
			subnet = tkz.nextToken();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
			throw new UnknownHostException("Not enough elements in route line " + importantRouteLine);
		}
		return new GatewayData(InetAddress.getByName(gateway), subnet);
	}
}
