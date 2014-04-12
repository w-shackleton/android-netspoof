/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2011 Will Shackleton
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

package uk.digitalsquid.netspoofer.servicestatus;

/**
 * Message for managing notifications
 * @author william
 *
 */
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
