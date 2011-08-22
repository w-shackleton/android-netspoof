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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A class to hold the configuration; It is pulled from the shared preferences.
 * @author william
 *
 */
public final class ChrootConfig {
	static ChrootConfig DEFAULTS = null;
	
	private String loopdev;
	private int loopnum;
	
	private String debianMount;
	private String debianImage;
	
	private String iface;
	
	private final Map<String, String> values = new HashMap<String, String>();
	
	public ChrootConfig(Context context) {
		if(DEFAULTS == null) DEFAULTS = new ChrootConfig("/dev/block/loop2000", 250, "/data/local/mnt", context.getExternalFilesDir(null) + "/" + Config.DEB_IMG, "eth0");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loopdev = prefs.getString("loopdev", DEFAULTS.loopdev);
		if(loopdev.equals("")) loopdev = DEFAULTS.loopdev;
		values.put("LOOPDEV", loopdev);
		
		try {
			loopnum = prefs.getInt("loopnum", DEFAULTS.loopnum);
		} catch (NumberFormatException e) {
			loopnum = DEFAULTS.loopnum;
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

	public String getLoopdev() {
		return loopdev;
	}

	public int getLoopNum() {
		return loopnum;
	}

	public String getDebianMount() {
		return debianMount;
	}

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
