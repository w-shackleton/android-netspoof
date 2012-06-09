/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2011 Will Shackleton
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.digitalsquid.netspoofer.config.ChrootConfig;
import uk.digitalsquid.netspoofer.config.ChrootManager;
import uk.digitalsquid.netspoofer.config.FileFinder;
import uk.digitalsquid.netspoofer.config.IOHelpers;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.misc.AsyncTaskHelper;
import uk.digitalsquid.netspoofer.servicemsg.ImageLoader;
import uk.digitalsquid.netspoofer.servicemsg.ServiceMsg;
import uk.digitalsquid.netspoofer.servicemsg.SpoofStarter;
import uk.digitalsquid.netspoofer.servicestatus.InitialiseStatus;
import uk.digitalsquid.netspoofer.servicestatus.NewLogOutput;
import uk.digitalsquid.netspoofer.servicestatus.Notifyer;
import uk.digitalsquid.netspoofer.servicestatus.ServiceStatus;
import uk.digitalsquid.netspoofer.servicestatus.SpoofList;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class NetSpoofService extends Service implements LogConf {
	public static final int STATUS_LOADING = 0;
	public static final int STATUS_LOADED = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_STARTING = 3;
	public static final int STATUS_STARTED = 4;
	public static final int STATUS_STOPPING = 5;
	public static final int STATUS_FAILED = 6;
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.NetSpoofService.StatusUpdate";
	public static final String INTENT_SPOOFLIST = "uk.digitalsquid.netspoofer.NetSpoofService.SpoofList";
	public static final String INTENT_NEWLOGOUTPUT = "uk.digitalsquid.netspoofer.NetSpoofService.NewLogOutput";
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.NetSpoofService.status";
	public static final String INTENT_EXTRA_SPOOFLIST = "uk.digitalsquid.netspoofer.NetSpoofService.spooflist";
	public static final String INTENT_EXTRA_LOGOUTPUT = "uk.digitalsquid.netspoofer.NetSpoofService.logoutput";
	
	private static final int NS_RUNNING = 1;
	
	private NotificationManager notificationManager;
	private Notification notification;
	
	public class NetSpoofServiceBinder extends Binder {
        public NetSpoofService getService() {
            return NetSpoofService.this;
        }
	}
	private final NetSpoofServiceBinder binder = new NetSpoofServiceBinder();

	@Override
	public NetSpoofServiceBinder onBind(Intent arg0) {
		return binder;
	}
	
	/**
	 * Used to do things on the UI thread (ie toasts)
	 */
	Handler uiThreadHandler;
	
    @Override
    public void onCreate() {
          super.onCreate();
          uiThreadHandler = new Handler();
    }
    
    /**
     * Shows a toast on the UI thread
     * @param text
     * @param duration
     */
    private void showToast(final String text, final int duration) {
    	uiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getBaseContext(), text, duration).show();
			}
		});
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
    	Toast.makeText(getApplicationContext(), "Loaded setup", Toast.LENGTH_LONG).show();
    	
    	AsyncTaskHelper.execute(mainLoopManager, new ChrootConfig(getBaseContext()));
    	setStatus(STATUS_LOADING);
    	
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	started = true;
    }
    
    @Override
    public void onDestroy() {
    	Toast.makeText(getApplicationContext(), "Unloaded setup", Toast.LENGTH_LONG).show();
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
    
    public void stopSpoof() {
    	try {
	    	tasks.add(new ServiceMsg(ServiceMsg.MESSAGE_STOPSPOOF));
    	} catch(IllegalStateException e) {
    		e.printStackTrace();
    	}
    }
    
    private void sendLogOutput(NewLogOutput logOutput) {
		Intent intent = new Intent(INTENT_NEWLOGOUTPUT);
		intent.putExtra(INTENT_EXTRA_LOGOUTPUT, logOutput);
		sendBroadcast(intent);
    }
    
    public void saveImageToWebserver(Uri image) {
    	try {
	    	tasks.add(new ImageLoader(image));
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
				if(!chroot.start()) {
					Log.e(TAG, "Chroot start returned false, not mounted");
					throw new IOException("Mounted chroot not found after start command executed.");
				}
			} catch (IOException e) {
				Log.e(TAG, "Chroot failed to load!");
				publishProgress(new InitialiseStatus(STATUS_FAILED));
				e.printStackTrace();
				try {
					chroot.stop();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return null;
			}
			publishProgress(new InitialiseStatus(STATUS_LOADED));
			if(isCancelled()) {
				Log.i(TAG, "Stop initiated, stopping...");
				try {
					chroot.stop();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
						} else if(task instanceof ImageLoader) {
							loadImageToDebian(chroot, ((ImageLoader)task).getUri());
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
			try {
				chroot.stop();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Chroot failed to stop.");
				publishProgress(new InitialiseStatus(STATUS_FAILED));
			}
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
				publishProgress(new InitialiseStatus(STATUS_LOADED));
			}
			publishProgress(new InitialiseStatus(STATUS_STARTED));
			publishProgress(new Notifyer(NS_RUNNING, Notifyer.STATUS_SHOW));
			
			boolean running = true;
			while(running) {
				ServiceMsg task = tasks.poll();
				if(task != null) {
					switch(task.getMessage()) {
					case ServiceMsg.MESSAGE_STOPSPOOF:
						stopSpoof(chroot, spoof);
						running = false;
						break;
					}
				}
				
				if(isCancelled()) {
					stopSpoof(chroot, spoof);
					running = false;
					break;
				}
				
				if(chroot.checkIfStopped()) {
					finishSpoof(chroot, spoof);
					running = false;
					break;
				}
				
				try {
					publishProgress(new NewLogOutput(chroot.getNewSpoofOutput())); // Send log back to anything listening.
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try { Thread.sleep(600); } catch (InterruptedException e) {}
			}
		}
		
		private void stopSpoof(ChrootManager chroot, SpoofData spoof) {
			publishProgress(new InitialiseStatus(STATUS_STOPPING));
			publishProgress(new Notifyer(NS_RUNNING, Notifyer.STATUS_HIDE));
			try {
				chroot.stopSpoof(spoof);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to stop spoof.");
				publishProgress(new InitialiseStatus(STATUS_STARTED));
				return;
			}
		}
		
		private void finishSpoof(ChrootManager chroot, SpoofData spoof) {
			try {
				publishProgress(new NewLogOutput(chroot.finishStopSpoof())); // Also send final output.
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to finish spoof.");
				return;
			}
			publishProgress(new InitialiseStatus(STATUS_LOADED));
		}
		
		/**
		 * Loads the image then saves it as a jpg to the debian.
		 * @param imageUri
		 */
		private void loadImageToDebian(ChrootManager chroot, Uri imageUri) {
			
			File tmpImage = new File(getFilesDir(), "customimage.jpg");
			Log.i(TAG, "Loading image from media store");
			try {
				Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
				Log.i(TAG, "Writing image");
				FileOutputStream out = openFileOutput("customimage.jpg", MODE_WORLD_READABLE);
				image.compress(CompressFormat.JPEG, 80, out);
				out.close();
				image.recycle();
				Log.i(TAG, "Written image");
				
				// Use BB to copy this as SU (need root to write to debian area)
				final String su = FileFinder.SU;
				final String bb = FileFinder.BUSYBOX;
				final File debianImageFolder = new File(chroot.config.getDebianMount() + "/var/www/images");
				final File debianImage = new File(debianImageFolder, "customimage.jpg");
				
				final List<String> mkdirArgs = new LinkedList<String>();
				// su -c busybox mkdir 
				mkdirArgs.add(su);
				mkdirArgs.add("-c");
				mkdirArgs.add(bb + " " + "mkdir" + " " + debianImageFolder.getCanonicalPath());
				Log.v(TAG, "Running " + mkdirArgs);
				try {
					IOHelpers.runProcess(mkdirArgs); // Is this necessary?
				} catch (IOException e) {
					Log.e(TAG, "Failed to create image folder.");
				}
				
				final List<String> cpArgs = new LinkedList<String>();
				// su busybox cp tmp.jpg deb.jpg
				cpArgs.add(su);
				cpArgs.add("-c");
				cpArgs.add(bb + " " + "cp" + " " + tmpImage.getCanonicalPath() + " " + debianImage.getCanonicalPath());
				Log.v(TAG, "Running " + cpArgs);
				if(IOHelpers.runProcess(cpArgs) != 0) {
					throw new IOException("Couldn't copy image from tmp to debian.\n Used cmdline " + cpArgs.toString());
				}
				Log.i(TAG, "Copied image to debian folder.");
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Couldn't open temporary file to write image", e);
				showToast("Couldn't load selected image!", Toast.LENGTH_LONG);
			} catch (IOException e) {
				Log.e(TAG, "Couldn't write to temporary image", e);
				showToast("Couldn't load selected image!", Toast.LENGTH_LONG);
			} finally {
				try {
					tmpImage.delete();
				} catch(Exception e) { } // Don't care if it fails.
			}
		}
    	
		protected void onProgressUpdate(ServiceStatus... progress) {
			ServiceStatus s = progress[0];
			if(s instanceof InitialiseStatus) {
				InitialiseStatus is = (InitialiseStatus) s;
				setStatus(is.status);
			} else if(s instanceof SpoofList) {
				sendSpoofList((SpoofList)s);
			} else if(s instanceof NewLogOutput) {
				sendLogOutput((NewLogOutput) s);
			} else if(s instanceof Notifyer) {
				Notifyer n = (Notifyer) s;
				
				switch(n.getStatus()) {
				case Notifyer.STATUS_SHOW:
					switch(n.getNotificationType()) {
					case NS_RUNNING:
						notification = new Notification(R.drawable.status, getString(R.string.spoofRunning), System.currentTimeMillis());
						notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
						
						Intent notificationIntent = new Intent(NetSpoofService.this, NetSpoofService.class);
						PendingIntent contentIntent = PendingIntent.getActivity(NetSpoofService.this, 0, notificationIntent, 0);
						
						notification.setLatestEventInfo(NetSpoofService.this, NetSpoofService.this.getString(R.string.spoofRunning), NetSpoofService.this.getString(R.string.spoofRunningDesc), contentIntent);
						notificationManager.notify(NS_RUNNING, notification);
						break;
					}
					break;
				case Notifyer.STATUS_HIDE:
						notificationManager.cancel(NS_RUNNING);
					break;
				}
			}
		}
		protected void onPostExecute(Void result) {
			setStatus(STATUS_FINISHED);
			stopSelf();
		}
    };
}
