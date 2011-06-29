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
