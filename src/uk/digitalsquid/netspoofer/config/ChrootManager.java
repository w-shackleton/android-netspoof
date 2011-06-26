package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
		
		am = context.getAssets();
	}
	
	private static final String FILE_START = "start";
	private static final String FILE_CONFIG = "config";
	private static final String FILESET_DIR = "files";
	
	private final AssetManager am;
	
	Process su;
	BufferedReader cout;
	BufferedReader cerr;
	OutputStreamWriter cin;
	
	/**
	 * Reads a whole file from is, and closes the stream.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static final String readFileContents(InputStream is) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[256];
		for (int n; (n = is.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		is.close();
		return out.toString();
	}
	
	private final void flushOutputs(boolean waitForCatchup) {
		flushOutputs(true, true, false, waitForCatchup);
	}
	private final void flushOutputs(boolean toLogcat, boolean waitForCatchup) {
		flushOutputs(true, true, toLogcat, waitForCatchup);
	}
	
	/**
	 * The key here is to output something with echo, and wait for its response. This allows us to wait for everything else to finish.
	 * @param fCout
	 * @param fCerr
	 * @param toLogcat
	 * @param waitForCatchup
	 */
	private final void flushOutputs(boolean fCout, boolean fCerr, boolean toLogcat, boolean waitForCatchup) {
		String key = "FIN" + System.currentTimeMillis(); // Just a simple key to test whether finished.
		if(waitForCatchup) {
			fCout = true;
			try {
				cin.write("echo \"" + key + "\"\n");
				cin.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
		if(fCout) {
			try {
				while(waitForCatchup || cout.ready()) {
					String msg = cout.readLine();
					if(toLogcat && msg != null) {
						Log.d(TAG, "cout: " + msg);
					}
					if(waitForCatchup || msg.equals(key)) break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(fCerr) {
			try {
				while(cerr.ready()) {
					String msg = cerr.readLine();
					if(toLogcat && msg != null) {
						Log.d(TAG, "cerr: " + msg);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sets up the shell's environment variables, mounts the image, and chroots into debian.
	 * @throws IOException 
	 */
	public synchronized void start() throws IOException {
		ProcessBuilder pb = new ProcessBuilder(FileFinder.SU);
		su = pb.start();
		cout = new BufferedReader(new InputStreamReader(su.getInputStream()));
		cerr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
		cin  = new OutputStreamWriter(su.getOutputStream());
		
		// Begin setting environment variables
		for(Entry<String, String> variable : config.getValues().entrySet()) {
			cin.write(String.format("export %s=%s\n", variable.getKey(), variable.getValue()));
		}
		// Enter SU.
		// Settings loaded, load config script.
		String configFile = readFileContents(am.open(FILESET_DIR + "/" + FILE_CONFIG));
		cin.write(configFile);
		cin.write("\n");
		cin.flush();
		flushOutputs(true);
		
		// Mount DEB.
		cin.write("deb-mount\n");
		cin.flush();
		flushOutputs(true, true);
		
		configFile = readFileContents(am.open(FILESET_DIR + "/" + FILE_START));
		cin.write(configFile);
		cin.write("\n");
		cin.flush();
		flushOutputs(true, true);
	}
	
	/**
	 * Stops the chroot.
	 * @throws IOException 
	 */
	public synchronized int stop() {
		try {
			// TODO: Make this better?
			cin.write("exit\n"); // Exit chroot
			cin.write("deb-umount\n"); // Umount
			cin.write("exit\n"); // Exit
			cin.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "Remaining messages from command line:");
		flushOutputs(true, true);
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
