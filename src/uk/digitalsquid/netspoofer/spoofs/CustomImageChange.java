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
public class CustomImageChange extends SquidScriptSpoof {
	private static final long serialVersionUID = 8490503138296852028L;

	public CustomImageChange() {
		super("Custom image change", "Change all images on all websites", "trollface.sh");
	}
	
	private String customImageURL;
	
	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Image URL");
		alert.setMessage("Enter the URL of the image you would like to replace all images with.");

		final EditText input = new EditText(context);
		input.setText(prefs.getString("imageChangeUrl", "http://"));
		alert.setView(input);

		alert.setPositiveButton("Done", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				customImageURL = input.getText().toString();
				prefs.edit().putString("imageChangeUrl", customImageURL).commit();
				onDone.onDone();
			}
		});

		return alert.create();
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		ret.put("SPOOFIMAGEURL", customImageURL);
		return ret;
	}
}
