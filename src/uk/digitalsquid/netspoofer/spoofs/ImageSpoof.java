package uk.digitalsquid.netspoofer.spoofs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;
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

/**
 * A type of spoof that manipulates an image.
 * @author william
 *
 */
public class ImageSpoof extends Spoof implements LogConf {

	private static final long serialVersionUID = -5667589389193684071L;
	
	public static final int IMAGE_FLIP = 1;
	
	protected final int mode;
	
	private static String getTitle(Context context, int mode) {
		switch(mode) {
		case IMAGE_FLIP:
			return context.getResources().getString(R.string.spoof_image_flip);
		default:
			return "Unknown image spoof";
		}
	}
	private static String getDescription(Context context, int mode) {
		switch(mode) {
		case IMAGE_FLIP:
			return context.getResources().getString(R.string.spoof_image_flip_description);
		default:
			return "";
		}
	}

	public ImageSpoof(Context context, int mode) {
		super(getTitle(context, mode), getDescription(context, mode));
		this.mode = mode;
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
		Bitmap bmp = BitmapFactory.decodeByteArray(
				response.getContent(), 0, response.getContent().length);
		if(bmp == null) {
			Log.v(TAG, "Failed to decod image");
			return; // Failed to decode
		}
		Log.v(TAG, "Manipulating image");
		// Now it's decoded, unset in response to allow GC to work
		response.clearContent();
		
		Bitmap out = Bitmap.createBitmap(
				bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
		Canvas canvas = new Canvas(out);
		redrawImage(canvas, bmp);
		bmp.recycle();
		// Use inputLength as rough guide to output size
		ByteArrayOutputStream os = new ByteArrayOutputStream(inputLength);
		if(!out.compress(CompressFormat.JPEG, 87, os))
			Log.w(TAG, "Image failed to recompress as JPEG");
		out.recycle();
		response.setContent(os.toByteArray());
		try {
			os.close();
		} catch (IOException e) { }
	}
	
	private static Paint PAINT = new Paint();
	
	protected void redrawImage(Canvas out, Bitmap in) {
		Matrix matrix;
		switch(mode) {
		case IMAGE_FLIP:
			matrix = new Matrix();
			matrix.setRotate(180, in.getWidth()/2, in.getHeight()/2);
			out.drawBitmap(in, matrix, PAINT);
			break;
		}
	}
}
