package uk.digitalsquid.netspoofer.proxy;

import java.util.StringTokenizer;

public class HttpRequest extends HttpMessage {
	private String method;
	private String url;
	private String version;

	public boolean setRequestLine(String line) {
		StringTokenizer tkz = new StringTokenizer(line, " ");
		if(!tkz.hasMoreTokens()) return false;
		method = tkz.nextToken();
		if(!tkz.hasMoreTokens()) return false;
		url = tkz.nextToken();
		if(!tkz.hasMoreTokens()) return false;
		version = tkz.nextToken();
		return true;
	}
	public String getRequestLine() {
		return String.format("%s %s %s", method, url, version);
	}

	public String getMethod() { return method; }
	public void setMethod(String method) { this.method = method; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }
	
	@Override
	public String toString() {
		return String.format("%s\r\n%s",
				getRequestLine(),
				getHeaders());
				
	}
}
