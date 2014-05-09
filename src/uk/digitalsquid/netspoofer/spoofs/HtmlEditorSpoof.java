package uk.digitalsquid.netspoofer.spoofs;

import org.jsoup.nodes.Document;

import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;

public abstract class HtmlEditorSpoof extends Spoof {

	private static final long serialVersionUID = -7850053238248022694L;

	public HtmlEditorSpoof(String title, String description) {
		super(title, description);
	}

	@Override
	public void modifyRequest(HttpRequest request) {
		throw new IllegalStateException("HtmlEditorSpoof.modifyRequest should "
				+ "never be called");
	}

	@Override
	public void modifyResponse(HttpResponse response, HttpRequest request) {
		throw new IllegalStateException("HtmlEditorSpoof.modifyResponse should "
				+ "never be called");
	}
	
	protected abstract void modifyDocument(Document document);
}
