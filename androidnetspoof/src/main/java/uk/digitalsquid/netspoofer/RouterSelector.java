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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.config.NetHelpers;
import uk.digitalsquid.netspoofer.config.NetHelpers.GatewayData;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;

public class RouterSelector extends Activity implements OnClickListener, LogConf {
	public static final String EXTRA_SPOOF = "uk.digitalsquid.netspoofer.RouterSelector.SPOOF";
	
	private Spoof spoof;
	
	private WifiManager wm;
	private WifiLock wL;
	
	private NetworkInterface wifiIface;
	private InetAddress wifiIP;
	private GatewayData wifiGateway;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.routerselector);
		
		spoof = (Spoof) getIntent().getSerializableExtra(EXTRA_SPOOF);
		if(spoof == null) {
			Log.e(TAG, "No spoof given in intent, finishing");
			finish();
		}
		
		gatewayListAdapter = new GatewayListAdapter();
		ListView gatewayList = (ListView) findViewById(R.id.routerList);
		gatewayList.setAdapter(gatewayListAdapter);
		gatewayList.setOnItemClickListener(gatewayListAdapter);
		
		findViewById(R.id.wifiSettings).setOnClickListener(this);
		
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wL = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "android-netspoof");
		wL.acquire();
        
        if(wm.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) getAndSetWifiInfo();
		
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // wifiFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        // wifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        // wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiListener, wifiFilter);
		
		// Google analytics
		Tracker t = ((App)getApplication()).getTracker();
		if(t != null) {
			t.setScreenName(getClass().getCanonicalName());
			t.send(new HitBuilders.AppViewBuilder().build());
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(wifiListener);
		wL.release();
		super.onDestroy();
	}
	
	BroadcastReceiver wifiListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if(info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_WIMAX) { // Only wifi
					if(info.getState() == State.CONNECTED) {
						getAndSetWifiInfo();
					}
					else gatewayListAdapter.setWifiGateway(null);
				}
			} else { // Some sort of wifi thing
				switch(wm.getConnectionInfo().getSupplicantState()) {
				case COMPLETED:
					break;
				default:
					// gatewayListAdapter.setWifiGateway(null);
					break;
				}
			}
		}
	};
	
	private GatewayListAdapter gatewayListAdapter;
	
	private class GatewayListAdapter extends BaseAdapter implements OnItemClickListener {
		private final LayoutInflater inflater;
		public static final int ITEM_DEFAULT = 0;
		public static final int ITEM_PASSIVE = 1;
		public static final int ITEM_OTHER = 2;
		
		private boolean wifiReady = false;
		private String wifiGateway = "";
		
		public void setWifiGateway(String gateway) {
			wifiReady = (gateway != null);
			wifiGateway = gateway;
			notifyDataSetChanged();
		}
		
		public GatewayListAdapter() {
			inflater = LayoutInflater.from(RouterSelector.this);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = inflater.inflate(R.layout.routerlistitem, null);
	        }
	        TextView name = (TextView) convertView.findViewById(R.id.routerName);
	        TextView description = (TextView) convertView.findViewById(R.id.routerDescription);
	        
	        switch(position) {
	        case ITEM_DEFAULT:
	        	if(wifiReady) {
	        		name.setText("Default gateway (recommended)");
	        		description.setText("Autodetected " + wifiGateway);
	        		convertView.setEnabled(true);
	        	} else {
	        		name.setText("Waiting for Wifi...");
	        		description.setText("Please connect to a Wifi network to automatically get the router's address");
	        		convertView.setEnabled(false);
	        	}
	        	break;
	        case ITEM_PASSIVE:
	        	name.setText("Run passively");
	        	description.setText("Run the spoof passively; run the process, but not for anyone. Choose this to test spoofs or if Wifi tethering. Some settings may not completely work like this.");
        		convertView.setEnabled(true);
	        	break;
	        case ITEM_OTHER:
	        	name.setText("Other");
	        	description.setText("Choose custom IP addresses to use as the default gateway, IP and interface");
        		convertView.setEnabled(true);
	        	break;
	        }
	        return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			switch(position) {
			case ITEM_DEFAULT:
				if(view.isEnabled()) {
					goToNextStep(RouterSelector.this.wifiIP.getHostAddress(), RouterSelector.this.wifiGateway.getSubnet(), RouterSelector.this.wifiGateway.getGateway().getHostAddress(), RouterSelector.this.wifiIface.getDisplayName());
				}
				break;
			case ITEM_PASSIVE:
					goToNextStep(true);
				break;
			case ITEM_OTHER:
				showDialog(DIALOG_CUSTOMIP);
				break;
			}
		}
	}
	
	private static final int DIALOG_CUSTOMIP = 0;
	
	@Override
	public Dialog onCreateDialog(int id, Bundle bundle) {
		switch(id) {
		case DIALOG_CUSTOMIP:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			LayoutInflater inflater = LayoutInflater.from(this);
			LinearLayout view = (LinearLayout)inflater.inflate(R.layout.otheripdialog, null);
			
			final EditText
				myIp = (EditText) view.findViewById(R.id.myIpText),
				subnetMask = (EditText) view.findViewById(R.id.subnetMaskText),
				routerIp = (EditText) view.findViewById(R.id.routerIpText),
				myIf = (EditText) view.findViewById(R.id.myIfText);
			
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String sMyIp = myIp.getText().toString();
					String sMyIf = myIf.getText().toString();
					String sRouterIp = routerIp.getText().toString();
					String sSubnetMask = subnetMask.getText().toString();
					
					// Check if valid
					try {
						InetAddress.getByName(sMyIp);
						InetAddress.getByName(sRouterIp);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Toast.makeText(getBaseContext(), "Invalid IP addresses entered. Please enter valid information.", Toast.LENGTH_LONG).show();
						return;
					}
					goToNextStep(sMyIp, sSubnetMask, sRouterIp, sMyIf);
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).setTitle(R.string.customIpSettings);
			return builder.create();
		default:
			return null;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.wifiSettings:
			try {
				startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			} catch(ActivityNotFoundException e) {
				Toast.makeText(getBaseContext(), "Couldn't load Wifi settings.", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	
	private boolean autoNextStepStarted = false;
	
	private void getAndSetWifiInfo() {
		int ip = wm.getConnectionInfo().getIpAddress();
		try {
			wifiIP = NetHelpers.inetFromInt(ip);
			wifiIface = NetHelpers.getIface(wifiIP);
			wifiGateway = NetHelpers.getDefaultGateway(wifiIface);
			gatewayListAdapter.setWifiGateway(wifiGateway.getGateway().getHostAddress());
			if(!autoNextStepStarted && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoChooseRouter", false)) {
				autoNextStepStarted = true;
				// Auto start if user specified to.
				goToNextStep(RouterSelector.this.wifiIP.getHostAddress(), RouterSelector.this.wifiGateway.getSubnet(), RouterSelector.this.wifiGateway.getGateway().getHostAddress(), RouterSelector.this.wifiIface.getDisplayName());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error getting wifi info. Please see log for more info.", Toast.LENGTH_LONG).show();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error getting wifi info. Please see log for more info.", Toast.LENGTH_LONG).show();
		}
	}
	
	private void goToNextStep(boolean runningPassively) {
		Intent intent = new Intent(this, SpoofRunning.class); 
		intent.putExtra(SpoofRunning.EXTRA_SPOOFDATA, new SpoofData(spoof, true));
		startActivity(intent);
	}
	
	private void goToNextStep(String myIp, String mySubnet, String gatewayIp, String wifiIface) {
		Intent intent = new Intent(this, VictimSelector.class);
		try {
			intent.putExtra(VictimSelector.EXTRA_SPOOFDATA, new SpoofData(spoof, myIp, mySubnet, wifiIface, gatewayIp));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't decode IP addresses.");
			Toast.makeText(this, "Error decoding IP addresses given. Perhaps you mistyped something or aren't connected to Wifi?", Toast.LENGTH_LONG).show();
			return;
		}
		startActivity(intent);
	}
}
