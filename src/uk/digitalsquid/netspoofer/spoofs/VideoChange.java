/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
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

package uk.digitalsquid.netspoofer.spoofs;

import java.util.Map;

import uk.digitalsquid.netspoofer.YoutubeSelector;
import android.content.Context;
import android.content.Intent;

/**
 * A custom version of the Google spoof which allows the user to enter their own google search query.
 * @author william
 *
 */
public class VideoChange extends SquidScriptSpoof {
	private static final long serialVersionUID = 8490503138296852028L;

	public VideoChange() {
		super("Custom Youtube video", "Change all videos on Youtube to a custom one", "rickroll.sh");
	}
	
	private String videoURL;
	
	@Override
	public Intent activityForResult(Context context) {
		return new Intent(context, YoutubeSelector.class);
	}
	
	@Override
	public boolean activityFinished(Context context, Intent result) {
		super.activityFinished(context, result);
		videoURL = result.getStringExtra(YoutubeSelector.CODE);
		if(videoURL == null) return false; // No video selected
		else return true;
	}
	
	@Override
	public Map<String, String> getCustomEnv() {
		Map<String, String> ret = super.getCustomEnv();
		ret.put("SPOOFVIDEOID", videoURL);
		return ret;
	}
}
