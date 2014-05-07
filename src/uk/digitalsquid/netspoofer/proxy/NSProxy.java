package uk.digitalsquid.netspoofer.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.http.impl.io.HttpRequestParser;

import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class NSProxy implements LogConf {
	
	public static final int PROXY_PORT = 3128;
	
	protected final List<Spoof> spoofs;

	public NSProxy(List<Spoof> spoofs) {
		this.spoofs = spoofs;
		
	}
	
	private static final int LAUNCH_FAIL = 1;
	
	private AsyncTask<Void, Void, Integer> launchTask =
			new AsyncTask<Void, Void, Integer> () {

		@SuppressLint("NewApi")
		@Override
		protected Integer doInBackground(Void... arg0) {
			try {
				ServerSocket ss = new ServerSocket(3128);
				while(!isCancelled()) {
					StartParams params = new StartParams();
					params.socket = ss.accept();
					
					ProxyTask task = new ProxyTask();
					if(Build.VERSION.SDK_INT <= 10)
						task.execute(params);
					else
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return LAUNCH_FAIL;
			}
			return 0;
		}
	};
	
	/**
	 * Parameters for starting a {@link ProxyTask}.
	 * @author william
	 *
	 */
	static class StartParams {
		Socket socket;
	}
	
	private static class ProxyTask extends AsyncTask<StartParams, Void, Integer> {

		@Override
		protected Integer doInBackground(StartParams... params) {
			Socket socket = params[0].socket;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				String requestLine = reader.readLine();
			} catch (IOException e) {
				Log.i(TAG, "Network communications failed for HTTP connection", e);
			} finally {
				if(reader != null)
					try {
						reader.close();
						socket.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed to close reader for HTTP connection", e);
					}
			}
			return 0;
		}
		
	}
}
