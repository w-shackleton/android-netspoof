package uk.digitalsquid.netspoofer.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import uk.digitalsquid.netspoofer.spoofs.Spoof;

import com.wpg.proxy.HttpMessageHandler;
import com.wpg.proxy.HttpMessageRequest;
import com.wpg.proxy.HttpMessageResponse;
import com.wpg.proxy.Proxy;

public class NSProxy extends Proxy {
	
	public static final int PROXY_PORT = 4123;
	
	protected final List<Spoof> spoofs;

	public NSProxy(List<Spoof> spoofs) throws UnknownHostException {
		super(InetAddress.getByAddress("0.0.0.0", new byte[] {0,0,0,0}), PROXY_PORT, 100);
		this.spoofs = spoofs;
	}
	
	protected HttpMessageHandler messageHandler = new HttpMessageHandler() {
		
		@Override
		public void receivedResponse(HttpMessageResponse response,
				HttpMessageRequest request) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void receivedRequest(HttpMessageRequest request) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void failedResponse(HttpMessageResponse response,
				HttpMessageRequest request, Exception exception) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void failedRequest(HttpMessageRequest request, Exception exception) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void failed(Exception exception) {
			// TODO Auto-generated method stub
			
		}
	};
}
