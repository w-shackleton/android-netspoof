package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
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

public class SpoofRunning extends Activity {
	public static final String EXTRA_SPOOFDATA = "uk.digitalsquid.netspoofer.SpoofRunning.SPOOFDATA";
	
	private SpoofData spoof;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spoof = (SpoofData) getIntent().getSerializableExtra(EXTRA_SPOOFDATA);
		if(spoof == null) {
			
		}
        bindService(new Intent(this, NetSpoofService.class), mConnection, Context.BIND_AUTO_CREATE);
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
            
            switch(SpoofRunning.this.service.getStatus()) {
            case NetSpoofService.STATUS_STARTED:
            	break;
            case NetSpoofService.STATUS_STOPPED:
            	break;
            }
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
				switch(intent.getIntExtra(NetSpoofService.INTENT_EXTRA_STATUS, NetSpoofService.STATUS_STOPPED)) {
				case NetSpoofService.STATUS_LOADING:
					break;
				}
			}
		}
	};
}
