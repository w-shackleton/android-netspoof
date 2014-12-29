package uk.digitalsquid.netspoofer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class Config {
	private static final String AD_UNIT_ID =
			"ca-app-pub-2071510574190457/6678438528";
	public static void configureRunningPage(Activity activity) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(activity);
		if(prefs.getBoolean("noadverts", false)) return;

		FrameLayout frame = (FrameLayout) activity.findViewById(R.id.banner);
		AdView adView = new AdView(activity);
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId(AD_UNIT_ID);

		frame.addView(adView);
		
		AdRequest request = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("1599F9EC2E068E3FBCC51A003B22EE40")
				.build();
		
		adView.loadAd(request);
	}
}
