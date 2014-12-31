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

package uk.digitalsquid.netspoofer.misc;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import uk.digitalsquid.netspoofer.config.LogConf;

/**
 * Provides helper methods for {@link AsyncTask} to do with differing API versions
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class AsyncTaskHelper implements LogConf {
	
	/**
	 * Better version of execute which copes with honeycomb and greater, which
	 * by default run methods in sequence. See
	 * http://developer.android.com/reference/android/os/AsyncTask.html#executeOnExecutor(java.util.concurrent.Executor, Params...)
	 * If Network Spoofer is ever upgraded to min API 11, this can be deleted and replaced with an actual method call.
	 * @param task
	 * @param args
	 */
	public static <S, P, F> void execute(AsyncTask<S, P, F> task, S... args) {
		init();
		if(executeOnExecutor == null || THREAD_POOL_EXECUTOR == null) {
			task.execute(args);
		} else {
			try {
				executeOnExecutor.invoke(task, THREAD_POOL_EXECUTOR, args);
				
				// In error cases, invoke old method instead.
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Failed to invoke executeOnExecutor", e);
				task.execute(args);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Failed to invoke executeOnExecutor", e);
				task.execute(args);
			} catch (InvocationTargetException e) {
				Log.e(TAG, "Failed to invoke executeOnExecutor", e);
				task.execute(args);
			}
		}
	}
	
	private static boolean initialised = false;
	
	private static Method executeOnExecutor;
	private static Executor THREAD_POOL_EXECUTOR;
	
	private static synchronized void init() {
		if(initialised) return;
		// Get AsyncTask.executeOnExecutor method - needed to correctly call parallel AsyncTasks
		// on platforms that have this method.
		try {
			executeOnExecutor = AsyncTask.class.getMethod("executeOnExecutor", Executor.class, Object[].class);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			executeOnExecutor = null;
			Log.i(TAG, "AsyncTask.executeOnExecutor method not found, will use AsyncTask.execute");
		}
		
		try {
			THREAD_POOL_EXECUTOR = (Executor) AsyncTask.class.getField("THREAD_POOL_EXECUTOR").get(null);
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Failed to get THREAD_POOL_EXECUTOR", e);
		} catch (SecurityException e) {
			Log.w(TAG, "Failed to get THREAD_POOL_EXECUTOR", e);
		} catch (IllegalAccessException e) {
			Log.w(TAG, "Failed to get THREAD_POOL_EXECUTOR", e);
		} catch (NoSuchFieldException e) {
			Log.w(TAG, "Failed to get THREAD_POOL_EXECUTOR", e);
		}
		
		Log.i(TAG, "Initiated AsyncTaskHelper");
		
		initialised = true;
	}
}
