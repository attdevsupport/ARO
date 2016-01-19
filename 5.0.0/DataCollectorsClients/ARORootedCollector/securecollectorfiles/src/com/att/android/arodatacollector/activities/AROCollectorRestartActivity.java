package com.att.android.arodatacollector.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.ARODataCollector;


public class AROCollectorRestartActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.arocollector_restart_screen);
	}
}
