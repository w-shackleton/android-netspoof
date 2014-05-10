package uk.digitalsquid.netspoofer.proxy;

import java.util.StringTokenizer;

import android.net.Uri;

public class HttpRequest extends HttpMessage {
	private String method;
	private String path;
	private String version;

	public boolean setRequestLine(String line) {
		StringTokenizer tkz = new StringTokenizer(line, " ");
		if(!tkz.hasMoreTokens()) return false;
		method = tkz.nextToken();
		if(!tkz.hasMoreTokens()) return false;
		path = tkz.nextToken();
		if(!tkz.hasMoreTokens()) return false;
		version = tkz.nextToken();
		return true;
	}
	public String getRequestLine() {
		return String.format("%s %s %s", method, path, version);
	}
	
	public String getMethod() { return method; }
	public void setMethod(String method) { this.method = method; }
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }
	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }
	
	@Override
	public String toString() {
		return String.format("%s\r\n%s",
				getRequestLine(),
				getHeaders());
				
	}
	public Uri getUri() {
		return Uri.parse(String.format("http://%s/%s", getHost(), getPath()));
	}
	public void setUri(Uri uri) {
		setHost(uri.getHost());
		// TODO: Check this is correct
		setPath(uri.getEncodedPath());
	}
	
	private boolean ignoreResponse = false;
	
	/**
	 * If this method is called at any point then an empty response will be
	 * returned; no request is actually made. This allows a spoof to completely
	 * rewrite a response with less overhead.
	 */
	public void returnNullResponse() {
		ignoreResponse = true;
	}
	/**
	 * If <code>true</code> indicates that no request should be made; a blank 
	 * response should be returned.
	 */
	public boolean shouldIgnoreResponse() {
		return ignoreResponse;
	}
	
	@Override
	public void reset() {
		super.reset();
		method = "";
		path = "";
		version = "";
		ignoreResponse = false;
	}
}
