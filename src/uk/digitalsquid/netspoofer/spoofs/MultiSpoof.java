/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
 * Copyright (C) 2011 Will Shackleton
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

package uk.digitalsquid.netspoofer.spoofs;

import java.util.ArrayList;
import java.util.Map;

import uk.digitalsquid.netspoofer.MultiSpoofDialogRunner;
import uk.digitalsquid.netspoofer.SpoofSelector;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

/**
 * A spoof which runs multiple other spoofs, and concatenates them by piping the scripts together.
 * @author william
 *
 */
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
