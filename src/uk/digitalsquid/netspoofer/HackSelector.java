package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

public class HackSelector extends Activity {
	ProgressDialog startingProgress;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        startService(new Intent(this, NetSpoofService.class));
        
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(NetSpoofService.INTENT_STATUSUPDATE);
		registerReceiver(statusReceiver, statusFilter);
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
        stopService(new Intent(this, NetSpoofService.class));
		unregisterReceiver(statusReceiver);
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
            
            if(HackSelector.this.service.getStatus() == NetSpoofService.STATUS_LOADING) {
            	showStartingDialog();
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
			switch(intent.getIntExtra(NetSpoofService.INTENT_EXTRA_STATUS, NetSpoofService.STATUS_STOPPED)) {
			case NetSpoofService.STATUS_LOADING:
				showStartingDialog();
				break;
			case NetSpoofService.STATUS_LOADED:
				if(startingDialog != null) startingDialog.cancel();
				break;
			case NetSpoofService.STATUS_STARTED:
				break;
			case NetSpoofService.STATUS_STOPPED:
				break;
			case NetSpoofService.STATUS_FAILED:
				break;
			}
			
		}
	};
	
	private ProgressDialog startingDialog;
	
	private void showStartingDialog() {
		if(startingDialog != null) if(startingDialog.isShowing()) return;
		startingDialog = new ProgressDialog(this);
		startingDialog.setTitle(R.string.loading);
		startingDialog.setMessage("Starting environment... This should take a few seconds");
		startingDialog.setCancelable(false);
		startingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		    @Override
		    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		        if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
		            return true;
		        }
		        return false;
		    }
		});
		startingDialog.show();
	}
}
