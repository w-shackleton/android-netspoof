package uk.digitalsquid.netspoofer.misc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.util.Log;

/**
 * An {@link InputStream} for reading HTTP-type protocols
 * @author william
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
