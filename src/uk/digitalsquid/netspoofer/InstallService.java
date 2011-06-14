package uk.digitalsquid.netspoofer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import android.widget.Toast;

public class InstallService extends Service implements Config {
	public static final String INTENT_EXTRA_STATUS = "uk.digitalsquid.netspoofer.InstallService.status";
	public static final String INTENT_EXTRA_DLPROGRESS = "uk.digitalsquid.netspoofer.InstallService.dlprogress";
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.netspoofer.config.ConfigChecker.StatusUpdate";
	public static final int STATUS_STARTED = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_FINISHED = 2;
	
	public static final int STATUS_DL_SUCCESS = 0;
	public static final int STATUS_DL_FAIL_MALFORMED_FILE = 1;
	public static final int STATUS_DL_FAIL_IOERROR = 2;
	public static final int STATUS_DL_FAIL_SDERROR = 3;
	public static final int STATUS_DL_FAIL_DLERROR = 4;
	
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
			break;
		}
		sendBroadcast(intent);
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
		private final int bytesDone, bytesTotal;
	}
	
	private final AsyncTask<String, DLProgress, Integer> downloadTask = new AsyncTask<String, DLProgress, Integer>() {
		@Override
		protected Integer doInBackground(String... params) {
			if(params.length != 1) throw new IllegalArgumentException("Please specify 1 parameter");
			URL downloadURL;
			try {
				downloadURL = new URL(params[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Toast.makeText(
						InstallService.this,
						"Couldn't locate download file '" + params[0] + "'. Please report this as a bug.",
						Toast.LENGTH_LONG).show();
				return STATUS_DL_FAIL_MALFORMED_FILE;
			}
			GZIPInputStream unzippedData;
			InputStream response;
			try {
				URLConnection connection = downloadURL.openConnection();
				response = connection.getInputStream();
				unzippedData = new GZIPInputStream(response, 0x40000); // Change buffer size?
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(
						InstallService.this,
						"Couldn't download file. Are you connected to the internet?",
						Toast.LENGTH_LONG).show();
				return STATUS_DL_FAIL_IOERROR;
			}
			
			final File sd = getExternalFilesDir(null);
			File debian = new File(sd.getAbsolutePath() + "/" + DEB_IMG);
			try {
				debian.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(
						InstallService.this,
						"Couldn't open file for writing on SD card",
						Toast.LENGTH_LONG).show();
				return STATUS_DL_FAIL_SDERROR;
			}
			
			FileOutputStream debWriter = null;
			try {
				debWriter = new FileOutputStream(debian);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			byte[] dlData = new byte[0x40000];
			int bytesRead = 0;
			try {
				while((bytesRead = unzippedData.read(dlData)) != 0) {
					debWriter.write(dlData, 0, bytesRead);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(
						InstallService.this,
						"An error ocurred whilst downloading the file. Please try again",
						Toast.LENGTH_LONG).show();
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
			return STATUS_DL_SUCCESS;
		}
		protected void onProgressUpdate(DLProgress... progress) {
			status = STATUS_DOWNLOADING;
			broadcastStatus();
		}
		protected void onPostExecute(Integer result) {
			status = STATUS_FINISHED;
			dlstatus = result;
			broadcastStatus();
		}
	};
}
