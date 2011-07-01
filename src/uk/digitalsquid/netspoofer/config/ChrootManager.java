package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.spoofs.IPRedirectSpoof;
import uk.digitalsquid.netspoofer.spoofs.SimpleScriptedSpoof;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import uk.digitalsquid.netspoofer.spoofs.SquidScriptSpoof;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.widget.Toast;

public class ChrootManager implements Config {
	private final Context context;
	private final ChrootConfig config;
	
	FileInstaller fi;
	
	public ChrootManager(Context context, ChrootConfig config) {
		this.context = context;
		this.config = config;
		
		try {
			fi = new FileInstaller(context);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void installFiles() {
		try {
			fi.installScript("config", R.raw.config);
			fi.installScript("start", R.raw.start);
			fi.installScript("mount", R.raw.mount);
			fi.installScript("umount", R.raw.umount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to install scripts.");
		}
	}
	
	/**
	 * Sets up the shell's environment variables, mounts the image, and chroots into debian.
	 * @throws IOException 
	 */
	public synchronized void start() throws IOException {
		installFiles();
		Map<String, String> env = config.getValues();
		// Setup & mount DEB.
		ProcessRunner.runProcess(env, FileFinder.SU, "-c", fi.getScriptPath("mount") + " " + fi.getScriptPath("config")); // Pass config script as arg.
	}
	
	/**
	 * Stops the chroot.
	 * @throws IOException 
	 */
	public synchronized int stop() throws IOException {
		Map<String, String> env = config.getValues();
		return ProcessRunner.runProcess(env, FileFinder.SU, "-c", fi.getScriptPath("umount") + " " + fi.getScriptPath("config"));
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
				spoofs.add(new SquidScriptSpoof(lines.get(1), lines.get(2), lines.get(0)));
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Couldn't read info for spoof " + file + ".");
			}
		}
		
		// General spoof - only arpspoof.
		spoofs.add(new SimpleScriptedSpoof(
				"Redirect traffic through phone",
				"Don't do anything to the traffic, only redirect other people's traffic through the phone. Useful in combination with 'Shark' app.",
				"spoof %s %s 0", "\n"));
		
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
	
	Process su;
	BufferedReader cout;
	BufferedReader cerr;
	OutputStreamWriter cin;
	
	public void startSpoof(SpoofData spoof) throws IOException {
		if(spoofRunning) throw new IllegalStateException("Spoof already running");
		synchronized(spoofLock) {
			spoofRunning = true;
			
			ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
					fi.getScriptPath("start") + " " + fi.getScriptPath("config") + " " + 
					spoof.getSpoof().getSpoofCmd(spoof.getVictimString(), spoof.getRouterIpString())); // Pass config script as arg.
			Map<String, String> env = pb.environment();
			env.putAll(config.getValues());
			
			env.put("WLAN", spoof.getMyIface());
			env.put("IP", spoof.getMyIp().getHostAddress());
			env.put("SUBNET", spoof.getMySubnetBaseAddressString());
			env.put("MASK", spoof.getMySubnetString());
			env.put("SHORTMASK", String.valueOf(spoof.getMySubnet()));

			su = pb.start();
			cout = new BufferedReader(new InputStreamReader(su.getInputStream()));
			cerr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
			cin  = new OutputStreamWriter(su.getOutputStream());
		}
	}
	
	/**
	 * Stops the current spoof.
	 * @return The final list of output messages
	 * @throws IOException
	 */
	public void stopSpoof(SpoofData spoof) throws IOException {
		if(!spoofRunning) return; // Don't do anything.
		synchronized(spoofLock) {
			cin.write(spoof.getSpoof().getStopCmd());
			cin.flush();
			
			try {
				su.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops the current spoof.
	 * @return The final list of output messages
	 * @throws IOException
	 */
	public ArrayList<String> finishStopSpoof() throws IOException {
		synchronized(spoofLock) {
			ArrayList<String> finalOutput = getNewSpoofOutput();
			
			try {
				cin.close();
				cout.close();
				cerr.close();
			} catch (IOException e) {
			}
			
			cin = null;
			cout = null;
			cerr = null;
			su = null;
			
			spoofRunning = false;
			
			return finalOutput;
		}
	}
	
	
	/**
	 * Checks if the process is stopped. Doesn't actually close anything, though.
	 * stopSpoof must be called with onlyClosePipes true if this returns true.
	 * @return
	 */
	public boolean checkIfStopped() {
		if(!spoofRunning) return false;
			
		if(su == null) return false;
		try {
			su.exitValue();
		} catch (IllegalThreadStateException e) {
			return false;
		}
		return true;
	}
	
	public synchronized ArrayList<String> getNewSpoofOutput() throws IOException {
		ArrayList<String> items = new ArrayList<String>();
		
		while(cerr.ready()) {
			String line = cerr.readLine();
			items.add(line);
		}
		while(cout.ready()) {
			String line = cout.readLine();
			items.add(line);
		}
		return items;
	}
}
