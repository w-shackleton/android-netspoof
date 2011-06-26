package uk.digitalsquid.netspoofer.config;

import java.io.File;
import java.io.FileNotFoundException;

public final class FileFinder {
	private FileFinder() { }
	private static boolean initialised = false;
	
	public static String SU = "";
	public static String BUSYBOX = "";
	
	private static final String[] BB_PATHS = { "/system/bin/busybox", "/system/xbin/busybox", "/system/sbin/busybox", "/vendor/bin/busybox", "busybox" };
	private static final String[] SU_PATHS = { "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su", "busybox" };
	
	/**
	 * Searches for the busybox executable
	 * @return
	 */
	private static final String findBusybox() {
		for(String bb : BB_PATHS) {
			if(new File(bb).exists()) {
				return bb;
			}
		}
		return "";
	}
	
	/**
	 * Searches for the su executable
	 * @return
	 */
	private static final String findSu() {
		for(String su : SU_PATHS) {
			if(new File(su).exists()) {
				return su;
			}
		}
		return "";
	}
	
	public static final void initialise() throws FileNotFoundException {
		if(initialised) {
			return;
		}
		initialised = true;
		BUSYBOX = findBusybox();
		if(BUSYBOX.equals("")) {
			throw new FileNotFoundException("busybox");
		}
		SU = findSu();
		if(SU.equals("")) {
			throw new FileNotFoundException("su");
		}
	}
}
