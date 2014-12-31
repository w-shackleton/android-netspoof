/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
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

package uk.digitalsquid.netspoofer.proxy;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.misc.MagicInputStream;
import uk.digitalsquid.netspoofer.spoofs.Spoof;

public class NSProxy implements LogConf {
	
	public static final int PROXY_PORT = 3128;
	
	protected final List<Spoof> spoofs;

	public NSProxy(List<Spoof> spoofs) {
		this.spoofs = spoofs;
		
	}
	
	private static final int LAUNCH_FAIL = 1;
	
	@SuppressLint("NewApi")
	public void start() {
		if(Build.VERSION.SDK_INT <= 10)
			launchTask.execute();
		else
			launchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void stop() {
		launchTask.cancel(true);
		try {
			ss.close();
		} catch (IOException e) { }
	}
	
	private ServerSocket ss;
	
	private AsyncTask<Void, Void, Integer> launchTask =
			new AsyncTask<Void, Void, Integer> () {

		@SuppressLint("NewApi")
		@Override
		protected Integer doInBackground(Void... arg0) {
			try {
				ss = new ServerSocket(3128);
				while(!isCancelled()) {
					StartParams params = new StartParams();
					params.socket = ss.accept();
					Log.v(TAG, "New socket accepted");
					
					ProxyTask task = new ProxyTask(params);
					task.start();
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket accepting failed", e);
				return LAUNCH_FAIL;
			} finally {
				if(ss != null)
					try {
						ss.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed to close proxy socket", e);
					}
			}
			return 0;
		}
	};
	
	/**
	 * Parameters for starting a {@link ProxyTask}.
	 * @author Will Shackleton <will@digitalsquid.co.uk>
	 *
	 */
	static class StartParams {
		Socket socket;
	}
	
	private static final byte[] NEWLINE = new byte[] {'\r','\n'};

    private int num = 1;
	
    // TODO: Use a thread pool here.
	private class ProxyTask extends Thread {
		
		private final StartParams params;
		
		public ProxyTask(StartParams params) {
			super("Proxy thread " + num++);
			this.params = params;
		}
		
		@Override
		public void run() {
			doInBackground(params);
		}

		protected int doInBackground(StartParams params) {
			Log.i(TAG, "New communication thread started");
			Socket socket = params.socket;
			MagicInputStream input = null;
			BufferedOutputStream output = null;
			try {
				Log.v(TAG, "New connection opened");
				input = new MagicInputStream(
						new BufferedInputStream(socket.getInputStream()));
				output = new BufferedOutputStream(socket.getOutputStream());

				HttpRequest request = new HttpRequest();
				request.setRequestLine(input.readStringLine());
				String header;
				while(!(header = input.readStringLine()).equals("")) {
					request.addHeader(header);
				}
				// Rest is content now.
				if(request.hasHeader("Content-Length")) {
					int len = Integer.parseInt(
							request.getHeader("Content-Length").get(0));
					request.readAllContent(input, len);
				}
				
				// Filter annoying requests
				if(!filterRequest(request)) return 1;
				
				// Manipulate request
				manipulateRequest(request);
				
				// Execute
				try {
					HttpResponse response = executeRequest(request);
					if(!whitelistRequest(request))
						manipulateResponse(response, request);
					output.write(String.format("HTTP/1.1 %d %s\r\n",
							response.getResponseCode(),
							response.getResponseMessage()).getBytes());
					
					for(Entry<String, List<String>> entry :
						response.getHeaderPairs().entrySet()) {
						for(String value : entry.getValue())
							output.write(String.format("%s:%s\r\n",
									entry.getKey(),
									value).getBytes());
					}
					Log.d(TAG, "Writing content");
					output.write(NEWLINE);
					output.write(response.getContent());
					output.flush();
				} catch(HttpExecuteException e) {
					Log.e(TAG, "Failed to execute HTTP request", e);
				} catch(IOException e) {
					Log.e(TAG, "Failed to execute HTTP request", e);
				}
			} catch (IOException e) {
				Log.i(TAG, "Network communications failed for HTTP connection", e);
			} finally {
				if(input != null)
					try {
						output.close();
						input.close();
						socket.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed to close reader for HTTP connection", e);
					}
			}
			return 0;
		}
	}
	
	private void manipulateRequest(HttpRequest request) {
		for(Spoof spoof : spoofs)
			spoof.modifyRequest(request);
	}
	
	private void manipulateResponse(HttpResponse response, HttpRequest request) {
		for(Spoof spoof : spoofs) {
			try {
				spoof.modifyResponse(response, request);
			} catch(Exception e) {
				Log.w(TAG, "Manipulate failed", e);
			}
		}
	}
	
	static {
		HttpURLConnection.setFollowRedirects(false);
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws IOException 
	 * @throws HttpExecuteException
	 * @throws MalformedURLException
	 */
	private HttpResponse executeRequest(HttpRequest request)
			throws IOException {
		if(request.shouldIgnoreResponse())
			return new HttpResponse();
		if(!request.hasHeader("Host"))
			throw new HttpExecuteException("HTTP Host not set");
		String host = request.getHeader("Host").get(0);
		
		URL url = new URL("http", host, 80, request.getPath());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(request.getMethod());
		for(Entry<String, List<String>> entry : request.getHeaderPairs().entrySet()) {
			for(String value : entry.getValue())
				connection.addRequestProperty(entry.getKey(), value);
		}
		
		HttpResponse response = new HttpResponse();
		response.setResponseCode(connection.getResponseCode());
		response.setResponseMessage(connection.getResponseMessage());
		for(Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			for(String value : entry.getValue())
				response.addHeader(entry.getKey(), value);
		}
		// responseType is eg. the 4 in 403
		int responseType = response.getResponseCode() / 100;
		if(responseType == 4 || responseType == 5)
			response.readAllContent(connection.getErrorStream());
		else
			response.readAllContent(connection.getInputStream());
		
		// Add Network Spoofer fingerprint
		response.addHeader("X-Network-Spoofer", "ON");

		return response;
	}
	
	/**
	 * Filters out known annoying requests.
	 * @param request
	 * @return <code>true</code> if the request should continue
	 */
	private boolean filterRequest(HttpRequest request) {
		String host = request.getHost();
		if(host.contains(".dropbox.com")) return false;
		return true;
	}
	
	/**
	 * Filters out requests that shouldn't be modified.
	 * @param request
	 * @return <code>true</code> if the request shouldn't be modified.
	 */
	private boolean whitelistRequest(HttpRequest request) {
		String host = request.getHost();
		if(host.equals("gravityscript.googlecode.com")) return true;
		return false;
	}
}
