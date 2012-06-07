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

package uk.digitalsquid.netspoofer.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * A {@link LinearLayout} which forwards it checking abilities to a supplied subclass
 * @author william
 *
 */
public class CheckedLinearLayout extends LinearLayout implements Checkable {
	
	private Checkable check;

	public CheckedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {
		if(check != null) return check.isChecked();
		return false;
	}

	@Override
	public void setChecked(boolean arg0) {
		if(check != null) check.setChecked(arg0);
	}

	@Override
	public void toggle() {
		if(check != null) check.toggle();
	}
	
	/**
	 * Sets the subclass to derive from
	 * @param check
	 */
	public void setCheckable(Checkable check) {
		this.check = check;
	}
}
