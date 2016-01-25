/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.att.arocollector.client;


import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.att.arocollector.R;

public class CaptureVpnClient extends Activity implements OnClickListener {
	
	private static final String TAG = "CaptureVpnClient";
	
    private TextView mServerAddress;
    private TextView mServerPort;
    private TextView mSharedSecret;
	private Button connectButton;
	private Button stopButton;
	private boolean vpnStatus = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        mServerAddress = (TextView) findViewById(R.id.address);
        mServerPort = (TextView) findViewById(R.id.port);
        mSharedSecret = (TextView) findViewById(R.id.secret);

        connectButton = (Button)findViewById(R.id.connect);
        stopButton = (Button)findViewById(R.id.stop_collection);

        connectButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.connect:
			// get user permission for VPN
			Intent intent = VpnService.prepare(this);
			if (intent != null) {
				startActivityForResult(intent, 0);
			} else {
				onActivityResult(0, RESULT_OK, null);
			}

			break;

		case R.id.stop_collection:
			if (vpnStatus) {

				Log.d(TAG, "attempt to stopService CaptureVpnService.class");
				stopService(new Intent(this, CaptureVpnService.class));
				
				
				vpnStatus = false;
			}
			break;

		default:
			break;
		}
	}

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            String prefix = getPackageName();
            Intent intent = new Intent(this, CaptureVpnService.class)
		            .putExtra(prefix + ".COMMAND", "dummy command")
		            .putExtra(prefix + ".ADDRESS", mServerAddress.getText().toString())
                    .putExtra(prefix + ".PORT", mServerPort.getText().toString())
                    .putExtra(prefix + ".SECRET", mSharedSecret.getText().toString());
            startService(intent);
            vpnStatus = true;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
}
