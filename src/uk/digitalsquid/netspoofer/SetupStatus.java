package uk.digitalsquid.netspoofer;

import uk.digitalsquid.netspoofer.config.ConfigChecker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SetupStatus extends Activity implements OnClickListener {
	TextView status;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setupstatus);
		status = (TextView) findViewById(R.id.dlStatus);
		findViewById(R.id.dlButton).setOnClickListener(this);
		if(ConfigChecker.isInstallServiceRunning(getApplicationContext()))
			findViewById(R.id.dlButton).setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.dlButton:
			startService(new Intent(getApplicationContext(), InstallService.class));
			v.setEnabled(false);
			status.setText(R.string.dlStarted);
			break;
		}
	}
}
