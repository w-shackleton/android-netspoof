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

package uk.digitalsquid.netspoofer.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import uk.digitalsquid.netspoofer.config.Config;
import android.util.Log;

/**
 * Simple zip archive unzipper
 * @author william
 *
 */
public final class UnZip implements Config {
	private UnZip() {}
	
	private static final int BUFSIZE = 1024;
	
	public static final boolean unzipArchive(File archive, File destDir) throws IOException {
		if(!destDir.exists()) destDir.mkdir();
		
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
		
		byte buffer[] = new byte[BUFSIZE];
		
		ZipEntry entry;
		while((entry = in.getNextEntry()) != null) {
			File extractTo = new File(destDir, entry.getName());
			if(entry.isDirectory()) {
				extractTo.mkdir();
				continue;
			}
			extractTo.getParentFile().mkdirs();
			
			Log.i(TAG, "Writing file " + extractTo);
			
			OutputStream out = new BufferedOutputStream(new FileOutputStream(extractTo));
			int count;
			while((count = in.read(buffer, 0, BUFSIZE)) != -1) {
				out.write(buffer, 0, count);
			}
			out.close();
		}
		
		in.close();
		
		return true;
	}

}
