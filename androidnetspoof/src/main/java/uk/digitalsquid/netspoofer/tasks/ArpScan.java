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

package uk.digitalsquid.netspoofer.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.StringTokenizer;

import uk.digitalsquid.netspoofer.config.FileFinder;
import uk.digitalsquid.netspoofer.config.FileInstaller;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.entities.Range;
import uk.digitalsquid.netspoofer.entities.Victim;

/**
 * Scans the local subnet using ICMP ping and ARP discovery
 */
public abstract class ArpScan extends AsyncTask<Range<InetAddress>, Victim, Void> implements LogConf {

    private final Context context;

    public ArpScan(Context context) {
        this.context = context;
    }

    protected Void doInBackground(Range<InetAddress>... range) {
        final InetAddress start = range[0].min;
        final InetAddress end = range[0].max;

        // Run arp-scan
        try {
            ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
                    String.format("%s -Nxg %s-%s", FileInstaller.getScriptPath(context, "arp-scan"),
                            start.getHostAddress(),
                            end.getHostAddress()));
            Process proc = pb.start();

            BufferedReader cout = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while ((line = cout.readLine()) != null) {
                StringTokenizer tkz = new StringTokenizer(line, "\t");
                if (!tkz.hasMoreTokens()) {
                    continue;
                }
                String ip = tkz.nextToken();
                if (!tkz.hasMoreTokens()) {
                    continue;
                }
                String mac = tkz.nextToken();
                if (!tkz.hasMoreTokens()) {
                    continue;
                }
                String vendor = tkz.nextToken();

                Victim victim = new Victim(InetAddress.getByName(ip));
                victim.setMac(mac);
                victim.setVendor(vendor);

                publishProgress(victim);
            }
        } catch (IOException e) {
            Log.w(TAG, "arp-scan failed", e);
        }
        return null;
    }
}
