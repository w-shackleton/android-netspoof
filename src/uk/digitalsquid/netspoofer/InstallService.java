/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CancellationException;
import java.util.zip.GZIPInputStream;

import uk.digitalsquid.netspoofer.config.Config;
import uk.digitalsquid.netspoofer.config.ConfigChecker;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class InstallService extends Service implements Config {
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.InstallService.status";
	public static final String INTENT_EXTRA_DLPROGRESS = "uk.digitalsquid.netspoofer.InstallService.dlprogress";
	public static final String INTENT_EXTRA_DLSTATE = "uk.digitalsquid.netspoofer.InstallService.dlprogress";
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.config.ConfigChecker.StatusUpdate";
	public static final String INTENT_START_URL = "uk.digitalsquid.netspoofer.config.InstallStatus.URL";
	/**
	 * When a file is given as the URL, pass true to this extra.
	 */
	public static final String INTENT_START_FILE = "uk.digitalsquid.netspoofer.config.InstallStatus.isFile";
	public static final String INTENT_START_URL_UNZIPPED = "uk.digitalsquid.netspoofer.config.InstallStatus.URLUnzipped";
	public static final String INTENT_START_URL_UPGRADE = "uk.digitalsquid.netspoofer.config.InstallStatus.isUpgrade";
	public static final int STATUS_STARTED = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_FINISHED = 2;
	
	public static final int STATUS_DL_SUCCESS = 0;
	public static final int STATUS_DL_FAIL_MALFORMED_FILE = 1;
	public static final int STATUS_DL_FAIL_IOERROR = 2;
	public static final int STATUS_DL_FAIL_SDERROR = 3;
	public static final int STATUS_DL_FAIL_DLERROR = 4;
	public static final int STATUS_DL_CANCEL = 5;
	
	private static final int DL_NOTIFY = 1;
	
	private int status = STATUS_STARTED;
	private int dlstatus = STATUS_DL_SUCCESS;
	
	private NotificationManager notificationManager;
	private Notification notification;
	
	boolean started = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
	    if(!started) start(intent);
		broadcastStatus();
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
	    if(!started) start(intent);
		broadcastStatus();
		return START_REDELIVER_INTENT;
	}
	
	private void start(Intent intent) {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notification = new Notification(R.drawable.status, getString(R.string.downloading), System.currentTimeMillis());
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.dl_notification);
		notification.contentView = contentView;
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		
		Intent notificationIntent = new Intent(this, InstallService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.contentIntent = contentIntent;
		
		notificationManager.notify(DL_NOTIFY, notification);
		
		started = true;
		// SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		// String downloadUrl = prefs.getString("debImgUrl", DEB_IMG_URL);
		String downloadUrl = intent.getStringExtra(INTENT_START_URL);
		boolean downloadUnzipped = intent.getBooleanExtra(INTENT_START_URL_UNZIPPED, false);
		boolean isUpgrade = intent.getBooleanExtra(INTENT_START_URL_UPGRADE, false);
		boolean useLocalFile = intent.getBooleanExtra(INTENT_START_FILE, false);
		if(downloadUrl == null) throw new IllegalArgumentException("Start URL was null");
		Log.v(TAG, "Downloading file " + downloadUrl);
		// if(downloadUrl.equals("")) downloadUrl = DEB_IMG_URL;
		downloadTask.execute(new DlStartData(downloadUrl, downloadUnzipped, isUpgrade, useLocalFile));
	}
	
	/**
	 * Information about the download to start
	 * @author william
	 *
	 */
	private static class DlStartData implements Serializable {
		private static final long serialVersionUID = 6287320665354658386L;
		public final String url;
		public final boolean unzipped;
		/**
		 * When <code>true</code>, indicates an incremental upgrade (ie. a patch).
		 */
		public final boolean upgrade;
		public final boolean useLocalFile;
		
		
		public DlStartData(String url, boolean unzipped, boolean isUpgrade, boolean useLocal) {
			this.url = url;
			this.unzipped = unzipped;
			this.upgrade = isUpgrade;
			useLocalFile = useLocal;
		}
	}
	
	private DLProgress dlProgress = new DLProgress(0, 1024);
	
	private void broadcastStatus() {
		Intent intent = new Intent(INTENT_STATUSUPDATE);
		intent.putExtra(INTENT_EXTRA_STATUS, status);
		switch(status) {
		case STATUS_STARTED:
			break;
		case STATUS_DOWNLOADING:
			intent.putExtra(INTENT_EXTRA_DLPROGRESS, dlProgress);
			break;
		case STATUS_FINISHED:
			intent.putExtra(INTENT_EXTRA_DLSTATE, dlstatus);
			break;
		}
		sendBroadcast(intent);
	}
	
	@Override
	public void onDestroy() {
		notificationManager.cancel(DL_NOTIFY);
		downloadTask.cancel(false);
	}
	
	public static final class DLProgress implements Serializable {
		private static final long serialVersionUID = -5366348392979726959L;
		
		public static final int STATUS_DOWNLOADING = 1;
		public static final int STATUS_EXTRACTING = 2;
		public static final int STATUS_PATCHING = 3;
		public static final int STATUS_RECOVERING = 4;
		
		private final int status;
		
		public DLProgress(int bytesDone, int bytesTotal) {
			this.bytesDone = bytesDone;
			this.bytesTotal = bytesTotal;
			status = STATUS_DOWNLOADING;
		}
		public DLProgress(int status, int bytesDone, int bytesTotal) {
			this.bytesDone = bytesDone;
			this.bytesTotal = bytesTotal;
			this.status = status;
		}
		
		public int getBytesDone() {
			return bytesDone;
		}
		public int getBytesTotal() {
			return bytesTotal;
		}
		public void setBytesDone(int bytes) {
			bytesDone = bytes;
		}
		public void setBytesTotal(int bytes) {
			bytesTotal = bytes;
		}
		public int getKBytesDone() {
			return bytesDone / 1024;
		}
		public int getKBytesTotal() {
			return bytesTotal / 1024;
		}
		public int getStatus() {
			return status;
		}
		private int bytesDone, bytesTotal;
	}
	
	public static interface DLProgressPublisher {
		public void publishDLProgress(DLProgress progress);
	}
	
	public final DownloadTask downloadTask = new DownloadTask();
	
	/**
	 * Task to download and extract data. This should be split up into more modular sections at some point.
	 */
	private final class DownloadTask extends AsyncTask<DlStartData, DLProgress, Integer> implements DLProgressPublisher {
		private InputStream response;
		private URLConnection connection;
		
		private File sd;
		/**
		 * This is the destination to download to. In the case of an upgrade, it is the upgrade file.
		 */
		private File dlDestination;
		
		private URL downloadURL;
		private FileOutputStream dlWriter;
		
		public void publishDLProgress(DLProgress progress) {
			publishProgress(progress);
		}
		
		@Override
		protected Integer doInBackground(DlStartData... params) {
			if(params.length != 1) throw new IllegalArgumentException("Please specify 1 parameter");
			boolean downloadUnzipped = params[0].unzipped;
			boolean useLocalFile = params[0].useLocalFile;
			final boolean upgrade = params[0].upgrade;
			if(upgrade) { // Never use local file when upgrading, and don't extract
				useLocalFile = false;
				downloadUnzipped = true;
			}
			sd = getExternalFilesDir(null);
			
			if(upgrade) {
				dlDestination = new File(getFilesDir() + "/" + "upgrade.zip");
			} else if(downloadUnzipped) {
				dlDestination = new File(sd.getAbsolutePath() + "/" + DEB_IMG); // Save directly to new location
			} else {
				dlDestination = new File(sd.getAbsolutePath() + "/" + DEB_IMG_GZ);
			}
			try {
				dlDestination.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return STATUS_DL_FAIL_SDERROR;
			}
			
			// Delete version file
			final int oldVersionNumber = ConfigChecker.getVersionNumber(getBaseContext());
			File oldversion = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			oldversion.delete();
			
			boolean done = true;
			if(!useLocalFile) {
				try {
					dlWriter = new FileOutputStream(dlDestination);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				try {
					downloadURL = new URL(params[0].url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return STATUS_DL_FAIL_MALFORMED_FILE;
				}
				
				int fileSize = 0;
				try {
					connection = downloadURL.openConnection();
					connection.connect();
					fileSize = connection.getContentLength();
				} catch (IOException e1) { }
				
				int downloaded = 0;
				while(downloaded < fileSize) {
					if(isCancelled()) {
						done = false;
						break;
					}
					try {
						downloaded += tryDownload(downloaded, fileSize);
					} catch (CancellationException e) {
						done = false;
						break;
					} catch (IOException e) {
						e.printStackTrace();
						Log.w(TAG, "Download failed, trying to continue...");
						try { Thread.sleep(300); } catch (InterruptedException e1) { }
					}
				}
				
				try {
					dlWriter.close();
					if(response != null) response.close(); // Could be nothing that was downloaded
				} catch (IOException e) {
				}
			} else { // Use local
				File localFile = new File(params[0].url);
				if(!localFile.exists()) return STATUS_DL_FAIL_IOERROR;
				if(!localFile.renameTo(dlDestination)) return STATUS_DL_FAIL_SDERROR;
			}
			
			if(!downloadUnzipped) { // Don't bother extracting
				try {
					unzip(dlDestination);
				} catch (IOException e1) {
					e1.printStackTrace();
					done = false;
				}
			}
			
			if(upgrade) { // Perform zip based upgrade
				// TODO: Upgrade code here
			}
			
			if(done) {
				File version = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
				try {
					version.createNewFile(); // Make sure exists
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					FileOutputStream versionWriter = new FileOutputStream(version);
					versionWriter.write(("" + Config.DEB_IMG_URL_VERSION).getBytes());
					versionWriter.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Couldn't write version file");
				}
			}
			
			Log.i(TAG, "Finished download");
			return STATUS_DL_SUCCESS;
		}
		
		/**
		 * Tries to download part of the file
		 * @return the number of bytes downloaded
		 */
		private int tryDownload(final int bytesSoFar, final int totalBytes) throws IOException, CancellationException {
			connection = downloadURL.openConnection();
			connection.setRequestProperty("Range", "bytes=" + bytesSoFar + "-");
			connection.connect();
			response = new BufferedInputStream(connection.getInputStream());
			
			byte[] dlData = new byte[0x8000];
			int bytesRead = 0;
			int bytesDone = 0;
			DLProgress progress = new DLProgress(bytesSoFar, totalBytes);
			
			int i = 0;
			try {
				while((bytesRead = response.read(dlData)) != -1) {
					bytesDone += bytesRead;
					dlWriter.write(dlData, 0, bytesRead);
					if(i++ > 6) {
						i = 0;
						progress.setBytesDone(bytesDone + bytesSoFar);
						publishProgress(progress);
					}
					
					if(isCancelled()) {
						response.close();
						dlWriter.close();
						// Remove old files
						File delversion = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
						delversion.delete();
						File deldebian = new File(sd.getAbsolutePath() + "/" + DEB_IMG_GZ);
						deldebian.delete();
						throw new CancellationException();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					response.close(); // Simply close, return number written.
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return bytesDone;
		}
		
		private String unzip(File inFile) throws IOException
		{
		    InputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(inFile)));
		 
			DLProgress progress = new DLProgress(DLProgress.STATUS_EXTRACTING, 0, Config.DEB_IMG_URL_SIZE);
		 
		    String outFilePath = inFile.getAbsolutePath().replace(".gz", "");
		    OutputStream out = new FileOutputStream(outFilePath);
		    
		    byte[] buf = new byte[0x8000];
		    int len, total = 0;
		    int i = 0;
		    while ((len = gzipInputStream.read(buf)) > 0) {
		    	total += len;
		        out.write(buf, 0, len);
		        if(i++ > 60) {
		        	i = 0;
					progress.setBytesDone(total);
					publishProgress(progress);
		        }
		    }
		 
		    gzipInputStream.close();
		    out.close();
		 
		    inFile.delete();
		 
		    return outFilePath;
		}
		
		int statusUpdate = 0;
		protected void onProgressUpdate(DLProgress... progress) {
			status = STATUS_DOWNLOADING;
			dlProgress = progress[0];
			if(statusUpdate++ > 71) {
				statusUpdate = 0;
				notification.contentView.setProgressBar(R.id.dlProgressBar, dlProgress.bytesTotal, dlProgress.bytesDone, false);
				notificationManager.notify(DL_NOTIFY, notification);
			}
			broadcastStatus();
		}
		protected void onPostExecute(Integer result) {
			status = STATUS_FINISHED;
			dlstatus = result;
			broadcastStatus();
			onFinish();
		}
		
		@Override
		protected void onCancelled() {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	};
	
	void onFinish() {
		stopSelf();
	}
}
