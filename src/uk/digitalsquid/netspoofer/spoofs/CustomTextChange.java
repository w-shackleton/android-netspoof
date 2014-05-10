/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
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

import java.util.HashMap;
import java.util.Map;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class CustomTextChange extends Spoof {
	private static final long serialVersionUID = 8490503138296852028L;

	public CustomTextChange() {
		// TODO: Localise
		super("Text change", "Change all text on all websites");
	}
	
	private final Map<String, String> changeValues = new HashMap<String, String>(8);
	
	private static final int[] froms = {
		R.id.textFrom1,
		R.id.textFrom2,
		R.id.textFrom3,
		R.id.textFrom4,
		R.id.textFrom5,
		R.id.textFrom6,
		R.id.textFrom7,
		R.id.textFrom8,
	};
	private static final int[] tos = {
		R.id.textTo1,
		R.id.textTo2,
		R.id.textTo3,
		R.id.textTo4,
		R.id.textTo5,
		R.id.textTo6,
		R.id.textTo7,
		R.id.textTo8,
	};
	
	/**
	 * Sets the value in the environment map, and also saves to shared prefs.
	 * @param old
	 * @param position
	 * @param value
	 */
	private final void setValue(SharedPreferences prefs, boolean old, int position, String value) {
		if(old) {
			final String key = String.format("TEXT%dOLD", position);
			changeValues.put(key, value);
			prefs.edit().putString(key, value).commit();
		} else {
			final String key = String.format("TEXT%dNEW", position);
			changeValues.put(key, value);
			prefs.edit().putString(key, value).commit();
		}
	}
	
	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		final LayoutInflater inflater = LayoutInflater.from(context);
		final ScrollView view = (ScrollView)inflater.inflate(R.layout.customtextdialog, null);
		
		// Iterate through all
		for(int i = 0; i < 8; i++) {
			String from = prefs.getString(String.format("TEXT%dOLD", i), "");
			String to = prefs.getString(String.format("TEXT%dNEW", i), "");
			((TextView)view.findViewById(froms[i])).setText(from);
			((TextView)view.findViewById(tos[i])).setText(to);
		}
		
		
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Iterate through all
				for(int i = 0; i < 8; i++) {
					String from = ((TextView)view.findViewById(froms[i])).getText().toString();
					String to = ((TextView)view.findViewById(tos[i])).getText().toString();
					
					setValue(prefs, true, i, from);
					setValue(prefs, false, i, to);
				}
				
				onDone.onDone();
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).setTitle(R.string.customText);
		return builder.create();
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		return changeValues;
	}

	@Override
	public void modifyRequest(HttpRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyResponse(HttpResponse response, HttpRequest request) {
	}
}
