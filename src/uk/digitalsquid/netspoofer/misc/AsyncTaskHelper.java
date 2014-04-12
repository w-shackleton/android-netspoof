package uk.digitalsquid.netspoofer.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import uk.digitalsquid.netspoofer.config.LogConf;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Provides helper methods for {@link AsyncTask} to do with differing API versions
 * @author william
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
