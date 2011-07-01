package uk.digitalsquid.netspoofer.spoofs;

import android.app.Dialog;
import android.content.Context;

public class SimpleScriptedSpoof extends Spoof {
	private static final long serialVersionUID = 6510405899936627809L;
	
	private final String start, stop;

	/**
	 * 
	 * @param title
	 * @param description
	 * @param start Starting debian shell command - must contain 2 %s for IP addresses -
	 * 'all' in victim means arpspoof everyone.
	 * @param stop
	 */
	public SimpleScriptedSpoof(String title, String description, String start, String stop) {
		super(title, description);
		this.start = start;
		this.stop = stop;
	}

	@Override
	public String getSpoofCmd(String victim, String router) {
		return String.format(start, victim, router);
	}

	@Override
	public String getStopCmd() {
		return stop;
	}

	@Override
	public Dialog displayExtraDialog(Context context,
			OnExtraDialogDoneListener onDone) {
		return null;
	}

}
