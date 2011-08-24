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

import java.util.LinkedList;
import java.util.List;

import uk.digitalsquid.netspoofer.InstallService.DLProgress;
import uk.digitalsquid.netspoofer.config.Config;
import uk.digitalsquid.netspoofer.config.ConfigChecker;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SetupStatus extends Activity implements OnClickListener, Config {
	private Button dlButton;
	
	private ProgressBar dlProgress;
	
	private WebView webView;
	
	TextView status, dlProgressText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setupstatus);
		
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(InstallService.INTENT_STATUSUPDATE);
	    
		webView = (WebView)findViewById(R.id.sfWebView);
		
		status = (TextView) findViewById(R.id.dlStatus);
		dlProgressText = (TextView) findViewById(R.id.dlprogresstext);
		dlProgress = (ProgressBar) findViewById(R.id.dlprogress);
		dlButton = (Button) findViewById(R.id.dlButton);
		dlButton.setOnClickListener(this);
		if(ConfigChecker.isInstallServiceRunning(getApplicationContext())) {
			setWinStatus(true);
		} else { // Things for not running
			if(ConfigChecker.checkInstalledLatest(getApplicationContext())) {
				findViewById(R.id.redlconfirm).setVisibility(View.VISIBLE);
				dlButton.setText(R.string.redownload);
			}
		}
	}
	
	private final List<String> possibleSfURLs = new LinkedList<String>();
	
	/**
	 * Activates the WebView used to get the DL url from sourceforge, if needed.
	 */
	private void activateSFWV() {
		final WebViewClient wvc = new WebViewClient() {
        	@Override
        	public void onLoadResource(WebView view, String url) {
        		// Note to self: check this every now and then, as SF may change a bit once in a while.
        		if(url.startsWith("http://downloads.sourceforge.net/project/netspoof/debian-images/debian")) {
	        		Log.i("android-netspoof", "Found SF DL URL: " + url);
					if(!ConfigChecker.isInstallServiceRunning(getApplicationContext())) {
						startServiceForUrl(url);
					}
					else possibleSfURLs.add(url);
        		}
        	}
		};
		webView.setVisibility(View.VISIBLE);
		findViewById(R.id.refreshWeb).setVisibility(View.VISIBLE);
		findViewById(R.id.refreshWeb).setOnClickListener(this);
		webView.setWebViewClient(wvc);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(SF_DEB_IMG_URL);
	}
	
	private void startServiceForUrl(String url) {
		Intent intent = new Intent(getApplicationContext(), InstallService.class);
		intent.putExtra(InstallService.INTENT_START_URL, url);
		startService(intent);
		status.setText(R.string.dlStarting);
		dlButton.setText(R.string.dlCancel);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.dlButton:
			if(!ConfigChecker.isInstallServiceRunning(getApplicationContext())) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String downloadUrl = prefs.getString("debImgUrl", "");
				if(!downloadUrl.equals("")) {
					startServiceForUrl(downloadUrl);
				} else activateSFWV();
			} else {
				stopService(new Intent(getApplicationContext(), InstallService.class));
				findViewById(R.id.sfWebView).setVisibility(View.GONE);
				setWinStatus(false);
			}
			break;
		case R.id.refreshWeb:
			webView.reload();
			break;
		}
	}
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(intent.getIntExtra(InstallService.INTENT_EXTRA_STATUS, InstallService.STATUS_FINISHED)) {
			case InstallService.STATUS_STARTED:
				setWinStatus(true);
				break;
			case InstallService.STATUS_DOWNLOADING:
				DLProgress progress = (DLProgress) intent.getSerializableExtra(InstallService.INTENT_EXTRA_DLPROGRESS);
				if(progress == null)
					Log.w(TAG, "Error in receiving download status");
				else {
					dlProgress.setMax(progress.getKBytesTotal());
					dlProgress.setProgress(progress.getKBytesDone());
					float mbDone = (float)progress.getKBytesDone() / 1024;
					float mbTotal = (float)progress.getKBytesTotal() / 1024;
					if(!progress.isExtracting()) {
						dlProgressText.setText(String.format("%.1f / %.0fMB\nDownloading", mbDone, mbTotal));
					} else {
						dlProgressText.setText(String.format("%.1f / %.0fMB\nExtracting", mbDone, mbTotal));
					}
				}
				break;
			case InstallService.STATUS_FINISHED:
				switch(intent.getIntExtra(InstallService.INTENT_EXTRA_DLSTATE, InstallService.STATUS_DL_FAIL_DLERROR)) {
				case InstallService.STATUS_DL_FAIL_DLERROR:
					Toast.makeText(
							SetupStatus.this,
							"An error ocurred whilst downloading the file. Please try again",
							Toast.LENGTH_LONG).show();
					break;
				case InstallService.STATUS_DL_FAIL_IOERROR:
					Toast.makeText(
							SetupStatus.this,
							"Couldn't download file. Are you connected to the internet?",
							Toast.LENGTH_LONG).show();
					break;
				case InstallService.STATUS_DL_FAIL_MALFORMED_FILE:
					Toast.makeText(
							SetupStatus.this,
							"Couldn't locate download file. Please check download URL, or please report this as a bug.",
							Toast.LENGTH_LONG).show();
					break;
				case InstallService.STATUS_DL_FAIL_SDERROR:
					Toast.makeText(
							SetupStatus.this,
							"Couldn't open file for writing on SD card",
							Toast.LENGTH_LONG).show();
					break;
				case InstallService.STATUS_DL_SUCCESS:
					finish();
					break;
				case InstallService.STATUS_DL_CANCEL:
					Toast.makeText(
							SetupStatus.this,
							"Cancelled",
							Toast.LENGTH_LONG).show();
					break;
				}
				if(possibleSfURLs.isEmpty()) { // Don't do anything
					findViewById(R.id.sfWebView).setVisibility(View.GONE);
					setWinStatus(false);
				}
				break;
			}
		}
	};
	
	@Override
	protected void onResume() {
		registerReceiver(statusReceiver, statusFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(statusReceiver);
		super.onPause();
	}
	
	private void setWinStatus(boolean running) {
		if(running) {
			dlButton.setText(R.string.dlCancel);
			status.setText(R.string.dlStarted);
			dlProgress.setVisibility(View.VISIBLE);
			dlProgressText.setVisibility(View.VISIBLE);
			dlButton.setEnabled(true);
		} else {
			dlButton.setText(R.string.startdl);
			status.setText(R.string.dlnotrunning);
			dlProgress.setVisibility(View.GONE);
			dlProgressText.setVisibility(View.GONE);
			dlButton.setEnabled(true);
			if(ConfigChecker.checkInstalledLatest(getApplicationContext())) {
				dlButton.setText(R.string.redownload);
			} else {
				dlButton.setText(R.string.startdl);
			}
		}
		if(ConfigChecker.checkInstalledLatest(getApplicationContext())) {
			findViewById(R.id.redlconfirm).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.redlconfirm).setVisibility(View.GONE);
		}
	}
}
