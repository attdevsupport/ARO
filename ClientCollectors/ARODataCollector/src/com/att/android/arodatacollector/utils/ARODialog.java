package com.att.android.arodatacollector.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.att.android.arodatacollector.R;

public class ARODialog extends Activity implements OnClickListener {

	private Button buttonOK;
	private String title;
	private String message;
	private boolean wearable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle extra = getIntent().getExtras();
		title = extra.getString("TITLE");
		message = extra.getString("MESSAGE");
		
		testForWearable();

		doDialog();

	}

	/**
	 * determine if device is a wearable ie. vertical is equal to horizontal
	 */
	private void testForWearable() {
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();
		display = null;
		if (width < 300){
			wearable = true;
		}
	}

	private void doDialog() {
		
		if (wearable) {
			setContentView(R.layout.aro_detail_dialog_wear);
		} else {
			setContentView(R.layout.aro_detail_dialog);
		}

		if (title != null) {
			final TextView aroDetailDialogTitle = (TextView) findViewById(R.id.aro_detail_dialog_title);
			aroDetailDialogTitle.setText(title);
		}

		final TextView aroDetailDialogMessage = (TextView) findViewById(R.id.aro_detail_message_text);
		aroDetailDialogMessage.setText(message);

		buttonOK = (Button) findViewById(R.id.aro_detail_dialog_button_ok);
		buttonOK.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		finish();
	}

}
