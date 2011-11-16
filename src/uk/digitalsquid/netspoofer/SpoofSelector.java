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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.digitalsquid.netspoofer.NetSpoofService.NetSpoofServiceBinder;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.misc.CheckedLinearLayout;
import uk.digitalsquid.netspoofer.servicestatus.SpoofList;
import uk.digitalsquid.netspoofer.spoofs.Spoof;
import uk.digitalsquid.netspoofer.spoofs.Spoof.OnExtraDialogDoneListener;
import uk.digitalsquid.netspoofer.spoofs.SquidScriptSpoof;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows a list of possible spoofs, either a single one or a multi choice.
 * @author william
 *
 */
public class SpoofSelector extends Activity implements OnClickListener, OnItemClickListener, LogConf {
	ProgressDialog startingProgress;
	
	private ListView spoofList;
	
	boolean haveSpoofList = false;
	boolean gettingSpoofList = false;
	
	boolean multiChoice = false;
	List<Spoof> multiSpoofList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(Intent.ACTION_PICK.equals(getIntent().getAction())) { // activity for result
			multiChoice = true;
			setContentView(R.layout.spoofmultiselector);
			findViewById(R.id.ok).setOnClickListener(this);
			findViewById(R.id.cancel).setOnClickListener(this);
		} else {
			setContentView(R.layout.spoofselector);
		}
	    startService(new Intent(this, NetSpoofService.class));
	    
	    spoofListAdapter = new SpoofListAdapter();
	    spoofList = (ListView) findViewById(R.id.spoofList);
	    if(!multiChoice) {
		    spoofList.setAdapter(spoofListAdapter);
	    	spoofList.setOnItemClickListener(this);
	    } else {
			spoofList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    	spoofList.setItemsCanFocus(false);
	    }
	    
	    statusFilter = new IntentFilter();
	    statusFilter.addAction(NetSpoofService.INTENT_STATUSUPDATE);
	    statusFilter.addAction(NetSpoofService.INTENT_SPOOFLIST);
		registerReceiver(statusReceiver, statusFilter);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
        stopService(new Intent(this, NetSpoofService.class));
		unregisterReceiver(statusReceiver);
	}

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        bindService(new Intent(this, NetSpoofService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (service != null) {
            unbindService(mConnection);
            service = null;
        }
    }
	
	private NetSpoofService service;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			NetSpoofServiceBinder binder = (NetSpoofServiceBinder) service;
            SpoofSelector.this.service = binder.getService();
            
            switch(SpoofSelector.this.service.getStatus()) {
            case NetSpoofService.STATUS_LOADING:
            	showStartingDialog();
            	break;
            case NetSpoofService.STATUS_LOADED:
            	if(!gettingSpoofList) {
            		gettingSpoofList = true;
            		SpoofSelector.this.service.requestSpoofs();
            	}
            	break;
            }
		}
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
		}
	};
	
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(NetSpoofService.INTENT_STATUSUPDATE)) {
				switch(intent.getIntExtra(NetSpoofService.INTENT_EXTRA_STATUS, NetSpoofService.STATUS_FINISHED)) {
				case NetSpoofService.STATUS_LOADING:
					showStartingDialog();
					break;
				case NetSpoofService.STATUS_LOADED:
					if(startingDialog != null) startingDialog.cancel();
	            	if(!gettingSpoofList) {
	            		gettingSpoofList = true;
	            		if(SpoofSelector.this.service != null) SpoofSelector.this.service.requestSpoofs();
	            	}
					break;
				case NetSpoofService.STATUS_FAILED:
					try {
						if(startingDialog != null) startingDialog.cancel();
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "Couldn't close dialog, ignoring...");
					}
					showDialog(DIALOG_FAIL_LOAD);
					break;
				}
			} else if(intent.getAction().equals(NetSpoofService.INTENT_SPOOFLIST)) {
				SpoofList spoofs = (SpoofList) intent.getSerializableExtra(NetSpoofService.INTENT_EXTRA_SPOOFLIST);
				if(multiChoice) {
					ArrayList<Spoof> filteredSpoofs = new ArrayList<Spoof>();
					for(Spoof s : spoofs.getSpoofs()) {
						if(s instanceof SquidScriptSpoof) {
							filteredSpoofs.add(s);
						}
					}
					spoofList.setAdapter(new ArrayAdapter<Spoof>(SpoofSelector.this, android.R.layout.simple_list_item_multiple_choice, filteredSpoofs));
					multiSpoofList = spoofs.getSpoofs();
				} else {
					spoofListAdapter.setSpoofs(spoofs.getSpoofs());
				}
			}
		}
	};
	
	private ProgressDialog startingDialog;
	
	private void showStartingDialog() {
		if(startingDialog != null) if(startingDialog.isShowing()) return;
		startingDialog = new ProgressDialog(this);
		startingDialog.setTitle(R.string.loading);
		startingDialog.setMessage("Starting environment... This should take a few seconds");
		startingDialog.setCancelable(false);
		startingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		    @Override
		    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		        if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
		            return true;
		        }
		        return false;
		    }
		});
		startingDialog.show();
	}
	
	private SpoofListAdapter spoofListAdapter;
	
	private class SpoofListAdapter extends BaseAdapter {
		private final LayoutInflater inflater;
		
		private List<Spoof> spoofs;
		
		public SpoofListAdapter() {
			inflater = LayoutInflater.from(SpoofSelector.this);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            if(spoofs == null) {
		        if (convertView == null) {
		            convertView = inflater.inflate(R.layout.listloadingitem, null);
		            convertView.setEnabled(false);
		        }
            } else {
		        if (convertView != null) {
		        	if(convertView.findViewById(R.id.spoofTitle) == null) // Must be other view
			            convertView = inflater.inflate(R.layout.spoofitem, null);
		        } else {
		            convertView = inflater.inflate(R.layout.spoofitem, null);
		        }
		        // NOTE: Not used, currently using ArrayAdapter.
	            if(multiChoice) {
	            	convertView.findViewById(R.id.checkbox).setVisibility(View.VISIBLE);
	            	((CheckedLinearLayout)convertView).setCheckable((Checkable) convertView.findViewById(R.id.checkbox));
	            } else {
	            	convertView.findViewById(R.id.checkbox).setVisibility(View.GONE);
	            }
		        TextView title = (TextView) convertView.findViewById(R.id.spoofTitle);
		        TextView description = (TextView) convertView.findViewById(R.id.spoofDescription);
		        
	        	title.setText(spoofs.get(position).getTitle());
	        	description.setText(spoofs.get(position).getDescription());
            }

            return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Spoof getItem(int position) {
			if(spoofs == null) return null;
			return spoofs.get(position);
		}
		
		@Override
		public int getCount() {
			if(spoofs == null) return 1;
			return spoofs.size();
		}
		
		public void setSpoofs(List<Spoof> spoofs) {
			if(!multiChoice) {
				this.spoofs = spoofs;
			} else {
				// Remove non squid spoofs
				this.spoofs = new LinkedList<Spoof>();
				for(Spoof spoof : spoofs) {
					if(spoof instanceof SquidScriptSpoof) {
						this.spoofs.add(spoof);
					}
				}
			}
			notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		final Spoof spoof = spoofListAdapter.getItem(position);
		if(spoof == null) return;
		
		// Start processing spoof etc.
		final OnExtraDialogDoneListener onDone = new OnExtraDialogDoneListener() {
			@Override
			public void onDone() {
				Log.d(TAG, "Dialog done, continuing");
				Intent intent = new Intent(SpoofSelector.this, RouterSelector.class);
				intent.putExtra(RouterSelector.EXTRA_SPOOF, spoof);
				startActivity(intent);
			}
		};
		Dialog optDialog = spoof.displayExtraDialog(this, onDone);
		if(optDialog == null) {
			// Only execute activity if no dialog. TODO: Make this logic better?
			Intent resultIntent = spoof.activityForResult(this);
			if(resultIntent != null) {
				activityResultSpoof = spoof;
				startActivityForResult(resultIntent, ACTIVITY_REQUEST_CUSTOM);
			}
			else onDone.onDone(); // Nothing to do
		} else { // Let dialog do so.
			optDialog.show();
		}
	}
	
	private static final int DIALOG_FAIL_LOAD = 1;
	
	/**
	 * The ID used for result intents returned by custom spoofs.
	 */
	private static final int ACTIVITY_REQUEST_CUSTOM = 2;
	/**
	 * The ID used for result intents returned by custom spoofs, part 2.
	 */
	private static final int ACTIVITY_REQUEST_CUSTOM_2 = 3;
	
	private Spoof activityResultSpoof;
	
	@Override
	public Dialog onCreateDialog(int id, Bundle bundle) {
		super.onCreateDialog(id, bundle);
		switch(id) {
		case DIALOG_FAIL_LOAD:
			Builder builder = new Builder(this);
			builder.setTitle(R.string.loadfailedtitle);
			builder.setMessage(R.string.loadfailed);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			return builder.create();
		default: return null;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case ACTIVITY_REQUEST_CUSTOM:
			if(activityResultSpoof != null) {
				if(resultCode == RESULT_OK) {
					if(activityResultSpoof.activityFinished(getBaseContext(), data)) { // If true, continue
						Log.d(TAG, "Activity done, continuing");
						// NEW: Run 2nd activity if needed.
						Intent part2 = activityResultSpoof.activityForResult2(this);
						if(part2 != null) {
							startActivityForResult(part2, ACTIVITY_REQUEST_CUSTOM_2);
						} else {
							Intent intent = new Intent(SpoofSelector.this, RouterSelector.class);
							intent.putExtra(RouterSelector.EXTRA_SPOOF, activityResultSpoof);
							startActivity(intent);
						}
					}
				}
			}
			break;
		case ACTIVITY_REQUEST_CUSTOM_2:
			if(activityResultSpoof != null) {
				if(resultCode == RESULT_OK) {
					if(activityResultSpoof.activityFinished2(getBaseContext(), data)) { // If true, continue
						Log.d(TAG, "Activity 2 done, continuing");
						Intent intent = new Intent(SpoofSelector.this, RouterSelector.class);
						intent.putExtra(RouterSelector.EXTRA_SPOOF, activityResultSpoof);
						startActivity(intent);
					}
				}
			}
			break;
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.ok:
			// Process selections
			SparseBooleanArray arr = spoofList.getCheckedItemPositions();
			LinkedList<Spoof> selected = new LinkedList<Spoof>();
			if(multiSpoofList != null) {
				for(int i = 0; i < multiSpoofList.size(); i++) {
					if(arr.get(i)) {
						selected.add(multiSpoofList.get(i));
					}
				}
			}
			if(selected.size() != 0) {
				Intent result = new Intent();
				result.putExtra("uk.digitalsquid.netspoof.SpoofSelector.spoofs", selected);
				setResult(RESULT_OK, result);
				finish();
			} else {
				setResult(RESULT_CANCELED);
				Toast.makeText(this, "Please specify some spoofs to use", Toast.LENGTH_LONG).show();
				finish();
			}
			break;
		case R.id.cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}
}
