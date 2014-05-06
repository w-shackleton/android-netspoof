package uk.digitalsquid.netspoofer.spoofs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

public class NullSpoof extends Spoof {

	private static final long serialVersionUID = 3975389786995524404L;

	public NullSpoof() {
		super("Man-in-the-middle", "Redirect traffic but don't change anything");
	}

	@Override
	public Dialog displayExtraDialog(Context context,
			OnExtraDialogDoneListener onDone) {
		return null;
	}

	@Override
	public Intent activityForResult(Context context) {
		return null;
	}

	@Override
	public boolean activityFinished(Context context, Intent result) {
		return false;
	}

}
