package uk.digitalsquid.netspoofer.servicemsg;

import uk.digitalsquid.netspoofer.spoofs.SpoofData;

public class SpoofStarter extends ServiceMsg {
	private static final long serialVersionUID = 6740057018207849838L;

	private final SpoofData spoof;
	
	public SpoofStarter(SpoofData spoof) {
		super();
		this.spoof = spoof;
	}

	public SpoofData getSpoof() {
		return spoof;
	}
}
