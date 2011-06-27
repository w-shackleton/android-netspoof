package uk.digitalsquid.netspoofer.config;

import java.io.IOException;
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
				(byte) ((ip >> 24) & 0xFF),
						});
	}
	
	public static final InetAddress getDefaultGateway(NetworkInterface iface) throws UnknownHostException {
		if(iface == null) throw new IllegalArgumentException("iface is null");
		String ifacename = iface.getDisplayName();
		List<String> routeArgs = new ArrayList<String>();
		routeArgs.add(FileFinder.BUSYBOX);
		routeArgs.add("route");
		routeArgs.add("-n");
		
		String importantRouteLine = "";
		try {
			List<String> routeTable = IOHelpers.runProcessOutputToLines(routeArgs);
			for(String line : routeTable) {
				if(line.contains(ifacename)) { // Important info
					importantRouteLine = line;
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
		return InetAddress.getByName(gateway);
	}
}
