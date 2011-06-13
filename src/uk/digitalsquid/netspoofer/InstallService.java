package uk.digitalsquid.netspoofer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class InstallService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
	    start(intent);
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
	    start(intent);
		return START_STICKY;
	}
	
	private void start(Intent intent) {
		
	}
}
