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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class IOHelpers {
	private IOHelpers() {}
	
	/**
	 * Reads a whole file from is, and closes the stream.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static final String readFileContents(InputStream is) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[256];
		for (int n; (n = is.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		is.close();
		return out.toString();
	}
	
	/**
	 * Reads a whole file into an array of lines.
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public static final List<String> readFileToLines(String filename) throws IOException {
		FileReader reader = new FileReader(filename);
		BufferedReader br = new BufferedReader(reader);
		
		List<String> lines = new ArrayList<String>();
		String line;
		while((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		reader.close();
		return lines;
	}
	
	public static final List<String> runProcessOutputToLines(List<String> args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader cout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> lines = new ArrayList<String>();
		String line;
		while((line = cout.readLine()) != null) {
			lines.add(line);
		}
		cout.close();
		return lines;
	}
	
	public static final int runProcess(List<String> args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		
		Process proc = pb.start();
		try {
			return proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static final void deleteFolder(File dir) {
		if(!dir.exists()) return;
		if(!dir.isDirectory()) {
			dir.delete();
			return;
		}
		for(String child : dir.list()) {
			File sub = new File(dir, child);
			if(sub.isDirectory()) {
				deleteFolder(sub);
				sub.delete();
			}
			else sub.delete();
		}
	}
}
