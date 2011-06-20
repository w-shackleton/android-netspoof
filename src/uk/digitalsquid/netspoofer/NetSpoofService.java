package uk.digitalsquid.netspoofer;

import java.io.IOException;

import uk.digitalsquid.netspoofer.config.ChrootConfig;
import uk.digitalsquid.netspoofer.config.ChrootManager;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.servicestatus.InitialiseStatus;
import uk.digitalsquid.netspoofer.servicestatus.ServiceStatus;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

public class NetSpoofService extends Service implements LogConf {
	public static final int STATUS_LOADING = 0;
	public static final int STATUS_LOADED = 1;
	public static final int STATUS_STARTED = 2;
	public static final int STATUS_STOPPED = 3;
	public static final int STATUS_FAILED = 4;
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.NetSpoofService.StatusUpdate";
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.NetSpoofService.status";
	
	public class NetSpoofServiceBinder extends Binder {
        NetSpoofService getService() {
            return NetSpoofService.this;
        }
	}
	private final NetSpoofServiceBinder binder = new NetSpoofServiceBinder();

	@Override
	public NetSpoofServiceBinder onBind(Intent arg0) {
		return binder;
	}
	
    @Override
    public void onCreate() {
          super.onCreate();
    }
    
    private boolean started = false;
	
	private int status;
	private void setStatus(int status) {
		this.status = status;
		broadcastStatus();
	}
	public int getStatus() {
		return status;
	}
	
	public void broadcastStatus() {
		Intent intent = new Intent(INTENT_STATUSUPDATE);
		intent.putExtra(INTENT_EXTRA_STATUS, getStatus());
		sendBroadcast(intent);
	}
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
    	if(!started) start();
    	return START_NOT_STICKY;
    }
    
    private void start() {
    	Toast.makeText(getApplicationContext(), "Starting chroot", Toast.LENGTH_LONG).show();
    	
    	mainLoopManager.execute(new ChrootConfig(getBaseContext()));
    	setStatus(STATUS_LOADING);
    	
    	started = true;
    }
    
    @Override
    public void onDestroy() {
    	Toast.makeText(getApplicationContext(), "Stopping chroot", Toast.LENGTH_LONG).show();
    	super.onDestroy();
    }
    
    private final AsyncTask<ChrootConfig, ServiceStatus, Void> mainLoopManager = new AsyncTask<ChrootConfig, ServiceStatus, Void>() {

		@Override
		protected Void doInBackground(ChrootConfig... params) {
			Log.i(TAG, "Setting up chroot files...");
			final ChrootManager chroot = new ChrootManager(NetSpoofService.this, params[0]);
	    	chroot.createFileSet();
	    	
			Log.i(TAG, "Starting chroot...");
			try {
				chroot.start();
			} catch (IOException e) {
				Log.e(TAG, "Chroot failed to load!");
				publishProgress(new InitialiseStatus(InitialiseStatus.INIT_FAIL));
				e.printStackTrace();
				chroot.stop();
				return null;
			}
			publishProgress(new InitialiseStatus(InitialiseStatus.INIT_COMPLETE));
	
			Log.i(TAG, "Stopping chroot...");
			chroot.stop();
			return null;
		}
    	
		protected void onProgressUpdate(ServiceStatus... progress) {
			ServiceStatus s = progress[0];
			if(s instanceof InitialiseStatus) {
				InitialiseStatus is = (InitialiseStatus) s;
				switch(is.status) {
				case InitialiseStatus.INIT_COMPLETE:
					setStatus(STATUS_LOADED);
					break;
				case InitialiseStatus.INIT_FAIL:
					setStatus(STATUS_FAILED);
					break;
				}
			}
		}
		protected void onPostExecute(Void result) {
		}
    };
}
