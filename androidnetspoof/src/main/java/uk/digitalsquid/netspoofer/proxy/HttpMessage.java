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

package uk.digitalsquid.netspoofer.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import uk.digitalsquid.netspoofer.config.Lists;

public class HttpMessage {
    protected HashMap<String, List<String>> headers =
            new HashMap<String, List<String>>();
    
    private byte[] content;
    
    /**
     * Adds a header to this {@link HttpMessage}.
     * @param header
     * @return <code>true</code> if successful
     */
    public boolean addHeader(String header) {
        int colon = header.indexOf(':');
        if(colon == -1) return false;
        String key = header.substring(0, colon).trim();
        String val = header.substring(colon+1).trim();
        addHeader(key, val);
        return true;
    }
    public void addHeader(String key, String val) {
        if(key == null) return;
        key = key.toLowerCase(Locale.ENGLISH);
        if(key.contains("cache")) return; // TODO: debug!!!
        if(key.equals("if-none-match")) return; // TODO: debug!!!
        if(key.equals("if-modified-since")) return; // TODO: debug!!!
        if(key.equals("accept-encoding")) return; // TODO: debug!!!
        // Remove any transfer-encoding left over from the server connection
        if(key.equals("transfer-encoding")) return; // TODO: debug!!!
        if(val == null) val = "";
        if(headers.containsKey(key))
            headers.get(key).add(val);
        else
            headers.put(key, Lists.singleton(val));
    }
    
    /**
     * Returns HTTP formatted headers
     * @return
     */
    public String getHeaders() {
        StringWriter w = new StringWriter();
        for(Entry<String, List<String>> header : headers.entrySet()) {
            for(String val : header.getValue()) {
                w.append(header.getKey());
                w.append(':');
                w.append(val);
                w.append('\r');
                w.append('\n');
            }
        }
        return w.toString();
    }
    public HashMap<String, List<String>> getHeaderPairs() {
        return headers;
    }
    public List<String> getHeader(String key) {
        key = key.toLowerCase(Locale.ENGLISH);
        return headers.get(key);
    }
    public boolean hasHeader(String key) {
        key = key.toLowerCase(Locale.ENGLISH);
        return headers.containsKey(key);
    }
    public void changeHeader(String key, String val) {
        headers.remove(key);
        addHeader(key, val);
    }

    /**
     * Reads from in until all content is read.
     * @param in
     * @throws IOException
     */
    public void readAllContent(InputStream in) throws IOException {
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        while((len=in.read(data)) != -1) {
            arr.write(data, 0, len);
        }
        content = arr.toByteArray();
    }

    /**
     * Reads from in for the specified content-length
     * @param in
     * @param total The total content length to read.
     * @throws IOException
     */
    public void readAllContent(InputStream in, int total) throws IOException {
        byte[] data = new byte[total];
        int len;
        int soFar = 0;
        while((len=in.read(data, soFar, total)) != -1 && total > 0) {
            total -= len;
            soFar += len;
        }
        content = data;
        updateContentLength();
    }
    private static final byte[] EMPTY_CONTENT = new byte[] {};
    public byte[] getContent() {
        if(content == null) return EMPTY_CONTENT;
        return content;
    }
    public void clearContent() {
        content = null;
    }
    public void setContent(byte[] content) {
        this.content = content;
        updateContentLength();
    }
    private void updateContentLength() {
        if(content == null) return;
        headers.remove("content-length");
        headers.put("content-length", Lists.singleton(
                String.valueOf(content.length)));
    }
    
    public String getHost() {
        List<String> host = headers.get("host");
        if(host == null) return "";
        return host.get(0);
    }
    public void setHost(String host) {
        changeHeader("host", host);
    }
    
    /**
     * Resets ALL fields of this {@link HttpMessage}
     */
    public void reset() {
        headers = new HashMap<String, List<String>>();
        content = null;
    }
}
