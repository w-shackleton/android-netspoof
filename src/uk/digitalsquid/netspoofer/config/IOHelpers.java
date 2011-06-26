package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class IOHelpers {
	private IOHelpers() {}
	
	/**
	 * Reads a whole file from is, and closes the stream.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static final String readFileContents(InputStream is) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[256];
		for (int n; (n = is.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		is.close();
		return out.toString();
	}
	
	/**
	 * Reads a whole file into an array of lines.
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public static final List<String> readFileToLines(String filename) throws IOException {
		FileReader reader = new FileReader(filename);
		BufferedReader br = new BufferedReader(reader);
		
		List<String> lines = new ArrayList<String>();
		String line;
		while((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		reader.close();
		return lines;
	}
}
