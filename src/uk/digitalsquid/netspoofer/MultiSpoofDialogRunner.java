/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer;

import java.util.ArrayList;
import java.util.Iterator;

import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.Spoof.OnExtraDialogDoneListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

/**
 * Displays the dialogs and configuration options for multiple spoofs.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class MultiSpoofDialogRunner extends Activity implements OnExtraDialogDoneListener {
	public static final String SPOOF_LIST = "uk.digitalsquid.netspoofer.MultiSpoofDialogRunner.list";
	
	ArrayList<Spoof> spoofs;
	Iterator<Spoof> currentSpoof;
	Spoof spoof;
	
	Dialog currentDialog;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spoofs = (ArrayList<Spoof>) getIntent().getSerializableExtra(SPOOF_LIST);
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
