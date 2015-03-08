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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.Lists;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;

/**
 * A type of spoof that manipulates an image.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class ImageSpoof extends Spoof implements LogConf {

    private static final long serialVersionUID = -5667589389193684071L;
    
    public static final int IMAGE_FLIP = 1;
    public static final int IMAGE_WOBBLY = 2;
    
    protected final List<Integer> modes;
    
    private static String getTitle(Context context, int mode) {
        switch(mode) {
        case IMAGE_FLIP:
            return context.getResources().getString(R.string.spoof_image_flip);
        case IMAGE_WOBBLY:
            return context.getResources().getString(R.string.spoof_image_wobbly);
        default:
            return "Unknown image spoof";
        }
    }
    private static String getDescription(Context context, int mode) {
        switch(mode) {
        case IMAGE_FLIP:
            return context.getResources().getString(R.string.spoof_image_flip_description);
        case IMAGE_WOBBLY:
            return context.getResources().getString(R.string.spoof_image_wobbly_description);
        default:
            return "";
        }
    }

    public ImageSpoof(Context context, int mode) {
        super(getTitle(context, mode), getDescription(context, mode));
        this.modes = Lists.singleton(mode);
    }

    @Override
    public Dialog displayExtraDialog(Context context,
            OnExtraDialogDoneListener onDone) {
        return null;
    }

    @Override
    public Intent activityForResult(Context context) {
        return null;
    }

    @Override
    public boolean activityFinished(Context context, Intent result) {
        return true;
    }

    @Override
    public void modifyRequest(HttpRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void modifyResponse(HttpResponse response,
            HttpRequest request) {
        List<String> contentType = response.getHeader("Content-Type");
        if(contentType == null) return;
        boolean isImage = false;
        for(String type : contentType) {
            if(type.toLowerCase(Locale.ENGLISH).startsWith("image"))
                isImage = true;
        }
        if(!isImage) return;
        int inputLength = response.getContent().length;
        // Only allow one image to process at once
        synchronized(LOCK) {
            Bitmap bmp = BitmapFactory.decodeByteArray(
                    response.getContent(), 0, response.getContent().length);
            if(bmp == null) {
                Log.v(TAG, "Failed to decode image");
                return; // Failed to decode
            }
            // Now it's decoded, unset in response to allow GC to work
            response.clearContent();

            Bitmap out = Bitmap.createBitmap(
                    bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            Canvas canvas = new Canvas(out);
            redrawImage(canvas, bmp);
            bmp.recycle();
            // Use inputLength as rough guide to output size
            ByteArrayOutputStream os = new ByteArrayOutputStream(inputLength);
            if(!out.compress(CompressFormat.PNG, 87, os))
                Log.w(TAG, "Image failed to recompress as PNG");
            out.recycle();
            response.setContent(os.toByteArray());
            // Set new content-type
            try {
                os.close();
            } catch (IOException e) { }
        }
    }
    
    private static Paint PAINT = new Paint();
    static {
        PAINT.setAntiAlias(true);// TODO: Customisable
    }
    private static Random RAND = new Random();
    
    private static final Object LOCK = new Object();
    
    protected void redrawImage(Canvas out, Bitmap in) {
        // Handle all matrix functions first
        Matrix matrix = new Matrix();
        for(int mode : modes) {
            switch(mode) {
            case IMAGE_FLIP:
                matrix.postRotate(180, in.getWidth()/2, in.getHeight()/2);
                break;
            case IMAGE_WOBBLY:
                float rot = (RAND.nextFloat()-0.5f) * 7f;
                matrix.postRotate(rot, in.getWidth()/2, in.getHeight()/2);
                break;
            }
        }
        out.drawBitmap(in, matrix, PAINT);
    }
    
    /**
     * Adds other's functionality to this spoof.
     * All {@link ImageSpoof} are merged for performance
     * and memory usage reasons
     * @param other
     */
    public void mergeImageSpoof(ImageSpoof other) {
        modes.addAll(other.modes);
    }
}
