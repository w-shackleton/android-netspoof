package uk.digitalsquid.netspoofer.config;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A class to hold the configuration; It is pulled from the shared preferences.
 * @author william
 *
 */
public class ChrootConfig {
	static ChrootConfig DEFAULTS = null;
	
	private final String loopdev;
	private final int loopnum;
	
	private final String debianMount;
	private final String debianImage;
	
	private final Map<String, String> values = new HashMap<String, String>();
	
	public ChrootConfig(Context context) {
		if(DEFAULTS == null) DEFAULTS = new ChrootConfig("/dev/block/loop2000", 2000, "/data/local/mnt", context.getExternalFilesDir(null) + "/" + Config.DEB_IMG);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loopdev = prefs.getString("loopdev", DEFAULTS.loopdev);
		values.put("LOOPDEV", loopdev);
		loopnum = prefs.getInt("loopnum", DEFAULTS.loopnum);
		values.put("LOOPNUM", "" + loopnum);
		debianMount = prefs.getString("debianMount", DEFAULTS.debianMount);
		values.put("DEB", debianMount);
		debianImage = prefs.getString("debianImage", DEFAULTS.debianImage);
		values.put("DEBIMG", debianImage);
	}
	
	private ChrootConfig(String loopdev, int loopnum, String debianMount, String debianImage) {
		this.loopdev = loopdev;
		this.loopnum = loopnum;
		this.debianMount = debianMount;
		this.debianImage = debianImage;
		values.put("LOOPDEV", loopdev);
		values.put("LOOPNUM", "" + loopnum);
		values.put("DEB", debianMount);
		values.put("DEBIMG", debianImage);
	}

	public String getLoopdev() {
		return loopdev;
	}

	public int getLoopNum() {
		return loopnum;
	}

	public String getDebianMount() {
		return debianMount;
	}

	public String getDebianImage() {
		return debianImage;
	}

	public Map<String, String> getValues() {
		return values;
	}
}
