package uk.digitalsquid.netspoofer.misc;

import java.io.File;
import java.io.IOException;

/**
 * Patches files using XDelta
 * @author william
 *
 */
public final class XDelta {
	private XDelta() {}

	public static final boolean patchFile(String patch, String file) throws IOException {
		File newFile = new File(file);
		if(!newFile.exists()) throw new IOException("Old file doesn't exist");
		File oldFile = new File(newFile.getAbsolutePath() + ".old");
		newFile.renameTo(oldFile);
		try {
			if(!patchFile(patch, oldFile.getAbsolutePath(), file)) {
				throw new IOException("Failed to patch file");
			}
		} catch (IOException e) {
			oldFile.renameTo(new File(file));
			return false;
		}
		oldFile.delete();
		return true;
	}
	
	public static final boolean patchFile(String patch, String from, String to) throws IOException {
		return false;
	}
}
