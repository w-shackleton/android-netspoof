package uk.digitalsquid.netspoofer.spoofs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class IPRedirectSpoof extends Spoof {
	private static final long serialVersionUID = -7780822391880161592L;
	public static final String KITTENWAR = "205.196.209.62";
	
	private InetAddress host;
	
	public IPRedirectSpoof(String title, String description, String hostTo) throws UnknownHostException {
		super(title, description);
		if(hostTo == null) {
			host = null;
			return;
		}
		host = InetAddress.getByName(hostTo);
	}
	
	/**
	 * Constructor that leaves host undefined, and shows dialog later.
	 * @param title
	 * @param description
	 */
	public IPRedirectSpoof(String title, String description) {
		super(title, description);
		host = null;
	}
	
	/**
	 * Returns the spoofing debian shell command.
	 * @param victim The victim of the attack; <code>null</code> means everyone on the subnet.
	 * @param router The router to intercept packets for.
	 */
	@Override
	public String getSpoofCmd(String victim, String router) {
		if(victim == null) victim = "*";
		return String.format("spoof %s %s 1 %s", victim, router, host.getHostAddress());
	}

	@Override
	public String getStopCmd() {
		return "\n"; // Enter stops this
	}

	@Override
	public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
		if(host == null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
	
			alert.setTitle("Website redirect");
			alert.setMessage("Please enter a website to redirect to.");
	
			final EditText input = new EditText(context);
			alert.setView(input);
	
			alert.setPositiveButton("Done", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						if(input.getText().toString().equals("")) throw new UnknownHostException("Blank host");
						host = InetAddress.getByName(input.getText().toString());
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Toast.makeText(context, "Couldn't find specified website, using kittenwar.", Toast.LENGTH_LONG).show();
						try {
							host = Inet4Address.getByName(KITTENWAR);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						}
					}
					onDone.onDone();
				}
			});
	
			return alert.create();
		}
		else return null;
	}
}
