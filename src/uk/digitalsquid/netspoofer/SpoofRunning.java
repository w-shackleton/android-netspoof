package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
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

public class SpoofRunning extends Activity implements OnClickListener, LogConf {
	public static final String EXTRA_SPOOFDATA = "uk.digitalsquid.netspoofer.SpoofRunning.SPOOFDATA";
	
	private SpoofData spoof;
	
	private Button startButton;
	private TextView logOutput;
	private ScrollView logscroller;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spoofrunning);
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		logOutput = (Button) findViewById(R.id.logoutput);
		logscroller = (ScrollView) findViewById(R.id.logscroller);
		spoof = (SpoofData) getIntent().getSerializableExtra(EXTRA_SPOOFDATA);
		if(spoof == null) {
			finish();
		} else {
	        bindService(new Intent(this, NetSpoofService.class), mConnection, Context.BIND_AUTO_CREATE);
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
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(NetSpoofService.INTENT_STATUSUPDATE)) {
				updateStatus(intent.getIntExtra(NetSpoofService.INTENT_EXTRA_STATUS, NetSpoofService.STATUS_FINISHED));
			}
		}
	};
	
	private void updateStatus(int status) {
		switch(status) {
        case NetSpoofService.STATUS_STARTING:
        	startButton.setEnabled(false);
        	logOutput.setText(R.string.spoofstarting);
        	break;
        case NetSpoofService.STATUS_STARTED:
        	startButton.setEnabled(true);
        	logOutput.setText(R.string.spoofstarted);
        	break;
        case NetSpoofService.STATUS_STOPPING:
        	startButton.setEnabled(false);
        	logOutput.setText(R.string.spoofstopping);
        	break;
        case NetSpoofService.STATUS_LOADED:
        	startButton.setEnabled(true);
        	logOutput.setText(R.string.spoofnotrunning);
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
				break;
			case NetSpoofService.STATUS_STARTED:
				service.stopSpoof();
				break;
			}
			break;
		}
	}
}
