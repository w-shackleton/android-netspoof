package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;

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
	
    @Override
    public void onCreate() {
          super.onCreate();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
    	return START_NOT_STICKY;
    }
    
    @Override
    public void onDestroy() {
          super.onDestroy();
    }
}
