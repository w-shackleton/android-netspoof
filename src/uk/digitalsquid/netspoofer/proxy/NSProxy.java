package uk.digitalsquid.netspoofer.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import android.util.Log;

import com.wpg.proxy.HttpMessageHandler;
import com.wpg.proxy.HttpMessageRequest;
import com.wpg.proxy.HttpMessageResponse;
import com.wpg.proxy.Proxy;
import com.wpg.proxy.ProxyProcessor;
import com.wpg.proxy.ProxyRegistry;

public class NSProxy extends Proxy implements LogConf {
	
	public static final int PROXY_PORT = 3128;
	
	protected final List<Spoof> spoofs;

	public NSProxy(List<Spoof> spoofs) throws UnknownHostException {
		super(InetAddress.getByAddress("0.0.0.0", new byte[] {0,0,0,0}), PROXY_PORT, 100);
		this.spoofs = spoofs;
		ProxyRegistry.addHandler(messageHandler);
	}
	
	protected HttpMessageHandler messageHandler = new HttpMessageHandler() {
		
		@Override
		public void receivedResponse(HttpMessageResponse response,
				HttpMessageRequest request) {
			for(Spoof spoof : spoofs)
				spoof.modifyResponse(response, request);
		}
		
		@Override
		public void receivedRequest(HttpMessageRequest request) {
			for(Spoof spoof : spoofs)
				spoof.modifyRequest(request);
			Log.v(TAG, "Request: " + request.getUri());
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
