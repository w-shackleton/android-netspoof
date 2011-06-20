package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class ChrootManager implements Config {
	private final Context context;
	private final ChrootConfig config;
	
	public ChrootManager(Context context, ChrootConfig config) {
		this.context = context;
		this.config = config;
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
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
		     Log.i(TAG, String.format("%s=%s%n", envName, env.get(envName)));
		}
		
		filesDir = context.getFilesDir();
		AssetManager am = context.getAssets();
		for(String file : FILESET) {
			try {
				copy(am.open(FILESET_DIR + "/" + file), context.openFileOutput(file, Context.MODE_PRIVATE));
				
				File newFile = new File(filesDir, file);
				Process newFilePermissions = Runtime.getRuntime().exec("busybox", new String[] { "chmod", "a+x", newFile.getAbsolutePath() });
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
	
	Process su;
	BufferedReader cout;
	BufferedReader cerr;
	OutputStreamWriter cin;
	
	/**
	 * Sets up the shell's environment variables, mounts the image, and chroots into debian.
	 * @throws IOException 
	 */
	public synchronized void start() throws IOException {
		su = Runtime.getRuntime().exec(FileFinder.SU);
		cout = new BufferedReader(new InputStreamReader(su.getInputStream()));
		cerr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
		cin  = new OutputStreamWriter(su.getOutputStream());
		
		// Begin setting environment variables
		for(Entry<String, String> variable : config.getValues().entrySet()) {
			cin.write(String.format("export %s=%s", variable.getKey(), variable.getValue()));
		}
	}
	
	/**
	 * Stops the chroot.
	 * @throws IOException 
	 */
	public synchronized int stop() {
		try {
			cin.write("exit\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { cin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { cout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { cerr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Give a bit of time to close naturally.
		try {
			return su.waitFor();
		} catch (InterruptedException e) { }
		return 1;
	}
}
