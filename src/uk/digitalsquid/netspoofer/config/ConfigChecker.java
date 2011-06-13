package uk.digitalsquid.netspoofer.config;

import uk.digitalsquid.netspoofer.InstallService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Environment;

public final class ConfigChecker implements Config {
	
	public static final boolean checkInstalled(Context context) {
		if(getSDStatus(false)) {
			
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
	
	private static ActivityManager am;
	public static final boolean isInstallServiceRunning(Context context) {
		if(am == null) 
		    am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
	    	if (InstallService.class.getName().equals(service.service.getClassName())) {
	            return true;
	    	}
	    }
	    return false;
	}
}
