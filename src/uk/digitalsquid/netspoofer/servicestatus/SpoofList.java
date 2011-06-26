package uk.digitalsquid.netspoofer.servicestatus;

import java.util.ArrayList;

import uk.digitalsquid.netspoofer.spoofs.Spoof;

public class SpoofList extends ServiceStatus {
	private static final long serialVersionUID = -3777527861258107613L;
	
	private final ArrayList<Spoof> spoofs;

	public SpoofList(ArrayList<Spoof> spoofs) {
		this.spoofs = spoofs;
	}

	public ArrayList<Spoof> getSpoofs() {
		return spoofs;
	}
}
