package uk.digitalsquid.netspoofer.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public final class FileInstaller implements LogConf {
	private static final String BIN_DIR = "/bin";
	private final Context context;
	
	public FileInstaller(Context context) throws FileNotFoundException {
		this.context = context;
		FileFinder.initialise(context);
		new File(context.getFilesDir().getParent() + BIN_DIR).mkdir();
	}
	
	private void installFile(String filename, String permissions, int id) throws Resources.NotFoundException, IOException {
		installFile(filename, id);
		ProcessRunner.runProcess(FileFinder.BUSYBOX, "chmod", "a+rx", filename);
	}
	
	public void installScript(String scriptName, int id) throws Resources.NotFoundException, IOException {
		String scriptPath = getScriptPath(scriptName);
		installFile(scriptPath, "a+x", id);
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
	
	public String getScriptPath(String scriptName) {
		return context.getFilesDir().getParent() + BIN_DIR + "/" + scriptName;
	}
}
