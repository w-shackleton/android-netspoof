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

package uk.digitalsquid.netspoofer.report;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.io.File;
import java.io.IOException;

import uk.digitalsquid.netspoofer.Preferences;
import uk.digitalsquid.netspoofer.R;
import uk.digitalsquid.netspoofer.config.LogConf;

public class DeviceReportActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_report);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Preferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener, LogConf {

        public PlaceholderFragment() {
        }

        private CheckBox logs, allLogs, networkConfig;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_device_report, container, false);
            logs = (CheckBox) rootView.findViewById(R.id.logs);
            allLogs = (CheckBox) rootView.findViewById(R.id.all_logs);
            networkConfig = (CheckBox) rootView.findViewById(R.id.network_config);
            rootView.findViewById(R.id.submit).setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.submit:
                    DeviceReport report = new DeviceReport(
                            getActivity(),
                            logs.isChecked(),
                            allLogs.isChecked(),
                            networkConfig.isChecked());
                    File file;
                    try {
                        file = report.generate();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to generate report", e);
                        break;
                    }
                    Uri contentUri = FileProvider.getUriForFile(getActivity(),
                            "uk.digitalsquid.netspoofer.report",
                            file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType("application/zip");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent,
                            getActivity().getString(R.string.send_via)));
                    break;
            }
        }
    }
}
