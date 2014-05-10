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
