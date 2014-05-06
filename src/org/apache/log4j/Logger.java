package org.apache.log4j;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.util.Log;

public class Logger implements LogConf {
	private static Logger logger = null;
	public static Logger getLogger(Class<?> cls) {
		if(logger == null) logger = new Logger();
		return logger;
	}
	
	public void info(Object msg) {
		Log.i(TAG, "Log4j: "+msg);
	}
	public void info(Object msg, Throwable t) {
		Log.i(TAG, "Log4j: "+msg, t);
	}
	
	public void error(Object msg) {
		Log.e(TAG, "Log4j: "+msg);
	}
	public void error(Object msg, Throwable t) {
		Log.e(TAG, "Log4j: "+msg, t);
	}
	public void fatal(Object msg) {
		Log.e(TAG, "Log4j (FATAL): "+msg);
	}
	public void fatal(Object msg, Throwable t) {
		Log.e(TAG, "Log4j (FATAL): "+msg, t);
	}
	public void debug(Object msg) {
		Log.d(TAG, "Log4j (FATAL): "+msg);
	}
	public void debug(Object msg, Throwable t) {
		Log.d(TAG, "Log4j (FATAL): "+msg, t);
	}
	public void warn(Object msg) {
		Log.w(TAG, "Log4j (FATAL): "+msg);
	}
	public void warn(Object msg, Throwable t) {
		Log.w(TAG, "Log4j (FATAL): "+msg, t);
	}
	public void trace(Object msg) {
		Log.i(TAG, "Log4j (trace): "+msg);
	}
	public void trace(Object msg, Throwable t) {
		Log.i(TAG, "Log4j (trace): "+msg, t);
	}
}
