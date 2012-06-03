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

package uk.digitalsquid.netspoofer.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import uk.digitalsquid.netspoofer.InstallService.DLProgress;
import uk.digitalsquid.netspoofer.InstallService.DLProgressPublisher;
import android.content.Context;
import android.util.Log;

/**
 * Installs upgrades, copying files into the chroot.
 * @author william
 *
 */
public final class UpgradeInstaller implements Config {
	
	/**
	 * Exceptions describing what went wrong during an upgrade
	 * @author william
	 *
	 */
	public static class UpgradeException extends IOException {
		private static final long serialVersionUID = 491497913367265516L;
		
		public static final int FAILED_TO_START = 1;
		public static final int FAILED_TO_RUN_SCRIPT = 2;
		
		int reason;
		
		public UpgradeException(int reason) {
			super();
			this.reason = reason;
		}
	}
	
	public static final void copyUpgrade(Context context, DLProgressPublisher callback, File src) throws UpgradeException {
		ChrootManager chroot = new ChrootManager(context, new ChrootConfig(context));
		File dest = new File(chroot.config.getDebianMount());
		
		boolean startFailed = false;
		try {
			startFailed = !chroot.start();
		} catch (IOException e) {
			e.printStackTrace();
			startFailed = true;
		}
		if(startFailed)
			throw new UpgradeException(UpgradeException.FAILED_TO_START);
		
		// Using arbitrary numbers here for progress
		callback.publishDLProgress(new DLProgress(DLProgress.STATUS_PATCHING, 1, 4));
		
		Map<String, String> env = chroot.config.getValues();
		try {
			if(ProcessRunner.runProcess(env, FileFinder.SU, "-c",
					FileInstaller.getScriptPath(context, "applyupgrade") + " " +
					FileInstaller.getScriptPath(context, "config") + " " +
					src.getAbsolutePath() + " " +
					dest.getAbsolutePath()
			) != 0) {
				Log.w(TAG, "Upgrade script failed to run!");
				chroot.stop();
				throw new UpgradeException(UpgradeException.FAILED_TO_RUN_SCRIPT);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.w(TAG, "Upgrade script failed to run!", e1);
			try { chroot.stop(); } catch (IOException e) { }
			throw new UpgradeException(UpgradeException.FAILED_TO_RUN_SCRIPT);
		}
		
		callback.publishDLProgress(new DLProgress(DLProgress.STATUS_PATCHING, 2, 4));
		
		try {
			chroot.stop();
		} catch (IOException e) {
			e.printStackTrace();
			// No error needed - don't worry if umount failed.
		}
		callback.publishDLProgress(new DLProgress(DLProgress.STATUS_PATCHING, 3, 4));
	}
}
