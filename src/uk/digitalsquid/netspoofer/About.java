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

package uk.digitalsquid.netspoofer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class About extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		findViewById(R.id.website).setOnClickListener(this);
		findViewById(R.id.devsite).setOnClickListener(this);
		findViewById(R.id.contactDev).setOnClickListener(this);
		findViewById(R.id.reportBug).setOnClickListener(this);
		findViewById(R.id.udternet).setOnClickListener(this);
		findViewById(R.id.squidscripts).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()) {
		case R.id.website:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/netspoof"));
			startActivity(intent);
			break;
		case R.id.devsite:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk"));
			startActivity(intent);
			break;
		case R.id.contactDev:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/contact"));
			startActivity(intent);
			break;
		case R.id.reportBug:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://URLFORREPORTBUG/"));
			startActivity(intent);
			break;
		case R.id.udternet:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ex-parrot.com/pete/upside-down-ternet.html"));
			startActivity(intent);
			break;
		case R.id.squidscripts:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://g0tmi1k.blogspot.com/2011/04/video-playing-with-traffic-squid.html"));
			startActivity(intent);
			break;
		}
	}
}
