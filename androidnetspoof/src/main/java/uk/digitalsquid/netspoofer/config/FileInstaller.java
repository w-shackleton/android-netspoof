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
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.digitalsquid.netspoofer.JNI;

public final class FileInstaller implements LogConf {
    private static final String BIN_DIR = "/bin";
    private static final String DATA_DIR = "/bindata";
    private final Context context;
    
    public FileInstaller(Context context) throws FileNotFoundException {
        this.context = context;
        try {
            new File(context.getFilesDir().getParent() + BIN_DIR).mkdir();
            new File(context.getFilesDir().getParent() + DATA_DIR).mkdir();
        } catch (NullPointerException e) {
            // One of the above fileops failed, most likely due to broken phone.
            Log.e(TAG, "Failed to create binary directory!");
        }
    }
    
    private void installFile(String filename, boolean executable, int id) throws Resources.NotFoundException, IOException {
        installFile(filename, id);
        if(executable) JNI.setExecutable(filename);
    }
    
    public void installScript(String scriptName, int id) throws Resources.NotFoundException, IOException {
        String scriptPath = getScriptPath(scriptName);
        installFile(scriptPath, true, id);
    }
    
    private void installFile(String filename, int id) throws Resources.NotFoundException, IOException {
        InputStream is = context.getResources().openRawResource(id);
        File outFile = new File(filename);
        outFile.createNewFile();
        Log.d(TAG, "Copying file '"+filename+"' ...");
        byte buf[] = new byte[1024];
        int len;
        OutputStream out = new FileOutputStream(outFile);
        while((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
    }

    /**
     * Installs an asset from the assets folder.
     * @param binaryName
     */
    public void installAsset(String path, String dest) throws IOException {
        InputStream is = context.getAssets().open(path);
        File outFile = new File(dest);
        outFile.createNewFile();
        Log.d(TAG, String.format("Copying from %s to %s", path, dest));
        byte buf[] = new byte[1024];
        int len;
        OutputStream out = new FileOutputStream(outFile);
        while((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
    }

    /**
     * Installs a binary from the assets folder.
     * @param binaryName
     */
    public void installBinary(String binaryName) throws IOException {
        String path = String.format("binaries/%s/%s", Build.CPU_ABI, binaryName);
        String dest = getScriptPath(binaryName);

        installAsset(path, dest);

        // Mark as executable
        JNI.setExecutable(dest);
    }

    /**
     * Installs a file from the assets folder.
     * @param binaryName
     */
    public void installData(String subdir, String filename) throws IOException {
        String path = String.format("data/%s/%s", subdir, filename);
        String dest = getDataPath(filename);

        installAsset(path, dest);
    }

    public static String getScriptPath(Context context, String scriptName) {
        return new File(context.getFilesDir().getParent() + BIN_DIR).getAbsolutePath() + "/" + scriptName;
    }

    public static String getDataPath(Context context, String file) {
        return new File(context.getFilesDir().getParent() + DATA_DIR).getAbsolutePath() + "/" + file;
    }

    private String getScriptPath(String scriptName) {
        return getScriptPath(context, scriptName);
    }

    private String getDataPath(String scriptName) {
        return getDataPath(context, scriptName);
    }
}
