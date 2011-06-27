package uk.digitalsquid.netspoofer.spoofs;

import java.io.Serializable;

import android.app.Dialog;
import android.content.Context;

public abstract class Spoof implements Serializable {
	private static final long serialVersionUID = -3207729013734241941L;
	
	private final String title, description;
	
	public Spoof(String title, String description) {
		this.description = description;
		this.title = title;
	}

	public abstract String getSpoofCmd(String victim, String router);
	public abstract String getStopCmd();
	
	public abstract Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone);
	
	public String getDescription() {
		return description;
	}
	
	public String getTitle() {
		return title;
	}

	public static interface OnExtraDialogDoneListener {
		void onDone();
	}
}
