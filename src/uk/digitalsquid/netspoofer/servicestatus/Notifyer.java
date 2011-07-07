package uk.digitalsquid.netspoofer.servicestatus;

public class Notifyer extends ServiceStatus {
	
	public static final int STATUS_SHOW = 0;
	public static final int STATUS_HIDE = 1;

	private static final long serialVersionUID = -3703680009063029815L;

	private final int notificationType, status;
	
	public Notifyer(int type, int status) {
		notificationType = type;
		this.status = status;
	}

	public int getNotificationType() {
		return notificationType;
	}

	public int getStatus() {
		return status;
	}
}
