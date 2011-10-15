package uk.digitalsquid.netspoofer;

/**
 * <p>
 * Application JNI interfaces. Not used <strong>at the moment</strong> for any purpose, but could be in the future.
 * </p>
 * <p>
 * Also used to force Android Market to filter by CPUs that this application actually supports, currently armel.
 * </p>
 * 
 * @author william
 *
 */
public class JNI {
	static {
		System.loadLibrary("netspooflib");
	}
	
	/**
	 * Chmod glibc function setting a file to 0755
	 * @param filename
	 * @param permissions
	 * @return
	 */
	public static native int setExecutable(String filename);
}
