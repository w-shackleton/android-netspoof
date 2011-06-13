package uk.digitalsquid.netspoofer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import uk.digitalsquid.netspoofer.config.ConfigChecker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class NetSpoof extends Activity implements OnClickListener {
	/**
	 * A dialog to tell the user to mount their SD card.
	 */
	static final int DIALOG_R_SD = 1;
	/**
	 * A dialog to tell the user to mount their SD card rw.
	 */
	static final int DIALOG_W_SD = 2;
	static final int DIALOG_ROOT = 3;

	private SharedPreferences prefs;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if(ConfigChecker.checkInstalled(getApplicationContext())) findViewById(R.id.startButton).setEnabled(true);
		findViewById(R.id.startButton).setOnClickListener(this);
		findViewById(R.id.setupButton).setOnClickListener(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if(!ConfigChecker.getSDStatus(false)) {
			showDialog(DIALOG_R_SD);
		} else {
			if(!ConfigChecker.getSDStatus(true)) {
				showDialog(DIALOG_R_SD);
			}
			firstTimeSetup();
		}
	}

	private void firstTimeSetup() {
		final File sd = getExternalFilesDir(null);
		File imgDir = new File(sd, "img");
		if(!imgDir.exists()) if(!imgDir.mkdir()) Toast.makeText(this, "Couldn't create 'img' folder.", Toast.LENGTH_LONG).show();

		Process p;  
		try {
			// Preform su to get root privledges  
			p = Runtime.getRuntime().exec("su");   

			// Attempt to write a file to a root-only  
			DataOutputStream os = new DataOutputStream(p.getOutputStream());  

			// Close the terminal  
			os.writeBytes("exit\n");  
			os.flush();  
			try {  
				p.waitFor();  
				if (p.exitValue() != 255) {  
					// TODO Code to run on success  
				}  
				else {  
					showDialog(DIALOG_ROOT);
				}  
			} catch (InterruptedException e) {  
				showDialog(DIALOG_ROOT);
			}  
		} catch (IOException e) {  
			showDialog(DIALOG_ROOT);
		}  
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.setupButton:
				startActivity(new Intent(this, SetupStatus.class));
				break;
			case R.id.startButton:
				break;
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder;
		switch(id) {
			case DIALOG_R_SD:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please connect SD card / exit USB mode to continue.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							NetSpoof.this.finish();
						}
					});
				dialog = builder.create();
				break;
			case DIALOG_W_SD:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please set the SD Card to writable. May work without but expect problems.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) { }
					});
				dialog = builder.create();
				break;
			case DIALOG_ROOT:
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Please root your phone before using this application. Search the internet for instructions on how to do this for your phone.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							NetSpoof.this.finish();
						}
					});
				dialog = builder.create();
				break;
		}
		return dialog;
	}
}
