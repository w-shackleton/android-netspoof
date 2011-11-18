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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A spoof which redirects a website to another.
 * @author william
 *
 */
public class IPRedirectSpoof extends SquidScriptSpoof implements LogConf {
	private static final long serialVersionUID = -7780822391880161592L;
	public static final String KITTENWAR = "kittenwar.com";
	
	private InetAddress host;
	
	public IPRedirectSpoof(String title, String description, String hostTo) throws UnknownHostException {
		super(title, description, "redirect.sh");
		if(hostTo == null) {
			host = null;
			return;
		}
		host = InetAddress.getByName(hostTo);
	}
	
	/**
	 * Constructor that leaves host undefined, and shows dialog later.
	 * @param title
	 * @param description
	 * @throws UnknownHostException 
	 */
	public IPRedirectSpoof(String title, String description) {
		super(title, description, "redirect.sh");
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
			
			// Run this in BG to make UX better
			final AsyncTask<Void, Void, InetAddress> bg = new AsyncTask<Void, Void, InetAddress>() {
				@Override
				protected void onPreExecute() {
					userEntry = input.getText().toString();
				}
				
				String userEntry;
				
				@Override
				protected InetAddress doInBackground(Void... params) {
					InetAddress host = null;
					try {
						if(userEntry.equals("")) throw new UnknownHostException("Blank host");
						host = InetAddress.getByName(userEntry);
						prefs.edit().putString("redirectUrl", userEntry).commit();
					} catch (UnknownHostException e) {
						try {
							host = InetAddress.getByName(KITTENWAR);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						}
					}
					return host;
				}
				@Override
				protected void onPostExecute(InetAddress addr) {
					if(!isCancelled()) {
						if(addr == null) {
							Toast.makeText(context, "Couldn't find specified website, using kittenwar.", Toast.LENGTH_LONG).show();
						}
						host = addr;
						dialog.dismiss();
						onDone.onDone();
					}
				}
			};
			
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					for(View v : progressParts) {
						v.setVisibility(View.VISIBLE);
					}
					bg.execute();
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bg.cancel(false);
					dialog.cancel();
				}
			});
	
			return dialog;
		}
		else return null;
	}
	
	/**
	 * Adds redirect env variable
	 */
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		if(host != null) ret.put("REDIRECTURL", host.getHostName());
		else Log.e(TAG, "Entered URL not here, probably a non-existent website.");
		return ret;
	}
}
