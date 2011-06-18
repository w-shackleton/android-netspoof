package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class HackSelector extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        bindService(new Intent(this, NetSpoofService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (service != null) {
            unbindService(mConnection);
            service = null;
        }
    }
	
	private NetSpoofService service;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			NetSpoofServiceBinder binder = (NetSpoofServiceBinder) service;
            HackSelector.this.service = binder.getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
		}
	};
}
