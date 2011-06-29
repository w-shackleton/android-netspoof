package uk.digitalsquid.netspoofer.servicestatus;

import java.util.ArrayList;

public class NewLogOutput extends ServiceStatus {
	private static final long serialVersionUID = 8663033245576144897L;

	private final ArrayList<String> logLines;
	
	public NewLogOutput(ArrayList<String> lines) {
		logLines = lines;
	}

	public ArrayList<String> getLogLines() {
		return logLines;
	}
}
