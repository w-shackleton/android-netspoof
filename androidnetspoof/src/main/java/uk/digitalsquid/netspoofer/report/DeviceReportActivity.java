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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

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

        private CheckBox info, logs, allLogs, networkConfig;
        private TextView codeView;
        private String code;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_device_report, container, false);
            info = (CheckBox) rootView.findViewById(R.id.device_info);
            logs = (CheckBox) rootView.findViewById(R.id.logs);
            allLogs = (CheckBox) rootView.findViewById(R.id.all_logs);
            networkConfig = (CheckBox) rootView.findViewById(R.id.network_config);

            codeView = (TextView) rootView.findViewById(R.id.code);
            try {
                code = new CHBSGenerator(getActivity()).generate();
            } catch (IOException e) {
                Log.e(TAG, "Failed to generate code", e);
                code = "correct-horse-battery-staple";
            }
            codeView.setText(code.replace('-', ' '));

            rootView.findViewById(R.id.submit).setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.submit:
                    DeviceReport report = new DeviceReport(
                            getActivity(),
                            code,
                            info.isChecked(),
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
                    shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "nsreport@digitalsquid.co.uk" });
                    startActivityForResult(Intent.createChooser(shareIntent,
                            getActivity().getString(R.string.send_via)), 1);
                    break;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case 1: // Email sent correctly
                    // Move on to final bit of UI
                    SuccessFragment fragment = new SuccessFragment();
                    Bundle args = new Bundle();
                    args.putString("code", code);
                    fragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();
                    break;
            }
        }
    }

    public static class SuccessFragment extends Fragment implements View.OnClickListener {
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_device_report_prompt, container, false);

            rootView.findViewById(R.id.copy).setOnClickListener(this);
            rootView.findViewById(R.id.submit).setOnClickListener(this);

            TextView codeView = (TextView) rootView.findViewById(R.id.code);
            codeView.setText(getCode());
            return rootView;
        }

        private String getCode() {
            return getArguments() != null ?
                    getArguments().getString("code") :
                    "correct-horse-battery-staple";
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.copy:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        android.content.ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("Network Spoofer report code", getCode()));
                    } else {
                        android.text.ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(getCode());
                    }
                    Toast.makeText(getActivity(), "Copied code to clipboard", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.submit:
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/w-shackleton/android-netspoof/issues/"));
                    startActivity(intent);
                    break;
            }
        }
    }
}
