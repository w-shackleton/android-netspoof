package uk.digitalsquid.netspoofer;

import java.util.ArrayList;
import java.util.Iterator;

import uk.digitalsquid.netspoofer.spoofs.Spoof.OnExtraDialogDoneListener;
import uk.digitalsquid.netspoofer.spoofs.SquidScriptSpoof;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

public class MultiSpoofDialogRunner extends Activity implements OnExtraDialogDoneListener {
	public static final String SPOOF_LIST = "uk.digitalsquid.netspoofer.MultiSpoofDialogRunner.list";
	
	ArrayList<SquidScriptSpoof> spoofs;
	Iterator<SquidScriptSpoof> currentSpoof;
	SquidScriptSpoof spoof;
	
	Dialog currentDialog;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spoofs = (ArrayList<SquidScriptSpoof>) getIntent().getSerializableExtra(SPOOF_LIST);
		currentSpoof = spoofs.iterator();
		
		processNextSpoof(false);
	}
	
	private static final int ITERATED_ACTIVITY_ID = 1;
	
	private int dialogId = 0;
	
	/**
	 * Processes the next spoof in the iterator.
	 * @param processActivities If <code>true</code>, does activities. Otherwise, does dialogs.
	 */
	void processNextSpoof(boolean processActivities) {
		if(!processActivities) {
			if(currentSpoof.hasNext()) {
				spoof = currentSpoof.next();
				Dialog d = spoof.displayExtraDialog(this, this);
				if(d != null) {
					currentDialog = d;
					showDialog(dialogId++);
				} else {
					processNextSpoof(false); // Too much Haskell programming = using recursion too much...
				}
			} else {
				currentSpoof = spoofs.iterator();
				processNextSpoof(true);
			}
		} else { // ACTIVITY processing
			if(currentSpoof.hasNext()) {
				spoof = currentSpoof.next();
				Intent i = spoof.activityForResult(getBaseContext());
				if(i != null) {
					startActivityForResult(i, ITERATED_ACTIVITY_ID);
				} else {
					processNextSpoof(true);
				}
			} else {
				completeProcess();
			}
		}
	}

	@Override
	public void onDone() {
		processNextSpoof(false);
	}
	
	// Using this as it allows any form of dialog
	@Override
	public Dialog onCreateDialog(int id, Bundle args) {
		return currentDialog;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case ITERATED_ACTIVITY_ID:
			switch(resultCode) {
			case RESULT_OK:
				spoof.activityFinished(getBaseContext(), intent);
				processNextSpoof(true);
				break;
			default:
				cancelProcess();
			}
			break;
		}
	}
	
	/**
	 * Cancels the config
	 */
	void cancelProcess() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	/**
	 * Completes the config
	 */
	void completeProcess() {
		Intent i = new Intent();
		i.putExtra(SPOOF_LIST, spoofs); // Send list back under same name.
		setResult(RESULT_OK, i);
		finish();
	}
}
