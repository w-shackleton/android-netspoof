/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

public class SquidScriptSpoof extends Spoof {
	private static final long serialVersionUID = 52887789907180627L;
	
	private final String scriptName;
	
	public SquidScriptSpoof(String title, String description, String scriptName) {
		super(title, description);
		this.scriptName = scriptName;
	}

	@Override
	public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) {
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

	public String getScriptName() {
		return scriptName;
	}
}
