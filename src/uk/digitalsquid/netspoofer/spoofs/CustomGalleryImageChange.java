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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class CustomGalleryImageChange extends Spoof implements LogConf {
	private static final long serialVersionUID = 8490503138296852028L;
	
	public static final int MODE_TROLLFACE = 1;
	public static final int MODE_CUSTOM = 2;
	
	private static String getTitle(Context context, int mode) {
		switch(mode) {
		case MODE_TROLLFACE:
			return context.getResources().getString(R.string.spoof_trollface);
		case MODE_CUSTOM:
			return context.getResources().getString(R.string.spoof_image_custom);
		default:
			return "Unknown image spoof";
		}
	}
	private static String getDescription(Context context, int mode) {
		switch(mode) {
		case MODE_TROLLFACE:
			return context.getResources().getString(R.string.spoof_trollface_description);
		case MODE_CUSTOM:
			return context.getResources().getString(R.string.spoof_image_custom_description);
		default:
			return "";
		}
	}
	
	private final int mode;
	
	public CustomGalleryImageChange(Context context, int mode) {
		super(getTitle(context, mode), getDescription(context, mode));
		this.mode = mode;

		InputStream is = context.getResources().openRawResource(R.raw.trollface);
		loadImage(is);
	}
	
	@Override
	public Intent activityForResult(Context context) {
		switch(mode) {
		case MODE_CUSTOM:
			return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		default:
			return null;
		}
	}
	
	private File customImage;
	private byte[] imageData;
	private String mimeType;
	
	@Override
	public boolean activityFinished(final Context context, Intent result) {
		super.activityFinished(context, result);
		final Uri customImageURI = result.getData();
		try {
			customImage = new File(new URI(customImageURI.toString()));
		} catch (URISyntaxException e) {
			Log.e(TAG, "Failed to load custom image", e);
		}
		
		return true;
	}

	@Override
	public void modifyRequest(HttpRequest request) {
	}
	
	private static final Object LOAD_SYNC = new Object();

	@Override
	public void modifyResponse(HttpResponse response, HttpRequest request) {
		if(imageData == null) return;
		List<String> contentType = response.getHeader("Content-Type");
		if(contentType == null) return;
		boolean isImage = false;
		for(String type : contentType) {
			if(type.toLowerCase(Locale.ENGLISH).startsWith("image"))
				isImage = true;
		}
		if(!isImage) return;
		
		// Load image
		synchronized(LOAD_SYNC) {
			if(imageData == null) {
				try {
					loadImage(new FileInputStream(customImage));
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Failed to load custom image", e);
				}
			}
		}
		
		// Set new content
		response.changeHeader("Content-Type", mimeType);
		response.setContent(imageData);
	}
	
	private void loadImage(InputStream is) {
		try {
			BufferedInputStream buf = new BufferedInputStream(is);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] intermediate = new byte[2048];
			int len;
			while((len = buf.read(intermediate)) != -1) {
				os.write(intermediate, 0, len);
			}
			imageData = os.toByteArray();
			buf.close();
			os.close();

			// Decode image format
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length, opts);
			mimeType = opts.outMimeType;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to load custom image", e);
		} catch (IOException e) {
			Log.e(TAG, "Failed to load custom image", e);
		}
	}
}
