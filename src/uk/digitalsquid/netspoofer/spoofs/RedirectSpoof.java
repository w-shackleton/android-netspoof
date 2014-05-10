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

import java.net.UnknownHostException;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * A spoof which redirects a website to another.
 * @author william
 *
 */
public class RedirectSpoof extends Spoof implements LogConf {
	private static final long serialVersionUID = -7780822391880161592L;
	
	public static final int MODE_BLUEBALL = 1;
	public static final int MODE_CUSTOM = 2;
	
	private String host;

	private static String getTitle(Context context, int mode) {
		switch(mode) {
		case MODE_BLUEBALL:
			return context.getResources().getString(R.string.spoof_blueball);
		case MODE_CUSTOM:
			return context.getResources().getString(R.string.spoof_redirect_custom);
		default:
			return "Unknown image spoof";
		}
	}
	private static String getDescription(Context context, int mode) {
		switch(mode) {
		case MODE_BLUEBALL:
			return context.getResources().getString(R.string.spoof_blueball_description);
		case MODE_CUSTOM:
			return context.getResources().getString(R.string.spoof_redirect_custom_description);
		default:
			return "";
		}
	}
	
	
	public RedirectSpoof(Context context, int mode) throws UnknownHostException {
		super(getTitle(context, mode), getDescription(context, mode));
		switch(mode) {
		case MODE_BLUEBALL:
			host = "http://blueballfixed.ytmnd.com/";
			break;
		}
	}
	
	/**
	 * Constructor that leaves host undefined, and shows dialog later.
	 * @param title
	 * @param description
	 * @throws UnknownHostException 
	 */
	public RedirectSpoof(String title, String description) {
		super(title, description);
	}
	
	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		if(host == null) {
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			final Dialog dialog = new Dialog(context);
	
			dialog.setTitle("Website redirect");
			
			dialog.setContentView(R.layout.iptextfield);
			
			final TextView input = (TextView) dialog.findViewById(R.id.text);
			input.setText(prefs.getString("redirectUrl", ""));
			final View[] progressParts = new View[] {
					dialog.findViewById(R.id.progress),
					dialog.findViewById(R.id.status) };
			final Button
					ok = (Button) dialog.findViewById(R.id.ok),
					cancel = (Button) dialog.findViewById(R.id.cancel);

			String userEntry = input.getText().toString();
			if(userEntry.equals("")) userEntry = "http://blueballfixed.ytmnd.com/";
			prefs.edit().putString("redirectUrl", userEntry).commit();
			
			host = userEntry;
			
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
					onDone.onDone();
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
	
			return dialog;
		}
		else return null;
	}
	@Override
	public void modifyRequest(HttpRequest request) {
	}
	@Override
	public void modifyResponse(HttpResponse response, HttpRequest request) {
		if(response.getContentType().equalsIgnoreCase("text/html")) {
			response.reset();
			response.setResponseCode(301);
			response.setResponseMessage("Moved Permanently");
			response.addHeader("Location", host);
		}
	}
}
