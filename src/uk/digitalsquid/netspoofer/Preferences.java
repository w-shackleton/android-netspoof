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

package uk.digitalsquid.netspoofer;

import java.io.FileNotFoundException;

import uk.digitalsquid.netspoofer.config.FileFinder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		if(prefs.getBoolean("builtinbusybox", true)) { // If builtinBB is true
			findPreference("pathToBB").setEnabled(false);
		} else {
			findPreference("pathToBB").setEnabled(true);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if(key.equals("builtinbusybox")) {
			if(prefs.getBoolean(key, true)) { // If builtinBB is true
				findPreference("pathToBB").setEnabled(false);
			} else {
				findPreference("pathToBB").setEnabled(true);
			}
		}
		
		if(key.equals("builtinbusybox") || key.equals("pathToBB")) {
			try {
				FileFinder.loadPaths();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(getBaseContext(), "Failed to find new BusyBox path", Toast.LENGTH_LONG).show();
			}
		}
	}
}
