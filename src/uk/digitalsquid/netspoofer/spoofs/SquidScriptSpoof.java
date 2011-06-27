package uk.digitalsquid.netspoofer.spoofs;

import android.app.Dialog;
import android.content.Context;

public class SquidScriptSpoof extends Spoof {
	private static final long serialVersionUID = 52887789907180627L;
	
	private final String scriptName;
	
	public SquidScriptSpoof(String title, String description, String scriptName) {
		super(title, description);
		this.scriptName = scriptName;
	}

	@Override
	public String getSpoofCmd(String victim, String router) {
		if(victim == null) victim = "*";
		return String.format("spoof %s %s 2 %s", victim, router, scriptName);
	}

	@Override
	public String getStopCmd() {
		return "\n";
	}

	@Override
	public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) {
		return null;
	}

	public String getScriptName() {
		return scriptName;
	}
}
