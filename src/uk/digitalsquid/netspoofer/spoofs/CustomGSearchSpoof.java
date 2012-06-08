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

import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.EditText;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author william
 *
 */
public class CustomGSearchSpoof extends SquidScriptSpoof {
	private static final long serialVersionUID = 8490503138296852028L;

	public CustomGSearchSpoof() {
		super("Custom Google search change", "Change the text in google searches", "googlemod.sh");
	}
	
	private String customFilter;
	
	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Search change pattern");
		alert.setMessage("Enter the text to change the google search to. Put '%' to enter the original search query.");

		final EditText input = new EditText(context);
		alert.setView(input);
		input.setText(prefs.getString("gSearchText", ""));

		alert.setPositiveButton("Done", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				customFilter = input.getText().toString().replace('\n', '+').replace('\t', '+').replace(' ', '+');
				prefs.edit().putString("gSearchText", input.getText().toString()).commit();
				onDone.onDone();
			}
		});

		return alert.create();
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		ret.put("GSEARCHSTRING", customFilter);
		return ret;
	}
}
