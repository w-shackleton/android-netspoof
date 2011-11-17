package uk.digitalsquid.netspoofer.spoofs;

import java.util.ArrayList;
import java.util.Map;

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
	
	private static final String BASE_REWRITE_URL = "/rewriters/";

	@Override
	public String getSpoofCmd(String victim, String router) {
		if(finalSpoofs == null) return null;
		final StringBuilder cmdBuilder = new StringBuilder();
		cmdBuilder.append("spoof %s %s 3 \"");
		boolean first = true;
		for(SquidScriptSpoof spoof : finalSpoofs) {
			// Leaving no spaces in script def
			if(!first) {
				cmdBuilder.append('|');
			} else {
				first = false;
			}
			cmdBuilder.append(BASE_REWRITE_URL);
			cmdBuilder.append(spoof.getScriptName());
		}
		cmdBuilder.append('"');
		return String.format(cmdBuilder.toString(), victim, router);
	}

	@Override
	public String getStopCmd() {
		return "\n";
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		if(finalSpoofs != null) {
			for(SquidScriptSpoof spoof : finalSpoofs) {
				ret.putAll(spoof.getCustomEnv());
			}
		}
		return ret;
	}

	@Override public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) { return null; }
}
