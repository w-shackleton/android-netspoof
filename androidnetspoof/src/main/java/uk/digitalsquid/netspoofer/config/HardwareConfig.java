/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
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

package uk.digitalsquid.netspoofer.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A class to hold the configuration; It is pulled from the shared preferences.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public final class HardwareConfig {
    static HardwareConfig DEFAULTS = null;
    
    private String iface;
    
    public HardwareConfig(Context context) {
        if(DEFAULTS == null) DEFAULTS = new HardwareConfig("eth0");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        iface = prefs.getString("iface", DEFAULTS.iface);
        if(iface.equals("")) iface = DEFAULTS.iface;
        // values.put("WLAN", iface); - Set in other places
    }
    
    private HardwareConfig(String iface) {
        this.iface = iface;
    }
}
