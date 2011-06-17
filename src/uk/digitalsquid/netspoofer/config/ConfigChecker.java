package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.digitalsquid.netspoofer.InstallService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Environment;

public final class ConfigChecker implements Config {
	
	public static final boolean checkInstalled(Context context) {
		if(getSDStatus(false)) {
			final File sd = context.getExternalFilesDir(null);
			File debian = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			if(debian.exists()) return true;
		}
		return false;
	}
	
	public static final boolean checkInstalledLatest(Context context) {
		if(getSDStatus(false)) {
			final File sd = context.getExternalFilesDir(null);
			File version = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
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
				if(Integer.parseInt(ver) >= DEB_IMG_URL_VERSION) return true;
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
