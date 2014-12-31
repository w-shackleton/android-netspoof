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

package uk.digitalsquid.netspoofer.misc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.digitalsquid.netspoofer.config.LogConf;

/**
 * An {@link InputStream} for reading HTTP-type protocols
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class MagicInputStream extends DataInputStream implements LogConf {

    public MagicInputStream(InputStream in) {
        super(in);
    }
    
    private StringBuffer rslWriter = new StringBuffer();
    
    /**
     * Reads a line without using buffering.
     * @return
     * @throws IOException 
     */
    public /*synchronized*/ String readStringLine() throws IOException {
        char c;
        while(true) {
            c = (char) readByte();
            String result;
            switch(c) {
            case '\n':
                result = rslWriter.toString();
                rslWriter.setLength(0);
                return result;
            case '\r':
                // Read next character to check for \n
                result = rslWriter.toString();
                char next = (char)readByte();
                switch(next) {
                case '\n':
                    rslWriter.setLength(0);
                    return result;
                default:
                    // Delete all but the last character we just read.
                    rslWriter.delete(0, rslWriter.length()-1);
                    return result;
                }
            default:
                rslWriter.append(c);
            }
        }
    }
}
