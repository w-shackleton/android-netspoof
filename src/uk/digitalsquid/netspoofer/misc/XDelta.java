package uk.digitalsquid.netspoofer.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import uk.digitalsquid.netspoofer.InstallService.DLProgress;
import uk.digitalsquid.netspoofer.InstallService.DLProgressPublisher;
import uk.digitalsquid.netspoofer.config.Config;
import uk.digitalsquid.netspoofer.config.FileInstaller;
import uk.digitalsquid.netspoofer.config.ProcessRunner;
import uk.digitalsquid.netspoofer.config.ProcessRunner.OutputCallback;
import android.content.Context;
import android.util.Log;

/**
 * Patches files using an XDelta based script
 * @author william
 *
 */
public final class XDelta implements Config {
	private XDelta() {}

	public static final boolean patchFile(DLProgressPublisher callback, Context context, String patch, String file, String archive) throws IOException {
		// To start off with we have an old archive, old file and patch.
		
		File newArchive = new File(archive);
		if(!newArchive.exists()) throw new IOException("Old file doesn't exist"); // Old file is actually new file here
		File oldArchive = new File(newArchive.getAbsolutePath() + ".old");
		newArchive.renameTo(oldArchive); // Now old is old file and new doesn't exist.
		
		File oldFile = new File(file);
		oldFile.delete();
		File newFile = oldFile; // Destination for new file creation
		
		try {
			if(!patchFile(callback, context, new File(patch), oldArchive, newFile, newArchive)) {
				throw new IOException("Failed to patch file");
			}
		} catch (IOException e) {
			oldArchive.renameTo(newArchive); // Undo
			unzipRecover(callback, newArchive, newFile, Config.DEB_IMG_URL_SIZE);
			return false;
		}
		oldArchive.delete();
		return true;
	}
	
	private static final boolean patchFile(final DLProgressPublisher callback, Context context, File patch, File origArchive, File newFile, File newArchive) throws IOException {
		String patchCmd = FileInstaller.getScriptPath(context, "patch");
		String busyboxCmd = FileInstaller.getScriptPath(context, "busybox");
		String xdelta3Cmd = FileInstaller.getScriptPath(context, "xdelta3");
		String pvCmd = FileInstaller.getScriptPath(context, "pv");
		
		String fifo = new File(context.getCacheDir(), "patchFifo").getAbsolutePath();
		final int unpackedSize = Config.DEB_IMG_URL_SIZE;
		
		return ProcessRunner.runProcessWithCallback(null, new OutputCallback() {
				@Override
				public void onNewCout(String line) {
					// Ignore cout, is logged anyway
				}
				
				@Override
				public void onNewCerr(String line) {
					// Cerr will contain percentages
					try {
						int num = Integer.parseInt(line);
						callback.publishDLProgress(new DLProgress(DLProgress.STATUS_PATCHING, num * Config.DEB_IMG_URL_SIZE / 100, Config.DEB_IMG_URL_SIZE)); // Here we are given a percentage by pv, need rough number of bytes
					} catch (NumberFormatException e) {
						Log.i(TAG, "Failed to parse patch status line \"" + line + "\"");
					}
				}
			},
			patchCmd,
			busyboxCmd,
			xdelta3Cmd,
			pvCmd,
			fifo,
			origArchive.getAbsolutePath(),
			String.valueOf(unpackedSize),
			patch.getAbsolutePath(),
			newFile.getAbsolutePath(),
			newArchive.getAbsolutePath()) == 0;
	}
	
	/**
	 * Recovers from a bad patch by unzipping the original archive.
	 * @param inFile
	 * @return
	 * @throws IOException
	 */
	private static final void unzipRecover(DLProgressPublisher callback, File archive, File file, int extractedSize) throws IOException
	{
	    InputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(archive)));
	 
		DLProgress progress = new DLProgress(DLProgress.STATUS_RECOVERING, 0, extractedSize);
	 
	    OutputStream out = new FileOutputStream(file);
	    
	    byte[] buf = new byte[0x8000];
	    int len, total = 0;
	    int i = 0;
	    while ((len = gzipInputStream.read(buf)) > 0) {
	    	total += len;
	        out.write(buf, 0, len);
	        if(i++ > 60) {
	        	i = 0;
				progress.setBytesDone(total);
				callback.publishDLProgress(progress);
	        }
	    }
	 
	    gzipInputStream.close();
	    out.close();
	}
}
