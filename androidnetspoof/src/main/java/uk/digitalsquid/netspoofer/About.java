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

package uk.digitalsquid.netspoofer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class About extends Activity implements OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        findViewById(R.id.website).setOnClickListener(this);
        findViewById(R.id.donate).setOnClickListener(this);
        findViewById(R.id.reportBug).setOnClickListener(this);
        
        // Google analytics
        Tracker t = ((App)getApplication()).getTracker();
        if(t != null) {
            t.setScreenName(getClass().getCanonicalName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
        case R.id.website:
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/netspoof"));
            startActivity(intent);
            break;
        case R.id.reportBug:
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/w-shackleton/android-netspoof/issues/new"));
            startActivity(intent);
            break;
        case R.id.donate:
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/netspoof/donate"));
            startActivity(intent);
            break;
        }
    }
}
