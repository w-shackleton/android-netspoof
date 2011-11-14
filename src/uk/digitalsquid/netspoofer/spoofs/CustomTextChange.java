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

import java.util.HashMap;
import java.util.Map;

import uk.digitalsquid.netspoofer.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author william
 *
 */
public class CustomTextChange extends SquidScriptSpoof {
	private static final long serialVersionUID = 8490503138296852028L;

	public CustomTextChange() {
		super("Text change", "Change all text on all websites", "textchange.sh");
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
	
	private final void setValue(boolean old, int position, String value) {
		if(old) {
			changeValues.put(String.format("TEXT%dOLD", position), value);
		} else {
			changeValues.put(String.format("TEXT%dNEW", position), value);
		}
	}
	
	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		final LayoutInflater inflater = LayoutInflater.from(context);
		final ScrollView view = (ScrollView)inflater.inflate(R.layout.customtextdialog, null);
		
		builder.setMessage(R.string.customTextDesc);
		
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Iterate through all
				for(int i = 0; i < 8; i++) {
					String from = ((TextView)view.findViewById(froms[i])).getText().toString();
					String to = ((TextView)view.findViewById(tos[i])).getText().toString();
					
					setValue(true, i, from);
					setValue(false, i, to);
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
}
