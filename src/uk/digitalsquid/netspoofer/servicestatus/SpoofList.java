/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
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
