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
	
	public static final int inetFromByte(byte[] ip) {
		return
			((int)ip[0] << 0 ) +
			((int)ip[1] << 8 ) +
			((int)ip[2] << 16) +
			((int)ip[3] << 24);
	}
	
	public static final InetAddress reverseInetFromInt(int ip) throws UnknownHostException {
		return InetAddress.getByAddress(new byte[] {
				(byte) ((ip >>>24) & 0xFF),
				(byte) ((ip >> 16) & 0xFF),
				(byte) ((ip >> 8 ) & 0xFF),
				(byte) ((ip >> 0 ) & 0xFF),
						});
	}
	
	public static final int reverseInetFromByte(byte[] ip) {
		return
			((int)ip[0] << 24) +
			((int)ip[1] << 16) +
			((int)ip[2] << 8 ) +
			((int)ip[3] << 0 );
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
		routeArgs.add(FileFinder.BUSYBOX);
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
