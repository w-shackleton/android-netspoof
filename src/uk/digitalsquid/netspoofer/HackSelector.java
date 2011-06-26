package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import uk.digitalsquid.netspoofer.servicestatus.SpoofList;
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
	
	boolean haveSpoofList = false;
	boolean gettingSpoofList = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        startService(new Intent(this, NetSpoofService.class));
        
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(NetSpoofService.INTENT_STATUSUPDATE);
	    statusFilter.addAction(NetSpoofService.INTENT_SPOOFLIST);
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
            
            switch(HackSelector.this.service.getStatus()) {
            case NetSpoofService.STATUS_LOADING:
            	showStartingDialog();
            	break;
            case NetSpoofService.STATUS_LOADED:
            	if(!gettingSpoofList) {
            		gettingSpoofList = true;
            		HackSelector.this.service.requestSpoofs();
            	}
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
					showStartingDialog();
					break;
				case NetSpoofService.STATUS_LOADED:
					if(startingDialog != null) startingDialog.cancel();
	            	if(!gettingSpoofList) {
	            		gettingSpoofList = true;
	            		HackSelector.this.service.requestSpoofs();
	            	}
					break;
				case NetSpoofService.STATUS_STARTED:
					break;
				case NetSpoofService.STATUS_STOPPED:
					break;
				case NetSpoofService.STATUS_FAILED:
					break;
				}
			} else if(intent.getAction().equals(NetSpoofService.INTENT_SPOOFLIST)) {
				SpoofList spoofs = (SpoofList) intent.getSerializableExtra(NetSpoofService.INTENT_EXTRA_SPOOFLIST);
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
