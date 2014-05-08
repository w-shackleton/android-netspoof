package uk.digitalsquid.netspoofer.proxy;

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
}
