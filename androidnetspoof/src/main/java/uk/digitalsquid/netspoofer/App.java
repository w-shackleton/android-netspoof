package uk.digitalsquid.netspoofer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import uk.digitalsquid.netspoofer.config.LogConf;

public class App extends Application implements LogConf{
    
    /*
     * A quick note on Network Spoofer's use of Google Analytics:
     * This app doesn't stalk you, doesn't track you or any strange things.
     * I (the developer) simply want to get an idea of how many users I have.
     * If you don't want to be counted in my statistics then you can disable
     * this in the options panel.
     */
    
    private Tracker tracker;
    public synchronized Tracker getTracker() {
        if(tracker == null) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(this);
            // Disable analytics if requested by user.
            if(prefs.getBoolean("notrack", false)) {
                Log.i(TAG, "Stats disabled, returning null for Tracker");
                return null;
            }

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.netspoof_stats);
        }
        return tracker;
    }
}
