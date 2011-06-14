package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.config.ConfigChecker;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SetupStatus extends Activity implements OnClickListener {
	
	private boolean serviceRunning = false;
	
	TextView status;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setupstatus);
		
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(InstallService.INTENT_STATUSUPDATE);
		
		status = (TextView) findViewById(R.id.dlStatus);
		findViewById(R.id.dlButton).setOnClickListener(this);
		if(ConfigChecker.isInstallServiceRunning(getApplicationContext())) {
			findViewById(R.id.dlButton).setEnabled(false);
			// 'Start' service again to receive broadcasted status.
			serviceRunning = true;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.dlButton:
			startService(new Intent(getApplicationContext(), InstallService.class));
			v.setEnabled(false);
			status.setText(R.string.dlStarting);
			break;
		}
	}
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(intent.getIntExtra(InstallService.INTENT_EXTRA_STATUS, InstallService.STATUS_FINISHED)) {
			case InstallService.STATUS_STARTED:
				status.setText(R.string.dlStarted);
				break;
			case InstallService.STATUS_DOWNLOADING:
				break;
			case InstallService.STATUS_FINISHED:
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
}
