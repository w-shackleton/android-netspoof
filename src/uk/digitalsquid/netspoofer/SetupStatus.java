package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.InstallService.DLProgress;
import uk.digitalsquid.netspoofer.config.Config;
import uk.digitalsquid.netspoofer.config.ConfigChecker;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SetupStatus extends Activity implements OnClickListener, LogConf {
	
	private boolean serviceRunning = false;
	
	private Button dlButton;
	
	private ProgressBar dlProgress;
	
	TextView status, dlProgressText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setupstatus);
		
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(InstallService.INTENT_STATUSUPDATE);
		
		status = (TextView) findViewById(R.id.dlStatus);
		dlProgressText = (TextView) findViewById(R.id.dlprogresstext);
		dlProgress = (ProgressBar) findViewById(R.id.dlprogress);
		dlButton = (Button) findViewById(R.id.dlButton);
		dlButton.setOnClickListener(this);
		if(ConfigChecker.isInstallServiceRunning(getApplicationContext())) {
			setWinStatus(true);
			// 'Start' service again to receive broadcasted status.
			// startService(new Intent(getApplicationContext(), InstallService.class));
			serviceRunning = true;
		} else { // Things for not running
			if(ConfigChecker.checkInstalledLatest(getApplicationContext())) {
				findViewById(R.id.redlconfirm).setVisibility(View.VISIBLE);
				dlButton.setText(R.string.redownload);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.dlButton:
			if(!serviceRunning) {
				startService(new Intent(getApplicationContext(), InstallService.class));
				status.setText(R.string.dlStarting);
				dlButton.setText(R.string.dlCancel);
				serviceRunning = true;
			} else {
				stopService(new Intent(getApplicationContext(), InstallService.class));
				setWinStatus(false);
				serviceRunning = false;
			}
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
					float mbDlTotal = (float)progress.getKBytesDownloadTotal() / 1024;
					dlProgressText.setText(String.format("%.1f / %.0fMB\nCompressed download is %.0fMB.", mbDone, mbTotal, mbDlTotal));
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
							"Couldn't locate download file '" + Config.DEB_IMG_URL + "'. Please report this as a bug.",
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
				setWinStatus(false);
				serviceRunning = false;
				break;
			}
		}
	};
	
	@Override
	protected void onResume() {
		registerReceiver(statusReceiver, statusFilter);
		
		// Receive status again.
		if(serviceRunning)
			startService(new Intent(getApplicationContext(), InstallService.class));
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
