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
package uk.digitalsquid.netspoofer.entities;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * A potential victim. Contains information about IP, name, vendor and so on.
 */
public class Victim implements Comparable<Victim>, Serializable {
    private static final long serialVersionUID = -8815727249378333391L;
    private final InetAddress ip;
    private String mac;
    private String vendor;

    private String hostname;

    public Victim(InetAddress ip) {
        this.ip = ip;
    }

    public InetAddress getIp() {
        return ip;
    }

    public String getIpString() {
        return ip.getHostAddress();
    }

    @Override
    public int compareTo(@NonNull Victim another) {
        byte[] me = ip.getAddress();
        byte[] other = another.getIp().getAddress();
        for(int i = 0; i < me.length && i < other.length; i++) {
            if(me[i] != other[i]) {
                return me[i] - other[i];
            }
        }
        return 0;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
