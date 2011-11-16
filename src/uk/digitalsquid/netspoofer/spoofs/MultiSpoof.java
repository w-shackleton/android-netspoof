package uk.digitalsquid.netspoofer.spoofs;

import java.util.ArrayList;

import uk.digitalsquid.netspoofer.MultiSpoofDialogRunner;
import uk.digitalsquid.netspoofer.SpoofSelector;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

public class MultiSpoof extends Spoof {

	public MultiSpoof() {
		super("Multiple spoofs", "Run multiple spoofs at once. May run slowly.");
	}
	
	private ArrayList<Spoof> selectedSpoofs;
	private ArrayList<SquidScriptSpoof> finalSpoofs;

	private static final long serialVersionUID = -848683524539301592L;

	@Override
	public Intent activityForResult(Context context) {
		Intent ret = new Intent(context, SpoofSelector.class);
		ret.setAction(Intent.ACTION_PICK);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean activityFinished(Context context, Intent intent) {
		selectedSpoofs = (ArrayList<Spoof>) intent.getSerializableExtra("uk.digitalsquid.netspoof.SpoofSelector.spoofs");
		return true;
	}

	@Override
	public Intent activityForResult2(Context context) {
		Intent ret = new Intent(context, MultiSpoofDialogRunner.class);
		ArrayList<SquidScriptSpoof> filteredSpoofs = new ArrayList<SquidScriptSpoof>();
		for(Spoof spoof : selectedSpoofs) {
			if(spoof instanceof SquidScriptSpoof) {
				filteredSpoofs.add((SquidScriptSpoof) spoof);
			}
		}
		ret.putExtra(MultiSpoofDialogRunner.SPOOF_LIST, filteredSpoofs);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean activityFinished2(Context context, Intent result) {
		finalSpoofs = (ArrayList<SquidScriptSpoof>) result.getSerializableExtra(MultiSpoofDialogRunner.SPOOF_LIST);
		return true;
	}

	@Override
	public String getSpoofCmd(String victim, String router) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStopCmd() {
		return "\n";
	}

	@Override public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) { return null; }
}
