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
	
	private String iface;
	
	private final Map<String, String> values = new HashMap<String, String>();
	
	public ChrootConfig(Context context) {
		if(DEFAULTS == null) DEFAULTS = new ChrootConfig("eth0");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		iface = prefs.getString("iface", DEFAULTS.iface);
		if(iface.equals("")) iface = DEFAULTS.iface;
		// values.put("WLAN", iface); - Set in other places
	}
	
	private ChrootConfig(String iface) {
		this.iface = iface;
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
