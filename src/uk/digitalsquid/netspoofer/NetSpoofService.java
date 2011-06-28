package uk.digitalsquid.netspoofer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.digitalsquid.netspoofer.config.ChrootConfig;
import uk.digitalsquid.netspoofer.config.ChrootManager;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.servicemsg.ServiceMsg;
import uk.digitalsquid.netspoofer.servicemsg.SpoofStarter;
import uk.digitalsquid.netspoofer.servicestatus.InitialiseStatus;
import uk.digitalsquid.netspoofer.servicestatus.ServiceStatus;
import uk.digitalsquid.netspoofer.servicestatus.SpoofList;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

public class NetSpoofService extends Service implements LogConf {
	public static final int STATUS_LOADING = 0;
	public static final int STATUS_LOADED = 1;
	public static final int STATUS_STARTING = 2;
	public static final int STATUS_STARTED = 3;
	public static final int STATUS_STOPPING = 4;
	public static final int STATUS_STOPPED = 5;
	public static final int STATUS_FAILED = 6;
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.NetSpoofService.StatusUpdate";
	public static final String INTENT_SPOOFLIST = "uk.digitalsquid.netspoofer.NetSpoofService.SpoofList";
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.NetSpoofService.status";
	public static final String INTENT_EXTRA_SPOOFLIST = "uk.digitalsquid.netspoofer.NetSpoofService.spooflist";
	
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
    	mainLoopManager.cancel(false);
    	tasks.add(new ServiceMsg(ServiceMsg.MESSAGE_STOP));
    	super.onDestroy();
    }
    
    public final void requestSpoofs() {
    	try {
	    	tasks.add(new ServiceMsg(ServiceMsg.MESSAGE_GETSPOOFS));
    	} catch(IllegalStateException e) {
    		e.printStackTrace();
    	}
    }
    
    private void sendSpoofList(SpoofList spoofs) {
		Intent intent = new Intent(INTENT_SPOOFLIST);
		intent.putExtra(INTENT_EXTRA_SPOOFLIST, spoofs);
		sendBroadcast(intent);
    }
    
    public void startSpoof(SpoofData spoof) {
    	try {
	    	tasks.add(new SpoofStarter(spoof));
    	} catch(IllegalStateException e) {
    		e.printStackTrace();
    	}
    }
    
	private final BlockingQueue<ServiceMsg> tasks = new LinkedBlockingQueue<ServiceMsg>();
    
    private final AsyncTask<ChrootConfig, ServiceStatus, Void> mainLoopManager = new AsyncTask<ChrootConfig, ServiceStatus, Void>() {

		@Override
		protected Void doInBackground(ChrootConfig... params) {
			Log.i(TAG, "Setting up chroot...");
			final ChrootManager chroot = new ChrootManager(NetSpoofService.this, params[0]);
	    	
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
			if(isCancelled()) {
				Log.i(TAG, "Stop initiated, stopping...");
				chroot.stop();
				Log.i(TAG, "Done.");
				return null;
			}
			
			// Main point. Process requests from task list.
			boolean running = true;
			while(running || !isCancelled()) {
				try {
					ServiceMsg task = tasks.take();
					switch(task.getMessage()) {
					case ServiceMsg.MESSAGE_OTHER:
						if(task instanceof SpoofStarter) {
							SpoofStarter starter = (SpoofStarter) task;
							spoofLoop(chroot, starter.getSpoof());
						}
						break;
					case ServiceMsg.MESSAGE_STOP:
						running = false;
						break;
					case ServiceMsg.MESSAGE_GETSPOOFS:
						SpoofList list = new SpoofList(chroot.getSpoofList());
						publishProgress(list);
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	
			Log.i(TAG, "Stopping chroot...");
			chroot.stop();
			Log.i(TAG, "Done.");
			return null;
		}
		
		private void spoofLoop(final ChrootManager chroot, SpoofData spoof) {
			publishProgress(new InitialiseStatus(STATUS_STARTING));
			try {
				chroot.startSpoof(spoof);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to start spoof.");
				publishProgress(new InitialiseStatus(STATUS_STOPPED));
			}
			publishProgress(new InitialiseStatus(STATUS_STARTED));
			
			boolean running = true;
			while(running) {
				ServiceMsg task = tasks.poll();
				switch(task.getMessage()) {
				case ServiceMsg.MESSAGE_STOPSPOOF:
					finishSpoof(chroot, spoof);
					running = false;
					break;
				}
				
				if(isCancelled()) {
					finishSpoof(chroot, spoof);
					break;
				}
				
			}
		}
		
		private void finishSpoof(ChrootManager chroot, SpoofData spoof) {
			publishProgress(new InitialiseStatus(STATUS_STOPPING));
			try {
				chroot.stopSpoof(spoof);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to stop spoof.");
				publishProgress(new InitialiseStatus(STATUS_STARTED));
				return;
			}
			publishProgress(new InitialiseStatus(STATUS_STOPPED));
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
			} else if(s instanceof SpoofList) {
				sendSpoofList((SpoofList)s);
			}
		}
		protected void onPostExecute(Void result) {
			setStatus(STATUS_STOPPED);
			stopSelf();
		}
    };
}
