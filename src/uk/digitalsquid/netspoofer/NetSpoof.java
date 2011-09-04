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

import uk.digitalsquid.netspoofer.config.ConfigChecker;
import uk.digitalsquid.netspoofer.config.FileFinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class NetSpoof extends Activity implements OnClickListener {
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
	static final int DIALOG_ABOUT = 5;
	
	private Button startButton, setupButton;

	@SuppressWarnings("unused")
	private SharedPreferences prefs;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if(ConfigChecker.checkInstalled(getApplicationContext())) findViewById(R.id.startButton).setEnabled(true);
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		setupButton = (Button) findViewById(R.id.setupButton);
		setupButton.setOnClickListener(this);
		
		if(!ConfigChecker.checkInstalledLatest(getApplicationContext())) {
			setupButton.setTypeface(setupButton.getTypeface(), Typeface.BOLD);
		} else {
			setupButton.setTypeface(setupButton.getTypeface(), Typeface.NORMAL);
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if(!ConfigChecker.getSDStatus(false)) {
			showDialog(DIALOG_R_SD);
		} else {
			if(!ConfigChecker.getSDStatus(true)) {
				showDialog(DIALOG_R_SD);
			}
			firstTimeSetup();
		}
		startButton.setEnabled(ConfigChecker.checkInstalled(getApplicationContext()));
		
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(InstallService.INTENT_STATUSUPDATE);
		
		registerReceiver(statusReceiver, statusFilter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(statusReceiver);
	}

	private void firstTimeSetup() {
		final File sd = getExternalFilesDir(null);
		File imgDir = new File(sd, "img");
		if(!imgDir.exists()) if(!imgDir.mkdir()) Toast.makeText(this, "Couldn't create 'img' folder.", Toast.LENGTH_LONG).show();
		
		try {
			FileFinder.initialise(getApplicationContext());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if(e.getMessage().equals("su")) {
				showDialog(DIALOG_ROOT);
			} else if(e.getMessage().equals("busybox")) {
				showDialog(DIALOG_BB);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.setupButton:
				startActivity(new Intent(this, SetupStatus.class));
				break;
			case R.id.startButton:
				startActivity(new Intent(this, HackSelector.class));
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
				break;
		}
		return dialog;
	}
	
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
}
