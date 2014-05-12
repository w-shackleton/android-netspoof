package uk.digitalsquid.netspoofer.spoofs;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.IOHelpers;
import uk.digitalsquid.netspoofer.config.Lists;
import uk.digitalsquid.netspoofer.config.LogConf;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

public class ContentChange extends HtmlEditorSpoof implements LogConf {
	
	private static final long serialVersionUID = 792590861534877480L;
	public static final int MODE_FLIP = 1;
	public static final int MODE_GRAVITY = 2;
	public static final int MODE_DELETE = 3;

	private static String getTitle(Context context, int mode) {
		switch(mode) {
		case MODE_FLIP:
			return context.getResources().getString(R.string.spoof_content_flip);
		case MODE_GRAVITY:
			return context.getResources().getString(R.string.spoof_gravity);
		case MODE_DELETE:
			return context.getResources().getString(R.string.spoof_delete);
		default:
			return "Unknown image spoof";
		}
	}
	private static String getDescription(Context context, int mode) {
		switch(mode) {
		case MODE_FLIP:
			return context.getResources().getString(R.string.spoof_content_flip_description);
		case MODE_GRAVITY:
			return context.getResources().getString(R.string.spoof_gravity_description);
		case MODE_DELETE:
			return context.getResources().getString(R.string.spoof_delete_description);
		default:
			return "";
		}
	}

	private final int mode;
	
	private final String js;

	public ContentChange(Context context, int mode) {
		super(getTitle(context, mode), getDescription(context, mode));
		this.mode = mode;
		switch(mode) {
		default:
		case MODE_GRAVITY:
			js = "<script src=\"http://gravityscript.googlecode.com/svn/trunk/gravityscript.js\"></script>";
			break;
		case MODE_DELETE:
			String payload = "";
			try {
				payload = IOHelpers.readFileContents(
						context.getResources().openRawResource(R.raw.js_removewords));
			} catch (NotFoundException e) {
				Log.w(TAG, "Failed to load js_removewords payload", e);
			} catch (IOException e) {
				Log.w(TAG, "Failed to load js_removewords payload", e);
			}
			js = payload;
			break;
		}
	}

	@Override
	protected void modifyDocument(Document document, Element body) {
		switch(mode) {
		case MODE_FLIP:
			modifyElement(body);
			break;
		case MODE_GRAVITY:
		case MODE_DELETE:
			document.select("head").append(js);
			break;
			
		}
	}
	
	/**
	 * Recursively modifies an element according to mode.
	 * @param element
	 */
	private void modifyElement(Element element) {
		for(Node node : element.childNodes()) {
			if(node instanceof TextNode) {
				modifyTextNode((TextNode) node);
			} else if(node instanceof Element) {
				modifyElement((Element) node);
			}
		}
	}
	
	private void modifyTextNode(TextNode node) {
		switch(mode) {
		case MODE_FLIP:
			String reversed =
					new StringBuilder(node.text()).reverse().toString();
			node.text(Lists.map(TitleChange.upsideDown, reversed));
			break;
		}
	}
}
