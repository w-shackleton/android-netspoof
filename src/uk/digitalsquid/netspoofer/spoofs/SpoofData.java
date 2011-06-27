package uk.digitalsquid.netspoofer.spoofs;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
	private int mySubnet = 24;
	private InetAddress myIp, routerIp;

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
}
