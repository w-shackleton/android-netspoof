package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * <p>Selects a Youtube video. Currently uses the mobile site.</p>
 * 
 * <p>Youtube '?v=' code returned in {@link YoutubeSelector.CODE}</p>
 * @author william
 *
 */
public class YoutubeSelector extends Activity implements LogConf {
	
	/**
	 * The extra the YT code is returned in.
	 */
	public static final String CODE = "code";
	
	WebView web;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube);
		
		web = (WebView) findViewById(R.id.web);
        
		web.setWebViewClient(wvc);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.loadUrl("http://m.youtube.com");
		
	}
	
	WebViewClient wvc = new WebViewClient() {
		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
			if(url.contains("/watch")) { // Probably is a video page
				Uri uri = Uri.parse(url);
				try {
					String videoID = uri.getQueryParameter("v"); // V is the video ID
					if(videoID != null) { // Send back result
						Intent intent = new Intent();
						intent.putExtra(CODE, videoID);
						YoutubeSelector.this.setResult(RESULT_OK, intent);
						
						Log.i(TAG, "Found video " + videoID + " at " + url);
						Toast.makeText(getApplicationContext(), "Got video", Toast.LENGTH_LONG).show();
						
						YoutubeSelector.this.finish();
					}
				} catch(UnsupportedOperationException e) { }
			}
		}
		
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.youtube, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.goback:
	        web.loadUrl("http://m.youtube.com");
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
