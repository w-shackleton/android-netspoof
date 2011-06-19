package uk.digitalsquid.netspoofer.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class ChrootManager implements Config {
	private final Context context;
	
	public ChrootManager(Context context) {
		this.context = context;
	}
	
	/**
	 * Simple file copier.
	 * Streams are closed.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private static final void copy(InputStream in, OutputStream out) throws IOException {
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	private static final String[] FILESET = {"config", "start"};
	private static final String FILESET_DIR = "files";
	
	private File filesDir;
	
	public void createFileSet() {
		Log.v(TAG, "Copying files used for running...");
		filesDir = context.getFilesDir();
		AssetManager am = context.getAssets();
		for(String file : FILESET) {
			try {
				copy(am.open(FILESET_DIR + "/" + file), context.openFileOutput(file, Context.MODE_PRIVATE));
				
				File newFile = new File(filesDir, file);
				Process newFilePermissions = Runtime.getRuntime().exec("/system/xbin/busybox", new String[] { "chmod", "a+x", newFile.getAbsolutePath() });
				try {
					newFilePermissions.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
