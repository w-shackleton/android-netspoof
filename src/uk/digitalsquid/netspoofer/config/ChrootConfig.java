/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
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
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * A class to hold the configuration; It is pulled from the shared preferences.
 * @author william
 *
 */
public final class ChrootConfig implements Config {
	static ChrootConfig DEFAULTS = null;
	
	private String loopdev;
	private int loopnum;
	
	private String debianMount;
	private String debianImage;
	
	private String iface;
	
	private final Map<String, String> values = new HashMap<String, String>();
	
	public ChrootConfig(Context context) {
		if(DEFAULTS == null) DEFAULTS = new ChrootConfig("/dev/block/loop250", 250, "/data/local/mnt", getDebImgPath(context), "eth0");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loopdev = prefs.getString("loopdev", DEFAULTS.loopdev);
		if(loopdev.equals("")) loopdev = DEFAULTS.loopdev;
		values.put("LOOPDEV", loopdev);
		
		File folder = context.getExternalFilesDir(null);
		Log.i(TAG, "Data folder is " + folder + ", absolute path is " + folder.getAbsolutePath());
		
		try {
			// loopnum = prefs.getInt("loopnum", DEFAULTS.loopnum);
			loopnum = Integer.parseInt(prefs.getString("loopnum", "" + DEFAULTS.loopnum));
		} catch (NumberFormatException e) {
			loopnum = DEFAULTS.loopnum;
			Toast.makeText(context, "Invalid loop device number, using default", Toast.LENGTH_SHORT).show();
		} catch (ClassCastException e) {
			loopnum = DEFAULTS.loopnum;
			Toast.makeText(context, "Invalid loop device number, using default", Toast.LENGTH_SHORT).show();
		}
		if(prefs.getString("loopnum", "").equals("")) loopnum = DEFAULTS.loopnum;
		values.put("LOOPNUM", "" + loopnum);
		
		debianMount = prefs.getString("debianMount", DEFAULTS.debianMount);
		if(debianMount.equals("")) debianMount = DEFAULTS.debianMount;
		values.put("DEB", debianMount);
		
		debianImage = prefs.getString("debianImage", DEFAULTS.debianImage);
		if(debianImage.equals("")) debianImage = DEFAULTS.debianImage;
		values.put("DEBIMG", debianImage);
		
		iface = prefs.getString("iface", DEFAULTS.iface);
		if(iface.equals("")) iface = DEFAULTS.iface;
		// values.put("WLAN", iface); - Set in other places
	}
	
	private static String getDebImgPath(Context context) {
		// Android 4.3 goes all ???? with root trying to access the path given
		// by SDK
		if(Build.VERSION.SDK_INT >= 18) { // 4.3
			// TODO: Find out what exactly /storage/emulated/0 is and what this means
			// for the root user
			return "/sdcard/Android/data/uk.digitalsquid.netspoofer/files/" + Config.DEB_IMG;
		}
		return context.getExternalFilesDir(null).getAbsolutePath() + "/" + Config.DEB_IMG;
	}
	
	private ChrootConfig(String loopdev, int loopnum, String debianMount, String debianImage, String iface) {
		this.loopdev = loopdev;
		this.loopnum = loopnum;
		this.debianMount = debianMount;
		this.debianImage = debianImage;
		this.iface = iface;
		values.put("LOOPDEV", loopdev);
		values.put("LOOPNUM", "" + loopnum);
		values.put("DEB", debianMount);
		values.put("DEBIMG", debianImage);
		// values.put("WLAN", iface); - Set in other places
	}

	/**
	 * Gets the location of the loop device
	 * @return
	 */
	public String getLoopdev() {
		return loopdev;
	}

	/**
	 * Gets the loop device number
	 * @return
	 */
	public int getLoopNum() {
		return loopnum;
	}

	/**
	 * Gets the location where debian is mounted
	 * @return
	 */
	public String getDebianMount() {
		return debianMount;
	}

	/**
	 * Gets the location of the debian image on the SD card
	 * @return
	 */
	public String getDebianImage() {
		return debianImage;
	}
	
	/**
	 * Adds busybox to the values, as found in FileFinder.
	 */
	private void addBBToValues() {
		if(!FileFinder.BUSYBOX.equals("")) // Leave undefined. This will cause the shell scripts to use the system utils instead, with dragons ahead.
			values.put("BB", FileFinder.BUSYBOX);
	}

	public Map<String, String> getValues() {
		addBBToValues();
		return values;
	}

	public String[] getExecValues() {
		addBBToValues();
		String[] vals = new String[values.size()];
		
		int i = 0;
		for(String key : values.keySet()) {
			vals[i++] = String.format("%s=%s", key, values.get(key));
		}
		
		return vals;
	}
}
