package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import uk.digitalsquid.netspoofer.spoofs.IPRedirectSpoof;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import uk.digitalsquid.netspoofer.spoofs.SquidScriptSpoof;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

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
					if(waitForCatchup && msg.equals(key)) break;
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
		String configFile = IOHelpers.readFileContents(am.open(FILESET_DIR + "/" + FILE_CONFIG));
		cin.write(configFile);
		cin.write("\n");
		cin.flush();
		flushOutputs(true);
		
		// Mount DEB.
		cin.write("deb-mount\n");
		cin.flush();
		flushOutputs(true, true);
		
		configFile = IOHelpers.readFileContents(am.open(FILESET_DIR + "/" + FILE_START));
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
	
	public ArrayList<Spoof> getSpoofList() {
		Log.i(TAG, "Searching for spoofs to use...");
		final ArrayList<Spoof> spoofs = new ArrayList<Spoof>();
		
		// 1. Scan squid rewriters
		File rewriteDir = new File(config.getDebianMount() + "/rewriters");
		for(String file : rewriteDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".txt");
			}
		})) {
			Log.v(TAG, "Spoof: " + file);
			try {
				List<String> lines = IOHelpers.readFileToLines(rewriteDir.getAbsolutePath() + "/" + file);
				if(lines.size() < 2) {
					Log.e(TAG, "Malformed description file for " + file + ".");
					continue;
				}
				// FIXME: Add proper title to objects here!
				spoofs.add(new SquidScriptSpoof(lines.get(0), lines.get(1), lines.get(0)));
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Couldn't read info for spoof " + file + ".");
			}
		}
		
		// IP Redirect spoofs.
		try {
			spoofs.add(new IPRedirectSpoof("All sites -> kittenwar", "Redirect all websites to kittenwar.com", IPRedirectSpoof.KITTENWAR));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Toast.makeText(context, "Couldn't load kittenwar webaddress", Toast.LENGTH_LONG).show();
		}
		spoofs.add(new IPRedirectSpoof("All sites -> other website", "Redirect all websites to another website"));
		return spoofs;
	}
	
	public boolean isSpoofRunning() {
		return spoofRunning;
	}

	private boolean spoofRunning = false;
	private Object spoofLock = new Object();
	
	public void startSpoof(SpoofData spoof) throws IOException {
		if(spoofRunning) throw new IllegalStateException("Spoof already running");
		synchronized(spoofLock) {
			spoofRunning = true;
			cin.write(String.format("export WLAN=%s", spoof.getMyIface()));
			cin.write(String.format("export IP=%s", spoof.getMyIp().getHostAddress()));
			cin.write(String.format("export SUBNET=%s", spoof.getMySubnetBaseAddressString()));
			cin.write(String.format("export MASK=%s", spoof.getMySubnetString()));
			cin.write(String.format("export SHORTMASK=%s", spoof.getMySubnet()));
			
			cin.write(spoof.getSpoof().getSpoofCmd(spoof.getVictim().getIpString(), spoof.getRouterIpString())); // Should now be started.
		}
	}
	
	/**
	 * Stops the current spoof.
	 * @return The final list of output messages
	 * @throws IOException
	 */
	public ArrayList<String> stopSpoof(SpoofData spoof) throws IOException {
		if(!spoofRunning) return new ArrayList<String>(); // Don't do anything.
		synchronized(spoofLock) {
			cin.write(spoof.getSpoof().getStopCmd());
			spoofRunning = false;
			
			return getNewSpoofOutput(true);
		}
	}
	
	public ArrayList<String> getNewSpoofOutput() throws IOException {
		return getNewSpoofOutput(false);
	}
	
	private synchronized ArrayList<String> getNewSpoofOutput(boolean waitForFinishLine) throws IOException {
		cin.write("echo \"SPOOF FINISHED\"\n");
		ArrayList<String> items = new ArrayList<String>();
		
		while(cerr.ready()) {
			String line = cerr.readLine();
			items.add(line);
		}
		while(waitForFinishLine || cout.ready()) {
			String line = cout.readLine();
			items.add(line);
			if(waitForFinishLine && line.contains("SPOOF FINISHED")) {
				break;
			}
		}
		return items;
	}
}
