package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.config.ChrootManager;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.widget.Toast;

public class NetSpoofService extends Service implements LogConf {
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
	
	private final ChrootManager chroot = new ChrootManager(this);
	
    @Override
    public void onCreate() {
          super.onCreate();
    }
    
    private boolean started = false;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
    	if(!started) start();
    	return START_NOT_STICKY;
    }
    
    private void start() {
    	Toast.makeText(getApplicationContext(), "Starting chroot", Toast.LENGTH_LONG).show();
    	chroot.createFileSet();
    	
    	started = true;
    }
    
    @Override
    public void onDestroy() {
    	Toast.makeText(getApplicationContext(), "Stopping chroot", Toast.LENGTH_LONG).show();
    	super.onDestroy();
    }
}
