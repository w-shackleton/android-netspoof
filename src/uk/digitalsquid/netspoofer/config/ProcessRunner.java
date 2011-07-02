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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import android.util.Log;

public final class ProcessRunner implements LogConf {
	private ProcessRunner() {}
	
	public static final int runProcess(String... args) throws IOException {
		return runProcess(null, args);
	}

	public static final int runProcess(Map<String, String> env, String... args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		if(env != null) pb.environment().putAll(env);
		Process proc = pb.start();
		BufferedReader cout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader cerr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		boolean running = true;
		while(running) {
			while(cout.ready()) {
				String msg = cout.readLine();
				Log.d(TAG, "cout: " + msg);
			}
			while(cerr.ready()) {
				String msg = cerr.readLine();
				Log.d(TAG, "cerr: " + msg);
			}
			try {
				proc.exitValue();
				running = false;
				cout.close();
				cerr.close();
				return proc.exitValue();
			}
			catch (IllegalThreadStateException e) { } // Not done
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
		return 0;
	}
}
