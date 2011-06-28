package uk.digitalsquid.netspoofer.servicemsg;

import java.io.Serializable;

/**
 * Base class for messages to the service.
 * Also used for some simple commands.
 * @author william
 *
 */
public class ServiceMsg implements Serializable {
	private static final long serialVersionUID = 4093240028206997618L;
	public static final int MESSAGE_OTHER = 0;
	public static final int MESSAGE_STOP = 1;
	public static final int MESSAGE_GETSPOOFS = 2;
	public static final int MESSAGE_STOPSPOOF = 3;
	
	private final int message;
	
	public ServiceMsg() {
		message = MESSAGE_OTHER;
	}
	public ServiceMsg(int message) {
		this.message = message;
	}
	public int getMessage() {
		return message;
	}
}
