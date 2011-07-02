package uk.digitalsquid.netspoofer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class About extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		findViewById(R.id.website).setOnClickListener(this);
		findViewById(R.id.devsite).setOnClickListener(this);
		findViewById(R.id.contactDev).setOnClickListener(this);
		findViewById(R.id.reportBug).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()) {
		case R.id.website:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk"));
			startActivity(intent);
			break;
		case R.id.devsite:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk"));
			startActivity(intent);
			break;
		case R.id.contactDev:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/contact"));
			startActivity(intent);
			break;
		case R.id.reportBug:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://URLFORREPORTBUG/"));
			startActivity(intent);
			break;
		}
	}
}
