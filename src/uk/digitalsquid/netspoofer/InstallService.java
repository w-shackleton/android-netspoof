package uk.digitalsquid.netspoofer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import uk.digitalsquid.netspoofer.config.Config;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class InstallService extends Service implements Config {
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.InstallService.status";
	public static final String INTENT_EXTRA_DLPROGRESS = "uk.digitalsquid.netspoofer.InstallService.dlprogress";
	public static final String INTENT_EXTRA_DLSTATE = "uk.digitalsquid.netspoofer.InstallService.dlprogress";
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.config.ConfigChecker.StatusUpdate";
	public static final int STATUS_STARTED = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_FINISHED = 2;
	
	public static final int STATUS_DL_SUCCESS = 0;
	public static final int STATUS_DL_FAIL_MALFORMED_FILE = 1;
	public static final int STATUS_DL_FAIL_IOERROR = 2;
	public static final int STATUS_DL_FAIL_SDERROR = 3;
	public static final int STATUS_DL_FAIL_DLERROR = 4;
	public static final int STATUS_DL_CANCEL = 5;
	
	private int status = STATUS_STARTED;
	private int dlstatus = STATUS_DL_SUCCESS;
	
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
		return START_STICKY;
	}
	
	private void start(Intent intent) {
		started = true;
		downloadTask.execute(DEB_IMG_URL);
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
		downloadTask.cancel(false);
	}
	
	public static final class DLProgress implements Serializable {
		private static final long serialVersionUID = -5366348392979726959L;
		
		public DLProgress(int bytesDone, int bytesTotal) {
			this.bytesDone = bytesDone;
			this.bytesTotal = bytesTotal;
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
		public void setBytesDownloadTotal(int bytesDownload) {
			this.bytesDownloadTotal = bytesDownload;
		}
		public int getBytesDownloadTotal() {
			return bytesDownloadTotal;
		}
		public int getKBytesDownloadTotal() {
			return bytesDownloadTotal / 1024;
		}
		private int bytesDone, bytesTotal, bytesDownloadTotal;
	}
	
	private final AsyncTask<String, DLProgress, Integer> downloadTask = new AsyncTask<String, DLProgress, Integer>() {
		private GZIPInputStream unzippedData;
		private InputStream response;
		private URLConnection connection;
		
		@Override
		protected Integer doInBackground(String... params) {
			if(params.length != 1) throw new IllegalArgumentException("Please specify 1 parameter");
			URL downloadURL;
			try {
				downloadURL = new URL(params[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return STATUS_DL_FAIL_MALFORMED_FILE;
			}
			try {
				connection = downloadURL.openConnection();
				response = connection.getInputStream();
				unzippedData = new GZIPInputStream(response, 0x40000); // Change buffer size?
			} catch (IOException e) {
				e.printStackTrace();
				return STATUS_DL_FAIL_IOERROR;
			}
			
			final File sd = getExternalFilesDir(null);
			File debian = new File(sd.getAbsolutePath() + "/" + DEB_IMG);
			try {
				debian.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return STATUS_DL_FAIL_SDERROR;
			}
			
			// Delete version file
			File oldversion = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
			oldversion.delete();
			
			FileOutputStream debWriter = null;
			try {
				debWriter = new FileOutputStream(debian);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			byte[] dlData = new byte[0x40000];
			int bytesRead = 0;
			int bytesDone = 0;
			DLProgress progress = new DLProgress(0, DEB_IMG_URL_SIZE);
			progress.setBytesDownloadTotal(connection.getContentLength());
			try {
				while((bytesRead = unzippedData.read(dlData)) != -1) {
					bytesDone += bytesRead;
					debWriter.write(dlData, 0, bytesRead);
					progress.setBytesDone(bytesDone);
					publishProgress(progress);
					
					if(isCancelled()) {
						debWriter.close();
						unzippedData.close();
						response.close();
						// Remove old files
						File delversion = new File(sd.getAbsolutePath() + "/" + DEB_VERSION_FILE);
						delversion.delete();
						File deldebian = new File(sd.getAbsolutePath() + "/" + DEB_IMG);
						deldebian.delete();
						return STATUS_DL_CANCEL;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					debWriter.close();
					unzippedData.close();
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return STATUS_DL_FAIL_DLERROR;
			}
			
			try {
				debWriter.close();
				unzippedData.close();
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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
			
			Log.i(TAG, "Finished download");
			return STATUS_DL_SUCCESS;
		}
		protected void onProgressUpdate(DLProgress... progress) {
			status = STATUS_DOWNLOADING;
			dlProgress = progress[0];
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
				unzippedData.close();
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
