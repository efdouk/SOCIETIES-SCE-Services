package com.asocom.components;

import com.asocom.activities.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class OptionsLeave extends LinearLayout {

	/**
	 * Esta variable copia la id del layout clikable util para los listener
	 */
	private int code;

	public OptionsLeave(Context context) {
		super(context);
		init();

	}

	private void init() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(
				infService);
		li.inflate(R.layout.options_leave, this, true);
		LinearLayout ll = (LinearLayout) findViewById(R.id.options_icon);
		code = ll.hashCode();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
