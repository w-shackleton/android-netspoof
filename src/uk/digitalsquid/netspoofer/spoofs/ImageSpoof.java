package uk.digitalsquid.netspoofer.spoofs;

import java.util.List;

import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

/**
 * A type of spoof that manipulates an image.
 * @author william
 *
 */
public class ImageSpoof extends Spoof {

	private static final long serialVersionUID = -5667589389193684071L;

	public ImageSpoof(String title, String description) {
		super(title, description);
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
			if(type.startsWith("image"))
				isImage = true;
		}
	}
}
