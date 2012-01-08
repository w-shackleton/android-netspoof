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

import uk.digitalsquid.netspoofer.NetSpoofService;
import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author william
 *
 */
public class CustomGalleryImageChange extends SquidScriptSpoof implements LogConf {
	private static final long serialVersionUID = 8490503138296852028L;
	
	public CustomGalleryImageChange() {
		super("Custom image change (image on phone)", "Change all images on all websites", "trollface.sh");
	}
	
	/**
	 * The image name, as it appears in $DEB/var/www/images/
	 */
	public static final String IMAGE_NAME = "customimage.jpg";
	
	@Override
	public Intent activityForResult(Context context) {
		return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
	}
	
	@Override
	public boolean activityFinished(final Context context, Intent result) {
		super.activityFinished(context, result);
		final Uri customImageURI = result.getData();
		
		// Communication with service to tell it to load image into debian area
		// Using service, as this makes sure this is finished before spoof is started.
		final ServiceConnection serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				NetSpoofServiceBinder binder = (NetSpoofServiceBinder) service;
	            binder.getService().saveImageToWebserver(customImageURI);
	            
	            // Close again
	            context.unbindService(this);
			}
			
			@Override
			public void onServiceDisconnected(ComponentName arg0) { }
		};
	
        context.bindService(new Intent(context, NetSpoofService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		return true;
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		ret.put("SPOOFIMAGEURL", "http://127.0.0.1/images/" + IMAGE_NAME);
		return ret;
	}
}
