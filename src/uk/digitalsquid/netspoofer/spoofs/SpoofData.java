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

package uk.digitalsquid.netspoofer.spoofs;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import uk.digitalsquid.netspoofer.VictimSelector.Victim;
import uk.digitalsquid.netspoofer.config.NetHelpers;

public class SpoofData implements Serializable {
	private static final long serialVersionUID = -690081983763949966L;
	private static final String[] SUBNETS = {
		"0.0.0.0", // This may seem like a stupid way to do this, but I can't be bothered to write a proper subnet decoder which still uses Android API8.
		"128.0.0.0", // Vim to the rescue!
		"192.0.0.0",
		"224.0.0.0",
		"240.0.0.0",
		"248.0.0.0",
		"252.0.0.0",
		"254.0.0.0",
		"255.0.0.0",
		"255.128.0.0",
		"255.192.0.0",
		"255.224.0.0",
		"255.240.0.0",
		"255.248.0.0",
		"255.252.0.0",
		"255.254.0.0",
		"255.255.0.0",
		"255.255.128.0",
		"255.255.192.0",
		"255.255.224.0",
		"255.255.240.0",
		"255.255.248.0",
		"255.255.252.0",
		"255.255.254.0",
		"255.255.255.0",
		"255.255.255.128",
		"255.255.255.192",
		"255.255.255.224",
		"255.255.255.240",
		"255.255.255.248",
		"255.255.255.252",
		"255.255.255.254",
		"255.255.255.255",
	};
	private final Spoof spoof;
	
	private String myIf;
	/**
	 * NOTE: This isn't the full address, but rather the (CIDL?) notation of /24 etc.
	 */
	private int mySubnet = 24;
	private InetAddress myIp, routerIp;
	
	private Victim victim;
	
	private boolean runningPassively = false;

	public SpoofData(Spoof spoof, boolean runningPassively) {
		if(!runningPassively) throw new IllegalStateException("Please use the other constructor");
		this.spoof = spoof;
		this.setRunningPassively(true);
	}

	public SpoofData(Spoof spoof, String myIp, String mySubnet, String myIf, String routerIp) throws UnknownHostException {
		this.spoof = spoof;
		this.myIp = InetAddress.getByName(myIp);
		for(int i = 0; i < SUBNETS.length; i++) {
			if(mySubnet.equals(SUBNETS[i])) this.mySubnet = i;
		}
		this.myIf = myIf;
		this.routerIp = InetAddress.getByName(routerIp);
	}

	public Spoof getSpoof() {
		return spoof;
	}
	public InetAddress getMyIp() {
		return myIp;
	}
	public int getMyIpInt() {
		return NetHelpers.inetFromByte(myIp.getAddress());
	}
	public int getMyIpReverseInt() {
		return NetHelpers.reverseInetFromByte(myIp.getAddress());
	}
	public String getMyIface() {
		return myIf;
	}
	public InetAddress getRouterIp() {
		return routerIp;
	}
	public String getRouterIpString() {
		if(routerIp == null) return "none";
		return routerIp.getHostAddress();
	}
	public int getRouterIpInt() {
		return NetHelpers.inetFromByte(routerIp.getAddress());
	}
	public int getMySubnet() {
		return mySubnet;
	}
	public byte[] getMySubnetBytes() {
		int mask = 0xffffffff << (32 - mySubnet);
		return new byte[]{ 
	            (byte)(mask >>> 24), (byte)(mask >> 16 & 0xff), (byte)(mask >> 8 & 0xff), (byte)(mask & 0xff) };
	}
	public int getMySubnetReverseInt() {
		return 0xffffffff << (32 - mySubnet);
	}
	public String getMySubnetString() {
		return SUBNETS[mySubnet];
	}
	
	public int getMySubnetBaseAddressReverseInt() {
		int ip = getMyIpReverseInt();
		return ip & getMySubnetReverseInt(); // Bottom possble IP
	}
	
	public String getMySubnetBaseAddressString() {
		int baseIp = getMySubnetBaseAddressReverseInt();
		try {
			return NetHelpers.reverseInetFromInt(baseIp).getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setVictim(Victim victim) {
		this.victim = victim;
	}

	public Victim getVictim() {
		return victim;
	}
	
	public String getVictimString() {
		if(isRunningPassively()) return "none";
		if(isEveryoneVictim()) return "all";
		return victim.getIpString();
	}

	public boolean isEveryoneVictim() {
		return victim == null;
	}

	public void setRunningPassively(boolean runningPassively) {
		this.runningPassively = runningPassively;
	}

	public boolean isRunningPassively() {
		return runningPassively;
	}
}
