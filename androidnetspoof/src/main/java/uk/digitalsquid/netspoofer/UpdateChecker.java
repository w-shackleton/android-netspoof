package uk.digitalsquid.netspoofer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.digitalsquid.netspoofer.UpdateChecker.UpdateInfo;
import uk.digitalsquid.netspoofer.config.LogConf;

public class UpdateChecker extends AsyncTask<Void, Void, UpdateInfo>
                implements LogConf {
	
	private String appVersion;
	private final OnUpdateListener listener;
	
	public UpdateChecker(App app, OnUpdateListener l) throws NameNotFoundException {
		// Check if newer than current version
		PackageInfo pInfo =
				app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
		appVersion = pInfo.versionName;
		listener = l;
	}
	
	public static class UpdateInfo implements Parcelable {
		public String url;
		public String versionName;
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(url);
			dest.writeString(versionName);
		}
		
		public static final Parcelable.Creator<UpdateInfo> CREATOR =
				new Creator<UpdateChecker.UpdateInfo>() {

			@Override
			public UpdateInfo[] newArray(int size) {
				return new UpdateInfo[size];
			}
			
			@Override
			public UpdateInfo createFromParcel(Parcel source) {
				UpdateInfo result = new UpdateInfo();
				result.url = source.readString();
				result.versionName = source.readString();
				return result;
			}
		};
	}

	@Override
	protected UpdateInfo doInBackground(Void... arg0) {
		// Use Github API to check for new releases.
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(
				"https://api.github.com/repos/w-shackleton/android-netspoof/releases");
		
		try {
			HttpResponse response = client.execute(request);
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							response.getEntity().getContent(), "UTF-8"));
			StringBuilder json = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {
				json.append(line);
			}
			return parseJson(json.toString());
		} catch (ClientProtocolException e) {
			Log.w(TAG, "Couldn't check for updates", e);
		} catch (IOException e) {
			Log.w(TAG, "Couldn't check for updates", e);
		} catch (JSONException e) {
			Log.w(TAG, "Couldn't parse updates", e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(UpdateInfo result) {
		super.onPostExecute(result);
		if(result == null) return;
		listener.updateAvailable(result);
	}
	
	private UpdateInfo parseJson(String input)
			throws JSONException {
		JSONArray releases = new JSONArray(input);
		// Find release with greatest release number
		if(releases.length() <= 0) return null;
		JSONObject latest = releases.getJSONObject(0);
		String latestVersion = latest.getString("tag_name");

		for(int i = 0 ; i < releases.length(); i++) {
			JSONObject iObj = releases.getJSONObject(i);
			String iVersion = iObj.getString("tag_name");
			if(versionCompare(iVersion, latestVersion) > 0) {
				latestVersion = iVersion;
				latest = iObj;
			}
		}
		if(versionCompare(latestVersion, appVersion) <= 0)
			return null;
		
		String url = latest.getString("html_url");
		
		JSONArray assets = latest.getJSONArray("assets");
		for(int i = 0; i < assets.length(); i++) {
			JSONObject asset = assets.getJSONObject(i);
			if(asset.getString("content_type")
					.equals("application/vnd.android.package-archive")) {
				UpdateInfo info = new UpdateInfo();
				info.url = url;
				info.versionName = latestVersion;
				return info;
			}
		}

		return null;
	}

	/**
	 * Compares two version strings. 
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical 
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1 a string of ordinal numbers separated by decimal points. 
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
	 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
	 *         The result is zero if the strings are _numerically_ equal.
	 *         
	 * http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
	 */
	public int versionCompare(String str1, String str2)
	{
	    String[] vals1 = str1.split("\\.");
	    String[] vals2 = str2.split("\\.");
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) 
	    {
	      i++;
	    }
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length) 
	    {
	        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
	        return Integer.signum(diff);
	    }
	    // the strings are equal or one string is a substring of the other
	    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
	    else
	    {
	        return Integer.signum(vals1.length - vals2.length);
	    }
	}
	
	public interface OnUpdateListener {
		public void updateAvailable(UpdateInfo info);
	}
}
