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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
    
    protected abstract void modifyDocument(Document document, Element body);
}
