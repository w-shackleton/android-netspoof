/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.servicestatus.NewLogOutput;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;

public class SpoofRunning extends Activity implements OnClickListener, LogConf {
	public static final String EXTRA_SPOOFDATA = "uk.digitalsquid.netspoofer.SpoofRunning.SPOOFDATA";
	
	private SpoofData spoof;
	
	private Button startButton;
	private TextView logOutput, spoofStatus;
	private ScrollView logscroller;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spoofrunning);
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		logOutput = (TextView) findViewById(R.id.logoutput);
		spoofStatus = (TextView) findViewById(R.id.spoofstatus);
		logscroller = (ScrollView) findViewById(R.id.logscroller);
		spoof = (SpoofData) getIntent().getSerializableExtra(EXTRA_SPOOFDATA);
		if(spoof == null) {
			finish();
		} else {
	        bindService(new Intent(this, NetSpoofService.class), mConnection, Context.BIND_AUTO_CREATE);
		}
		
		Config.configureRunningPage(this);
		
		// Google analytics
		Tracker t = ((App)getApplication()).getTracker();
		if(t != null) {
			t.setScreenName(getClass().getCanonicalName());
			t.send(new HitBuilders.AppViewBuilder().build());
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
        // Unbind from the service
        if (service != null) {
            unbindService(mConnection);
            service = null;
        }
	}

    @Override
    protected void onStart() {
        super.onStart();
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(NetSpoofService.INTENT_STATUSUPDATE);
	    statusFilter.addAction(NetSpoofService.INTENT_NEWLOGOUTPUT);
		registerReceiver(statusReceiver, statusFilter);
    }

    @Override
    protected void onStop() {
		unregisterReceiver(statusReceiver);
        super.onStop();
    }
	
	private NetSpoofService service;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			NetSpoofServiceBinder binder = (NetSpoofServiceBinder) service;
            SpoofRunning.this.service = binder.getService();
            
            updateStatus(SpoofRunning.this.service.getStatus());
		}
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
		}
	};
	
	private ArrayList<String> logList = new ArrayList<String>();
	private static final int LOG_LENGTH = 10;
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(NetSpoofService.INTENT_STATUSUPDATE)) {
				updateStatus(intent.getIntExtra(NetSpoofService.INTENT_EXTRA_STATUS, NetSpoofService.STATUS_FINISHED));
			} else if(intent.getAction().equals(NetSpoofService.INTENT_NEWLOGOUTPUT)) {
				NewLogOutput newLog = (NewLogOutput) intent.getSerializableExtra(NetSpoofService.INTENT_EXTRA_LOGOUTPUT);
				assert newLog != null;
				logList.addAll(newLog.getLogLines());
				logList.subList(0, logList.size() < LOG_LENGTH ? 0 : (logList.size() - LOG_LENGTH)).clear();
				StringBuilder sb = new StringBuilder();
				for(String entry : logList) {
					sb.append(entry);
					sb.append("\n");
				}
				logOutput.setText(sb);
				logscroller.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}
	};
	
	private void updateStatus(int status) {
		switch(status) {
        case NetSpoofService.STATUS_STARTING:
        	startButton.setEnabled(false);
        	startButton.setText(R.string.stop);
        	spoofStatus.setText(R.string.spoofstarting);
        	stopBackPress = true;
        	break;
        case NetSpoofService.STATUS_STARTED:
        	startButton.setEnabled(true);
        	startButton.setText(R.string.stop);
        	spoofStatus.setText(R.string.spoofstarted);
        	break;
        case NetSpoofService.STATUS_STOPPING:
        	startButton.setEnabled(false);
        	startButton.setText(R.string.start);
        	spoofStatus.setText(R.string.spoofstopping);
        	break;
        case NetSpoofService.STATUS_LOADED: // Stopped
        	startButton.setEnabled(true);
        	startButton.setText(R.string.start);
        	spoofStatus.setText(R.string.spoofnotrunning);
        	stopBackPress = false;
        	break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.startButton:
			switch(service.getStatus()) {
			case NetSpoofService.STATUS_LOADED:
				service.startSpoof(spoof);
				findViewById(R.id.spoofWarning).setVisibility(View.GONE);
				break;
			case NetSpoofService.STATUS_STARTED:
				service.stopSpoof();
				findViewById(R.id.spoofWarning).setVisibility(View.VISIBLE);
				break;
			}
			break;
		}
	}
	
	private boolean stopBackPress = false;
	
	// Stop accidental back press
	@Override
	public void onBackPressed() {
		if(!stopBackPress) finish();
		else {
			Toast.makeText(this, "Spoof is still running, please press back again to confirm that you want to exit.", Toast.LENGTH_LONG).show();
			stopBackPress = false;
		}
	}
}
