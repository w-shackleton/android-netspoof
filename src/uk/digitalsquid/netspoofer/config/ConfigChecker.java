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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public final class ConfigChecker implements Config {
	
	public static final boolean checkInstalled(Context context) {
		if(getSDStatus(false)) {
			final File sd = context.getExternalFilesDir(null);
			if(sd == null) return false;
			File debian = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			Log.i(TAG, "Checking if file " + debian.getAbsolutePath() + " exists");
			if(debian.exists()) return true;
		}
		return false;
	}
	
	public static final int getVersionNumber(Context context) {
		if(getSDStatus(false)) {
			final File sd = context.getExternalFilesDir(null);
			if(sd == null) return -1;
			File version = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			Log.i(TAG, "Getting version number from file");
			String ver;
			try {
				FileInputStream verReader = new FileInputStream(version);
				BufferedReader reader = new BufferedReader(new InputStreamReader(verReader));
				ver = reader.readLine();
				reader.close();
				verReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
			if(ver != null) {
				try {
					return Integer.parseInt(ver);
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		return -1;
	}
	
	public static final boolean checkInstalledLatest(Context context) {
		if(getSDStatus(false)) {
			final File sd = context.getExternalFilesDir(null);
			if(sd == null) return false;
			File version = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			Log.i(TAG, "Checking if file " + version.getAbsolutePath() + " is latest");
			String ver;
			try {
				FileInputStream verReader = new FileInputStream(version);
				BufferedReader reader = new BufferedReader(new InputStreamReader(verReader));
				ver = reader.readLine();
				reader.close();
				verReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			if(ver != null) {
				try {
					if(Integer.parseInt(ver) >= DEB_IMG_URL_VERSION) return true;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}

	public static final boolean getSDStatus(final boolean checkWritable) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
			return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return !checkWritable;
		} else {
			return false;
		}
	}
}
