package uk.digitalsquid.netspoofer.proxy;

import java.util.HashMap;
import java.util.List;

import uk.digitalsquid.netspoofer.config.Lists;

public class HTTPMessage {
	private HashMap<String, List<String>> headers =
			new HashMap<String, List<String>>();
	
	/**
	 * Adds a header to this {@link HTTPMessage}.
	 * @param header
	 * @return <code>true</code> if successful
	 */
	public boolean addHeader(String header) {
		int colon = header.indexOf(':');
		if(colon == -1) return false;
		String key = header.substring(0, colon).trim();
		String val = header.substring(colon+1).trim();
		addHeader(key, val);
		return true;
	}
	public void addHeader(String key, String val) {
		if(headers.containsKey(key))
			headers.get(key).add(val);
		else
			headers.put(key, Lists.singleton(val));
	}
}
