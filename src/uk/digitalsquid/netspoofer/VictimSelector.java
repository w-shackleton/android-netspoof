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

package uk.digitalsquid.netspoofer;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.config.NetHelpers;
import uk.digitalsquid.netspoofer.spoofs.SpoofData;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VictimSelector extends Activity implements OnClickListener, LogConf {
	public static final String EXTRA_SPOOFDATA = "uk.digitalsquid.netspoofer.VictimSelector.SPOOFDATA";
	
	private SpoofData spoof;
	
	private ProgressBar scanProgressBar;
	private TextView scanProgressText;
	private Button scanProgressRefresh;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
		
		final Intent myStarter = getIntent();
		spoof = (SpoofData) myStarter.getSerializableExtra(EXTRA_SPOOFDATA);
		if(spoof == null) {
			Log.e(TAG, "Incorrect data given in intent, finishing");
			finish();
		}
		
		if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoChooseVictim", false)) {
			// Auto advance as user requested.
			goToNextStep(null);
			return;
		}
		
		setContentView(R.layout.victimselector);
		
		victimListAdapter = new VictimListAdapter();
		final ListView victimList = (ListView) findViewById(R.id.victimList);
		victimList.setAdapter(victimListAdapter);
		victimList.setOnItemClickListener(victimListAdapter);
		
		scanProgressBar = (ProgressBar) findViewById(R.id.scanProgressBar);
		scanProgressText = (TextView) findViewById(R.id.scanProgressText);
		scanProgressRefresh = (Button) findViewById(R.id.scanProgressRefresh);
		scanProgressRefresh.setOnClickListener(this);
		
		startScanners();
		hostnameFinder.execute();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "VictimSelector stopping");
		hostnameFinder.cancel(false);
	}
	
	private VictimListAdapter victimListAdapter;
	
	public static class Victim implements Comparable<Victim>, Serializable {
		private static final long serialVersionUID = -8815727249378333391L;
		private final InetAddress ip;
		private String name;
		
		public Victim(InetAddress ip) {
			this.ip = ip;
		}

		public InetAddress getIp() {
			return ip;
		}

		public String getIpString() {
			return ip.getHostAddress();
		}
		
		/**
		 * Gets the predetermined hostname. This will initially be <code>null</code>, but will be filled in later by the lookup thread.
		 * @return
		 */
		public String getHostname() {
			return name;
		}
		
		public void setHostname(String name) {
			this.name = name;
		}
		
		@Override
		public int compareTo(Victim another) {
			byte[] me = ip.getAddress();
			byte[] other = another.getIp().getAddress();
			for(int i = 0; i < me.length && i < other.length; i++) {
				if(me[i] != other[i]) {
					return me[i] - other[i];
				}
			}
			return 0;
		}
	}
	
	private class VictimListAdapter extends BaseAdapter implements OnItemClickListener {
		private final LayoutInflater inflater;
		List<Victim> victims = new ArrayList<Victim>();
		
		private static final int ITEM_ALL = 0;
		private static final int ITEM_OTHER = 1;
		
		public VictimListAdapter() {
			inflater = LayoutInflater.from(VictimSelector.this);
		}
		
		@Override
		public int getCount() {
			return victims.size() + 2; // 2 for all + other
		}

		@Override
		public Victim getItem(int position) {
			if(position < 2) return null;
			return victims.get(position - 2);
		}

		@Override
		public long getItemId(int position) {
			if(position < 2) return position;
			return position - 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
	        if (convertView == null) {
	            convertView = inflater.inflate(R.layout.victimlistitem, null);
	            
	            holder = new ViewHolder();
		        holder.vIp = (TextView) convertView.findViewById(R.id.victimIp);
		        holder.vText = (TextView) convertView.findViewById(R.id.victimExtraText);
		        
	            convertView.setTag(holder);
	        } else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        switch(position) {
	        case ITEM_ALL:
        		holder.vIp.setText(R.string.allDevices);
        		holder.vText.setText(R.string.allDevicesDesc);
        		convertView.setEnabled(true);
	        	break;
	        case ITEM_OTHER:
	        	holder.vIp.setText(R.string.otherDevice);
	        	holder.vText.setText(R.string.otherDeviceDesc);
        		convertView.setEnabled(true);
	        	break;
        	default:
	        	holder.vIp.setText(getItem(position).ip.getHostAddress());
	        	holder.vText.setText(getItem(position).getHostname());
        		break;
	        }
	        return convertView;
		}
		
		private class ViewHolder {
			TextView vIp;
			TextView vText;
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			Victim victim = getItem(position);
			switch(position) {
			case ITEM_ALL:
				// null means spoof everyone.
				goToNextStep(null);
				break;
			case ITEM_OTHER:
				showDialog(DIALOG_GETIP);
				break;
			default:
				goToNextStep(victim);
				break;
			}
		}
		
		@SuppressWarnings("unused")
		public void addDeviceToList(Victim victim) {
			victims.add(victim);
			Collections.sort(victims);
			notifyDataSetChanged();
		}
		
		public void addDevicesToList(Victim[] victimList) {
			for(Victim victim : victimList) {
				victims.add(victim);
			}
			Collections.sort(victims);
			notifyDataSetChanged();
		}
		
		public void clearDeviceList() {
			victims.clear();
			notifyDataSetChanged();
		}
	}
	
	private IPScanner[] scanners;
	
	private class IPScanner extends AsyncTask<Void, Victim, Void> {
		
		private final long ipFrom, ipTo;
		
		private boolean running = true;
		
		public IPScanner(long ipFrom, long ipTo) {
			this.ipFrom = ipFrom;
			this.ipTo = ipTo;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, String.format("Starting scanning IPs %d to %d.", ipFrom, ipTo));
			for(long ip = ipFrom; ip < ipTo; ip++) {
				try {
					InetAddress addr = NetHelpers.reverseInetFromInt(ip);
					if(addr.isReachable(100)) {
						Victim v = new Victim(addr);
						hostnameFindQueue.add(v); // Get hostname async-ly
						publishProgress(v);
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Victim... victims) {
			victimListAdapter.addDevicesToList(victims);
		}
		
		@Override
		protected void onPostExecute(Void ret) {
			Log.d(TAG, String.format("Finished scanning IPs %d to %d.", ipFrom, ipTo));
			running = false;
			onScannerFinish();
		}

		public boolean isRunning() {
			return running;
		}
	}
	
	private void onScannerFinish() {
		boolean allFinished = true;
		for(IPScanner scanner : scanners) {
			if(scanner.isRunning()) allFinished = false;
		}
		if(allFinished) {
			Log.i(TAG, "All scanners finished");
			scanProgressBar.setVisibility(View.INVISIBLE);
			scanProgressText.setText("");
			scanProgressRefresh.setEnabled(true);
			
			setProgressBarIndeterminateVisibility(false);
		}
	}
	
	private void startScanners() {
		victimListAdapter.clearDeviceList();
		// Start IP Scanners
		long ip = spoof.getMyIpReverseInt();
		long baseIp = ip & spoof.getMySubnetReverseInt(); // Bottom possble IP
		long topIp = baseIp | (0xffffffffL >> spoof.getMySubnet()); // Top possible IP
		/*
		 * A little note here. The logical way to store IP addrs in ints (the 'reverse' commands)
		 * is the opposite to the one used by Android functions (the non reverse ones). The difference
		 * is the endianness.
		 */
		
		// Create as many scanners as user asked.
		int numScanners;
		try {
		numScanners = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("ipScanThreads", "4"));
		} catch(NumberFormatException e) {
			numScanners = 4;
		}
		if(numScanners < 1) numScanners = 1;
		if(numScanners > 40) numScanners = 40;
		scanners = new IPScanner[numScanners];
		
		long range = (topIp - baseIp) / scanners.length;
		for(int i = 0; i < scanners.length - 1; i++) {
			scanners[i] = new IPScanner(baseIp + (range * i), baseIp + (range * (i+1)));
		}
		// Make sure that none are left out.
		scanners[scanners.length - 1] = new IPScanner(baseIp + (range * (scanners.length - 1)), topIp + 1);
		
		// Start
		for(IPScanner scanner : scanners) {
			scanner.execute();
		}
		
		scanProgressBar.setVisibility(View.VISIBLE);
		scanProgressText.setText(R.string.scanning);
		scanProgressRefresh.setEnabled(false);
		
		setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.scanProgressRefresh:
			startScanners();
			break;
		}
	}
	
	private static final int DIALOG_GETIP = 0;
	
	@Override
	public Dialog onCreateDialog(int id, Bundle args) {
		super.onCreateDialog(id, args);
		switch(id) {
		case DIALOG_GETIP:
			LayoutInflater inflater = LayoutInflater.from(this);
			Builder builder = new Builder(this);
			View layout = inflater.inflate(R.layout.customip, null);
			builder.setView(layout).setTitle(R.string.enterip);
			final EditText ip = (EditText) layout.findViewById(R.id.ipBox);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						goToNextStep(new Victim(InetAddress.getByName(ip.getText().toString())));
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Log.e(TAG, "Incorrect IP given.");
						Toast.makeText(VictimSelector.this, "Please enter a valid IP address", Toast.LENGTH_LONG).show();
					}
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { }
			});
			
			return builder.show();
		default:
			return null;
		}
	}
	
	/**
	 * Goes to the next step of the setup process. <code>null</code> means everyone.
	 * @param victim
	 */
	private void goToNextStep(Victim victim) {
		spoof.setVictim(victim);
		Intent intent = new Intent(this, SpoofRunning.class); 
		intent.putExtra(SpoofRunning.EXTRA_SPOOFDATA, spoof);
		startActivity(intent);
	}
	
	LinkedBlockingQueue<Victim> hostnameFindQueue = new LinkedBlockingQueue<VictimSelector.Victim>();
	
	private final AsyncTask<Void, Victim, Void> hostnameFinder = new AsyncTask<Void, VictimSelector.Victim, Void>() {
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Starting to find hostnames");
			while(!isCancelled()) {
				Victim victim = null;
				try {
					victim = hostnameFindQueue.poll(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(victim != null) {
					Log.v(TAG, "Finding hostname for " + victim.getIp().getHostAddress());
					victim.setHostname(victim.getIp().getHostName());
					Log.v(TAG, "IP " + victim.getIp().getHostAddress() + " = " + victim.getHostname());
					publishProgress(victim);
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Victim... victim) {
			super.onProgressUpdate(victim);
			victimListAdapter.notifyDataSetChanged();
		}
	};
}
