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

import java.io.FileNotFoundException;

import uk.digitalsquid.netspoofer.config.FileFinder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class Preferences extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		SettingsFragment frag = new SettingsFragment();
		transaction.replace(android.R.id.content, frag);
		transaction.commit();
	}
	
	public static class SettingsFragment extends PreferenceFragment implements
                OnSharedPreferenceChangeListener,
                OnPreferenceChangeListener {

		SharedPreferences prefs;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			prefs = getPreferenceManager().getSharedPreferences();
			prefs.registerOnSharedPreferenceChangeListener(this);
			
			findPreference("noadverts").setOnPreferenceChangeListener(this);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			prefs.unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if(key.equals("noadverts")) {
				CheckBoxPreference noAdverts =
						(CheckBoxPreference) findPreference("noadverts");
				noAdverts.setChecked(prefs.getBoolean("noadverts", false));
			}
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(!preference.hasKey()) return true;
			if(preference.getKey().equals("noadverts")) {
				CheckBoxPreference noAdvert = (CheckBoxPreference) preference;
				if(!noAdvert.isChecked()) { // False -> True, display dialog
			        FragmentManager fm = getFragmentManager();
			        AdvertDialog dialog = new AdvertDialog();
			        dialog.show(fm, "fragment_advert_disable");
			        return false;
				}
			}
			return true;
		}
	}
	
	public static class AdvertDialog extends DialogFragment implements OnClickListener {
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View view = inflater.inflate(R.layout.advert_dialog, container);
	        Button donate = (Button) view.findViewById(R.id.donate);
	        donate.setOnClickListener(this);
	        Button disable = (Button) view.findViewById(R.id.disable);
	        disable.setOnClickListener(this);
	        Button cancel = (Button) view.findViewById(R.id.cancel);
	        cancel.setOnClickListener(this);
	        getDialog().setTitle("Disable adverts?");

	        return view;
	    }

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.donate:
				PreferenceManager.getDefaultSharedPreferences(getActivity())
					.edit().putBoolean("noadverts", true).commit();
				getDialog().dismiss();
				Intent intent = new Intent(
						Intent.ACTION_VIEW, Uri.parse(
								"http://digitalsquid.co.uk/netspoof/donate"));
				startActivity(intent);
				break;
			case R.id.disable:
				PreferenceManager.getDefaultSharedPreferences(getActivity())
					.edit().putBoolean("noadverts", true).commit();
				getDialog().dismiss();
				break;
			case R.id.cancel:
				getDialog().dismiss();
				break;
			}
		}
	}
}
