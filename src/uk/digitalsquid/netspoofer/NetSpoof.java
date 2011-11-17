/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
 * Copyright (C) 2011 Will Shackleton
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.digitalsquid.netspoofer.config.ConfigChecker;
import uk.digitalsquid.netspoofer.config.FileFinder;
import uk.digitalsquid.netspoofer.config.FileInstaller;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NetSpoof extends Activity implements OnClickListener, LogConf {
	/**
	 * A dialog to tell the user to mount their SD card.
	 */
	static final int DIALOG_R_SD = 1;
	/**
	 * A dialog to tell the user to mount their SD card rw.
	 */
	static final int DIALOG_W_SD = 2;
	static final int DIALOG_ROOT = 3;
	static final int DIALOG_BB = 4;
	/**
	 * BB found, no chroot.
	 */
	static final int DIALOG_BB_2 = 6;
	static final int DIALOG_ABOUT = 5;
	
	private Button startButton, setupButton;

	@SuppressWarnings("unused")
	private SharedPreferences prefs;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		setupButton = (Button) findViewById(R.id.setupButton);
		setupButton.setOnClickListener(this);
		
		if(!ConfigChecker.checkInstalledLatest(getApplicationContext())) {
			setupButton.setTypeface(setupButton.getTypeface(), Typeface.BOLD);
		} else {
			setupButton.setTypeface(setupButton.getTypeface(), Typeface.NORMAL);
		}
		
		loadTask.execute();
		
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(InstallService.INTENT_STATUSUPDATE);
		
		registerReceiver(statusReceiver, statusFilter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(statusReceiver);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.setupButton:
				startActivity(new Intent(this, InstallStatus.class));
				break;
			case R.id.startButton:
				startActivity(new Intent(this, SpoofSelector.class));
				break;
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder;
		switch(id) {
			case DIALOG_R_SD:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please connect SD card / exit USB mode to continue.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							NetSpoof.this.finish();
						}
					});
				dialog = builder.create();
				break;
			case DIALOG_W_SD:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please set the SD Card to writable. May work without but expect problems.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) { }
					});
				dialog = builder.create();
				break;
			case DIALOG_ROOT:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please root your phone before using this application. Search the internet for instructions on how to do this for your phone.\nA custom firmware (such as CyanogenMod) is also recommended.\n" +
				"If the 'su' application is somewhere else on your phone, please specify it in the settings.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) { }
					});
				dialog = builder.create();
				break;
			case DIALOG_BB:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please install Busybox (either manually or from the Android Market) before using this application. Network Spoofer will try to run, but may be unstable as Busybox is missing.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) { }
					});
				dialog = builder.create();
			case DIALOG_BB_2:
				builder = new AlertDialog.Builder(this);
				builder.setTitle("So close..");
				builder.setMessage("You have Busybox installed, but it doesn't appear to have a required component '" + missingBBComponent + "'. Please update Busybox or try a different version of Busybox. Network Spoofer will most likely not work until you have updated Busybox")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) { }
					});
				dialog = builder.create();
				break;
		}
		return dialog;
	}
	
	private String missingBBComponent = "?";
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(intent.getIntExtra(InstallService.INTENT_EXTRA_STATUS, InstallService.STATUS_FINISHED)) {
			case InstallService.STATUS_FINISHED:
				switch(intent.getIntExtra(InstallService.INTENT_EXTRA_DLSTATE, InstallService.STATUS_DL_FAIL_DLERROR)) {
				case InstallService.STATUS_DL_SUCCESS:
					startButton.setEnabled(true);
					break;
				default:
					startButton.setEnabled(false);
					break;
				}
				break;
			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.netspoofmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.netSpoofMenuItemPrefs:
	    	startActivity(new Intent(this, Preferences.class));
	        return true;
	    case R.id.netSpoofMenuItemAbout:
	    	startActivity(new Intent(this, About.class));
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private AsyncTask<Void, Integer, Void> loadTask = new AsyncTask<Void, Integer, Void>() {
		
		@Override
		protected Void doInBackground(Void... params) {
			prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			// Install scripts & BB
			installFiles();
	
			// Check SD and files on it
			if(!ConfigChecker.getSDStatus(false)) {
				publishProgress(DIALOG_R_SD);
			} else {
				if(!ConfigChecker.getSDStatus(true)) {
					publishProgress(DIALOG_W_SD);
				}
				
				final File sd = getExternalFilesDir(null);
				File imgDir = new File(sd, "img");
				if(!imgDir.exists()) if(!imgDir.mkdir()) Log.e(TAG, "Couldn't create 'img' dir");
				
				// Find files etc.
				try {
					FileFinder.initialise(getApplicationContext());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					if(e.getMessage().equals("su")) {
						publishProgress(DIALOG_ROOT);
					} else if(e.getMessage().equals("busybox")) {
						publishProgress(DIALOG_BB);
					} else if(e.getMessage().startsWith("bb:")) {
						missingBBComponent = e.getMessage().substring(2); // 2 = end of bb:
						publishProgress(DIALOG_BB_2);
					}
					
				}
			}
		
			return null;
		}
		
		/**
		 * Shows the dialog with the given ID.
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			showDialog(values[0]);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(!ConfigChecker.checkInstalledLatest(getApplicationContext())) {
				setupButton.setTypeface(setupButton.getTypeface(), Typeface.BOLD);
			} else {
				setupButton.setTypeface(setupButton.getTypeface(), Typeface.NORMAL);
			}
			startButton.setEnabled(ConfigChecker.checkInstalled(getApplicationContext()));
			
			findViewById(R.id.loading).setVisibility(View.INVISIBLE);
		}
		
		/**
		 * Installs scripts & files into the data folder.
		 */
		private void installFiles() {
			try {
				FileInstaller fi = new FileInstaller(getBaseContext());
				
				fi.installScript("config", R.raw.config);
				fi.installScript("start", R.raw.start);
				fi.installScript("mount", R.raw.mount);
				fi.installScript("umount", R.raw.umount);
				
				fi.installScript("busybox", R.raw.busybox);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to install scripts.");
			}
		}
	};
}
