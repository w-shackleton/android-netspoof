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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.EditText;
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
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
	
			alert.setTitle("Website redirect");
			alert.setMessage("Please enter a website to redirect to, not including http:// (eg. kittenwar.com).");
	
			final EditText input = new EditText(context);
			alert.setView(input);
	
			alert.setPositiveButton("Done", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						if(input.getText().toString().equals("")) throw new UnknownHostException("Blank host");
						host = InetAddress.getByName(input.getText().toString());
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Toast.makeText(context, "Couldn't find specified website, using kittenwar.", Toast.LENGTH_LONG).show();
						try {
							host = Inet4Address.getByName(KITTENWAR);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						}
					}
					onDone.onDone();
				}
			});
	
			return alert.create();
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
