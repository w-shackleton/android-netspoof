package uk.digitalsquid.netspoofer.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * A {@link LinearLayout} which forwards it checking abilities to a supplied subclass
 * @author william
 *
 */
public class CheckedLinearLayout extends LinearLayout implements Checkable {
	
	private Checkable check;

	public CheckedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {
		if(check != null) return check.isChecked();
		return false;
	}

	@Override
	public void setChecked(boolean arg0) {
		if(check != null) check.setChecked(arg0);
	}

	@Override
	public void toggle() {
		if(check != null) check.toggle();
	}
	
	/**
	 * Sets the subclass to derive from
	 * @param check
	 */
	public void setCheckable(Checkable check) {
		this.check = check;
	}
}
