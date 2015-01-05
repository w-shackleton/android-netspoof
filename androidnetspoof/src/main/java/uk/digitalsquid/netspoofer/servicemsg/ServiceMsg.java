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

package uk.digitalsquid.netspoofer.servicemsg;

import java.io.Serializable;

/**
 * Base class for messages to the service.
 * Also used for some simple commands.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class ServiceMsg implements Serializable {
    private static final long serialVersionUID = 4093240028206997618L;
    public static final int MESSAGE_OTHER = 0;
    public static final int MESSAGE_STOP = 1;
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
