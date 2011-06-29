package uk.digitalsquid.netspoofer.servicestatus;

public class InitialiseStatus extends ServiceStatus {
	private static final long serialVersionUID = 7918466113688717777L;
	
	// Now using values straight from NetSpoofService
	/*
	 * public static final int INIT_COMPLETE = 0;
	 * public static final int INIT_FAIL = 1;
	 */
	
	public final int status;

	public InitialiseStatus(int status) {
		this.status = status;
	}
}
