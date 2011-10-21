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

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.digitalsquid.netspoofer.spoofs.CustomGSearchSpoof;
import uk.digitalsquid.netspoofer.spoofs.CustomImageChange;
import uk.digitalsquid.netspoofer.spoofs.IPRedirectSpoof;
import uk.digitalsquid.netspoofer.spoofs.SimpleScriptedSpoof;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import uk.digitalsquid.netspoofer.spoofs.SquidScriptSpoof;
import uk.digitalsquid.netspoofer.spoofs.VideoChange;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ChrootManager implements Config {
	private final Context context;
	private final ChrootConfig config;
	
	public ChrootManager(Context context, ChrootConfig config) {
		this.context = context;
		this.config = config;
	}
	
	/**
	 * Sets up the shell's environment variables, mounts the image, and chroots into debian.
	 * @return true of mount completed successfully.
	 * @throws IOException 
	 */
	public synchronized boolean start() throws IOException {
		Map<String, String> env = config.getValues();
		// Setup & mount DEB.
		FileFinder.initialise(context.getApplicationContext()); // In case of weird android instancing
		ProcessRunner.runProcess(env, FileFinder.SU, "-c", FileInstaller.getScriptPath(context, "mount") + " " + FileInstaller.getScriptPath(context, "config")); // Pass config script as arg.
		
		try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
		return new File(config.getDebianMount() + "/rewriters").exists();
	}
	
	/**
	 * Stops the chroot.
	 * @throws IOException 
	 */
	public synchronized int stop() throws IOException {
		Map<String, String> env = config.getValues();
		FileFinder.initialise(context.getApplicationContext()); // In case of weird android instancing
		return ProcessRunner.runProcess(env, FileFinder.SU, "-c", FileInstaller.getScriptPath(context, "umount") + " " + FileInstaller.getScriptPath(context, "config"));
	}
	
	public ArrayList<Spoof> getSpoofList() {
		Log.i(TAG, "Searching for spoofs to use...");
		final ArrayList<Spoof> spoofs = new ArrayList<Spoof>();
		
		// 1. Scan squid rewriters
		File rewriteDir = new File(config.getDebianMount() + "/rewriters");
		if(rewriteDir.exists()) {
			for(String file : rewriteDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".txt");
				}
			})) {
				Log.v(TAG, "Spoof: " + file);
				try {
					List<String> lines = IOHelpers.readFileToLines(rewriteDir.getAbsolutePath() + "/" + file);
					if(lines.size() < 2) {
						Log.e(TAG, "Malformed description file for " + file + ".");
						continue;
					}
					spoofs.add(new SquidScriptSpoof(lines.get(1), lines.get(2), lines.get(0)));
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Couldn't read info for spoof " + file + ".");
				}
			}
		}
		
		spoofs.add(new CustomGSearchSpoof());
		spoofs.add(new CustomImageChange());
		spoofs.add(new VideoChange());
		
		// General spoof - only arpspoof.
		spoofs.add(new SimpleScriptedSpoof(
				"Redirect traffic through phone",
				"Don't do anything to the traffic, only redirect other people's traffic through the phone. Useful in combination with 'Shark' app.",
				"spoof %s %s 0", "\n"));
		
		// IP Redirect spoofs.
		spoofs.add(new IPRedirectSpoof("All sites -> other website", "Redirect all websites to another website"));
		
		Collections.sort(spoofs);
		
		return spoofs;
	}
	
	public boolean isSpoofRunning() {
		return spoofRunning;
	}

	private boolean spoofRunning = false;
	private Object spoofLock = new Object();
	
	Process su;
	BufferedReader cout;
	BufferedReader cerr;
	OutputStreamWriter cin;
	
	public void startSpoof(SpoofData spoof) throws IOException {
		if(spoofRunning) throw new IllegalStateException("Spoof already running");
		FileFinder.initialise(context.getApplicationContext()); // In case of weird android instancing
		synchronized(spoofLock) {
			spoofRunning = true;
			
			if(Build.VERSION.SDK_INT >= 9) { // 2.2 doesn't like this method
				ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
						FileInstaller.getScriptPath(context, "start") + " " + FileInstaller.getScriptPath(context, "config") + " " + 
						spoof.getSpoof().getSpoofCmd(spoof.getVictimString(), spoof.getRouterIpString())); // Pass config script as arg.
				Map<String, String> env = pb.environment();
				env.putAll(config.getValues());
				env.putAll(spoof.getSpoof().getCustomEnv());
				
				if(!spoof.isRunningPassively()) {
					env.put("WLAN", spoof.getMyIface());
					env.put("IP", spoof.getMyIp().getHostAddress());
					env.put("SUBNET", spoof.getMySubnetBaseAddressString());
					env.put("MASK", spoof.getMySubnetString());
					env.put("SHORTMASK", String.valueOf(spoof.getMySubnet()));
				}
	
				su = pb.start();
			} else {
				Map<String, String> systemEnv = System.getenv(); // We also must include this
				Map<String, String> combinedEnv = new HashMap<String, String>();
				
				combinedEnv.putAll(systemEnv);
				combinedEnv.putAll(config.getValues());
				combinedEnv.putAll(spoof.getSpoof().getCustomEnv());
				
				if(!spoof.isRunningPassively()) {
					combinedEnv.put("WLAN", spoof.getMyIface());
					combinedEnv.put("IP", spoof.getMyIp().getHostAddress());
					combinedEnv.put("SUBNET", spoof.getMySubnetBaseAddressString());
					combinedEnv.put("MASK", spoof.getMySubnetString());
					combinedEnv.put("SHORTMASK", String.valueOf(spoof.getMySubnet()));
				}
				
				String[] envArray = new String[combinedEnv.size()];
				int i = 0;
				for(String key : combinedEnv.keySet()) {
					envArray[i++] = String.format("%s=%s", key, combinedEnv.get(key));
				}
				su = Runtime.getRuntime().exec(new String [] {
						FileFinder.SU, "-c",
						FileInstaller.getScriptPath(context, "start") + " " + FileInstaller.getScriptPath(context, "config") + " " + 
						spoof.getSpoof().getSpoofCmd(spoof.getVictimString(), spoof.getRouterIpString()), // Pass config script as arg.
				}, envArray);
			}
			cout = new BufferedReader(new InputStreamReader(su.getInputStream()));
			cerr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
			cin  = new OutputStreamWriter(su.getOutputStream());
		}
	}
	
	/**
	 * Stops the current spoof.
	 * @return The final list of output messages
	 * @throws IOException
	 */
	public void stopSpoof(SpoofData spoof) throws IOException {
		if(!spoofRunning) return; // Don't do anything.
		synchronized(spoofLock) {
			cin.write(spoof.getSpoof().getStopCmd());
			cin.flush();
			
			try {
				su.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops the current spoof.
	 * @return The final list of output messages
	 * @throws IOException
	 */
	public ArrayList<String> finishStopSpoof() throws IOException {
		synchronized(spoofLock) {
			ArrayList<String> finalOutput = getNewSpoofOutput();
			
			try {
				cin.close();
				cout.close();
				cerr.close();
			} catch (IOException e) {
			}
			
			cin = null;
			cout = null;
			cerr = null;
			su = null;
			
			spoofRunning = false;
			
			return finalOutput;
		}
	}
	
	
	/**
	 * Checks if the process is stopped. Doesn't actually close anything, though.
	 * stopSpoof must be called with onlyClosePipes true if this returns true.
	 * @return
	 */
	public boolean checkIfStopped() {
		if(!spoofRunning) return false;
			
		if(su == null) return false;
		try {
			su.exitValue();
		} catch (IllegalThreadStateException e) {
			return false;
		}
		return true;
	}
	
	public synchronized ArrayList<String> getNewSpoofOutput() throws IOException {
		ArrayList<String> items = new ArrayList<String>();
		
		while(cerr.ready()) {
			String line = cerr.readLine();
			items.add(line);
		}
		while(cout.ready()) {
			String line = cout.readLine();
			items.add(line);
		}
		return items;
	}
}
