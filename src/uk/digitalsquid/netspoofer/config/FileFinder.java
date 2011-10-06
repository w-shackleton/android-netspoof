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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public final class FileFinder implements LogConf {
	private FileFinder() { }
	private static boolean initialised = false;
	
	public static String SU = "";
	public static String BUSYBOX = "";
	
	private static final String[] BB_PATHS = { "/system/bin/busybox", "/system/xbin/busybox", "/system/sbin/busybox", "/vendor/bin/busybox", "busybox" };
	private static final String[] SU_PATHS = { "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su", "su" };
	
	/**
	 * Searches for the busybox executable
	 * @return
	 */
	private static final String findBusybox(SharedPreferences prefs) {
		if(prefs != null) {
			String customPath = prefs.getString("pathToBB", "");
			if(!customPath.equals("") && new File(customPath).exists()) return customPath;
		}
		for(String bb : BB_PATHS) {
			if(new File(bb).exists()) {
				return bb;
			}
		}
		return "";
	}
	
	/**
	 * Searches for the su executable
	 * @return
	 */
	private static final String findSu(SharedPreferences prefs) {
		if(prefs != null) {
			String customPath = prefs.getString("pathToSu", "");
			if(!customPath.equals("") && new File(customPath).exists()) return customPath;
		}
		for(String su : SU_PATHS) {
			if(new File(su).exists()) {
				return su;
			}
		}
		return "";
	}
	
	public static final void initialise(Context context) throws FileNotFoundException {
		if(initialised) {
			return;
		}
		SharedPreferences prefs = null;
		if(context != null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		initialised = true;
		BUSYBOX = findBusybox(prefs);
		if(BUSYBOX.equals("")) {
			throw new FileNotFoundException("busybox");
		}
		SU = findSu(prefs);
		if(SU.equals("")) {
			throw new FileNotFoundException("su");
		}
		
		checkBBInstalledFunctions();
	}
	
	/**
	 * Checks that the necessary BB commands are available
	 * @throws FileNotFoundException 
	 */
	static final void checkBBInstalledFunctions() throws FileNotFoundException {
		List<String> result = new LinkedList<String>();
		try {
			ProcessRunner.runProcess(null, result, BUSYBOX);
		} catch (IOException e) {
			Log.e(TAG, "Failed to check BB programs, probably as BB doesn't exist?");
			e.printStackTrace();
		}
		boolean chrootFound = false;
		for(String line : result) {
			if(line.contains("chroot")) {
				chrootFound = true;
			}
		}
		if(!chrootFound) throw new FileNotFoundException("chroot");
	}
	
	/**
	 * Initialise that doesn't search custom paths. Not recommended.
	 * @throws FileNotFoundException
	 */
	public static final void initialise() throws FileNotFoundException {
		if(initialised) {
			return;
		}
		initialised = true;
		BUSYBOX = findBusybox(null);
		if(BUSYBOX.equals("")) {
			throw new FileNotFoundException("busybox");
		}
		SU = findSu(null);
		if(SU.equals("")) {
			throw new FileNotFoundException("su");
		}
	}
}
