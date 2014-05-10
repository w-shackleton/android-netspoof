package uk.digitalsquid.netspoofer.proxy;

import java.util.List;

public class HttpResponse extends HttpMessage {
	private int responseCode;
	private String responseMessage;
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	// A few functions for getting certain headers
	public String getContentType() {
		List<String> result = headers.get("content-type");
		if(result == null) return "";
		return result.get(0);
	}
	
	@Override
	public void reset() {
		super.reset();
		responseCode = 0;
		responseMessage = "";
	}
}
