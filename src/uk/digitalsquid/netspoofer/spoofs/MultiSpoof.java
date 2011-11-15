package uk.digitalsquid.netspoofer.spoofs;

import uk.digitalsquid.netspoofer.SpoofSelector;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

public class MultiSpoof extends Spoof {

	public MultiSpoof() {
		super("Multiple spoofs", "Run multiple spoofs at once. May run slowly.");
	}

	private static final long serialVersionUID = -848683524539301592L;

	@Override
	public Intent activityForResult(Context context) {
		Intent ret = new Intent(context, SpoofSelector.class);
		ret.setAction(Intent.ACTION_PICK);
		return ret;
	}
	
	public boolean activityFinished(Intent intent) {
		return false;
	}

	@Override
	public String getSpoofCmd(String victim, String router) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStopCmd() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) { return null; }
}
