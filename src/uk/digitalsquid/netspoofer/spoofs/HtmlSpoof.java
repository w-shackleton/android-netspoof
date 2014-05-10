package uk.digitalsquid.netspoofer.spoofs;

import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;

/**
 * Represents a spoof that manipulates the HTML content of a web page.
 * @author Will Shackleton <w.shackleton@gmail.com>
 *
 */
public class HtmlSpoof extends Spoof {

	private static final long serialVersionUID = -1966412296143206193L;
	
	private List<HtmlEditorSpoof> editors;

	public HtmlSpoof(String title, String description) {
		super(title, description);
	}

	@Override
	public void modifyRequest(HttpRequest request) {
	}

	@Override
	public void modifyResponse(HttpResponse response, HttpRequest request) {
		List<String> contentType = response.getHeader("Content-Type");
		if(contentType == null) return;
		boolean isHtml = false;
		for(String type : contentType) {
			if(type.toLowerCase(Locale.ENGLISH).startsWith("text/html"))
				isHtml = true;
		}
		if(!isHtml) return;
		
		Document doc = Jsoup.parse(new String(request.getContent()));
		
		for(HtmlEditorSpoof editor : editors) {
			editor.modifyDocument(doc);
		}
	}
}
