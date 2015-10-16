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

package uk.digitalsquid.netspoofer.root;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.digitalsquid.netspoofer.config.FileFinder;
import uk.digitalsquid.netspoofer.config.FileInstaller;
import uk.digitalsquid.netspoofer.config.LogConf;

/**
 * Checks that the device has root access.
 */
public class RootChecker implements LogConf {
    private Context mContext;

    public RootChecker(Context context) {
        mContext = context;
    }

    public enum RootCheckResult {
        ERROR,
        UNAVAILABLE,
        BINARY_NO_EXEC,
        AVAILABLE
    }

    public RootCheckResult check() {
        try {
            FileFinder.initialise(mContext);
        } catch (FileNotFoundException e) {
            // Handle error below
        }
        if (FileFinder.SU == null || FileFinder.SU == "") {
            return RootCheckResult.UNAVAILABLE;
        }

        ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
                FileInstaller.getScriptPath(mContext, "rootcheck"));

        try {
            Process p = pb.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String idLine = stdout.readLine();

            // Munch garbage
            if (idLine != null) {
                while (stdout.readLine() != null) ;
            }
            while (stderr.readLine() != null);

            p.waitFor();

            return idLine != null && idLine.contains("(root)") ?
                    RootCheckResult.AVAILABLE :
                    RootCheckResult.BINARY_NO_EXEC;
        } catch (IOException e) {
            Log.e(TAG, "Failed to run root check", e);
            return RootCheckResult.ERROR;
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to run root check", e);
            return RootCheckResult.ERROR;
        }
    }
}
