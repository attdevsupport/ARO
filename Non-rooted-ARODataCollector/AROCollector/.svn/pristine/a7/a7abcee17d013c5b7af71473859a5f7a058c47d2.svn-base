package com.att.arocollector;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.att.arocollector.client.CaptureVpnService;
import com.att.arodatacollector.AROCollectorService;
import com.att.arodatacollector.AROGpsMonitorService;

public class AROCollectorActivity extends Activity {

	private static String TAG = "AroCollectorActivity";
	private Context context;
	private boolean vpnStatus;
	private Intent captureVpnServiceIntent;
	private ComponentName componentName;
	private BroadcastReceiver analyzerCloseCmdReceiver = null;
	private Intent aROCollectorService;
	private ComponentName collectorService;
	private Intent aROGpsMonitorService;
	private ComponentName gpsMonitorService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(...)");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		context = getApplicationContext();
		if(networkAndAirplaneModeCheck()){
			
			// register to listen for close down message
			registerAnalyzerCloseCmdReceiver();
			
			startVPN();
		}
		
		{ // test code
			PackageInfo packageInfo = null;
			try {
				packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boolean valu = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			String display = "version: " + packageInfo.versionName + " ("+(valu ? "Debug" : "Production")+")";
			((TextView) findViewById(R.id.version)).setText(display);
		}
	}

	/**
	 * initiate trace services
	 */
	private void startServices() {
		launchAROCollectorService();
		launchAROGpsMonitorService();
	//	launchAROCameraMonitorService();
	}
	
	private void stopServices(){
		stopAROCollectorService();
		stopAROGpsMonitorService();
	//	stopAROCameraMonitorService();
	}

	/**
	 * Launch intent for user approval of VPN connection
	 * 
	 */
	private void startVPN() {
		Log.i(TAG, "startVPN()");

		// check for VPN already running
		try {
			if (!checkForActiveInterface(getApplicationContext(), "tun0")) {

				// get user permission for VPN
				Intent intent = VpnService.prepare(this);
				if (intent != null) {
					Log.d(TAG, "ask user for VPN permission");
					startActivityForResult(intent, 0);
				} else {
					Log.d(TAG, "already have VPN permission");
					onActivityResult(0, RESULT_OK, null);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception checking network interfaces :" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * check a network interface by name
	 * 
	 * @param context
	 * @param networkInterfaceName
	 * @return true if interface exists and is active
	 * @throws Exception
	 */
	private boolean checkForActiveInterface(Context context, String networkInterfaceName) throws Exception {

		List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
		for (NetworkInterface intf : interfaces) {
			if (intf.getName().equals(networkInterfaceName)) {
				return intf.isUp();
			}
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult(... resultCode{" + resultCode + "} ...)");
		if (resultCode == RESULT_OK) {
			String prefix = getPackageName();
			captureVpnServiceIntent = new Intent(getApplicationContext(), CaptureVpnService.class);
			captureVpnServiceIntent.putExtra("TRACE_DIR", "/sdcard/ARO/");
			componentName = startService(captureVpnServiceIntent);
			vpnStatus = true;
//			timeoutToBackground();
			
			// start collecting META data
			startServices();
	
		} else if (resultCode == RESULT_CANCELED) {
			showVPNRefusedDialog();
		}
	}
	
	/**
	 * Show dialog to educate the user about VPN trust
	 * abort app if user chooses to quit
	 * otherwise relaunch the startVPN()
	 */
	private void showVPNRefusedDialog() {
		
		new AlertDialog.Builder(this)
		.setTitle("Usage Alert")
		.setMessage("You must trust the AROCollector\nIn order to run a VPN based trace")
		.setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startVPN();
				}
			})
		.setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
		.show();
		
	}

	/**
	 * 
	 * @param title
	 * @param message
	 */
	private void showInfoDialog(String title, String message) {
		
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
	/*	.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startVPN();
				}
			}) */
		
		.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//finish();
				}
			})
		.show();
	
	}


	
	private void timeoutToBackground() {
		
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
					finish();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * launch AROCollectorService for the collection of META data
	 */
	private void launchAROCollectorService() {
		aROCollectorService = new Intent(getApplicationContext(), AROCollectorService.class);
		aROCollectorService.putExtra("TRACE_DIR", "/sdcard/ARO/");
		collectorService = startService(aROCollectorService);
	}

	/**
	 * launch AROGpsMonitorService for the collection of META data
	 */
	private void launchAROGpsMonitorService() {
		aROGpsMonitorService = new Intent(getApplicationContext(), AROGpsMonitorService.class);
		aROGpsMonitorService.putExtra("TRACE_DIR", "/sdcard/ARO/");
		gpsMonitorService = startService(aROGpsMonitorService);
	}

	/**
	 * stop AROCollectorService for the collection of META data
	 */
	private void stopAROCollectorService() {
		stopService(new Intent(AROCollectorService.ARO_COLLECTOR_SERVICE));
	}

	/**
	 * stop AROGpsMonitorService for the collection of META data
	 */
	private void stopAROGpsMonitorService() {

		stopService(new Intent(AROGpsMonitorService.ARO_GPS_MONITOR_SERVICE));
	}
	

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if (analyzerCloseCmdReceiver != null) {
			Log.d(TAG, "calling unregisterAnalyzerCloseCmdReceiver inside onDestroy()");
			unregisterAnalyzerCloseCmdReceiver();
		}

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
//		stopAROCollectorService();
//		stopAROGpsMonitorService();
		if (analyzerCloseCmdReceiver != null) {
			Log.d(TAG, "calling unregisterAnalyzerCloseCmdReceiver inside onPause()");
			unregisterAnalyzerCloseCmdReceiver();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	public static final String ANALYZER_CLOSE_CMD_INTENT = "arodatacollector.home.activity.close";

	/**
	 * Received broadcast adb shell am broadcast -a arodatacollector.home.activity.close
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			Log.d(TAG, "received analyzer close cmd intent at " + System.currentTimeMillis());
			boolean rez = stopService(captureVpnServiceIntent);
			Log.d(TAG, "stopService result=" + rez);
			unregisterReceiver(broadcastReceiver);
			finish();
		}
	};

	/**
	 * register broadcastReceiver for "arodatacollector.home.activity.close"
	 */
	private void registerAnalyzerCloseCmdReceiver() {
		if (analyzerCloseCmdReceiver  == null) {
			Log.i(TAG, "registering Receiver");
			analyzerCloseCmdReceiver = broadcastReceiver;
			registerReceiver(analyzerCloseCmdReceiver, new IntentFilter(AROCollectorActivity.ANALYZER_CLOSE_CMD_INTENT));
		}
	}

	/**
	 * do not need broadcastReceiver anymore so unregister it!
	 */
	private void unregisterAnalyzerCloseCmdReceiver() {
		Log.d(TAG, "inside unregisterAnalyzerCloseCmdReceiver");
		try {
			if (analyzerCloseCmdReceiver != null) {
				unregisterReceiver(analyzerCloseCmdReceiver);
				analyzerCloseCmdReceiver = null;

				Log.d(TAG, "successfully unregistered analyzerCloseCmdReceiver");
			}
		} catch (Exception e) {
			Log.d(TAG, "Ignoring exception in analyzerCloseCmdReceiver", e);
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		registerAnalyzerCloseCmdReceiver();

		super.onResume();
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed");
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	* Gets the state of Airplane Mode.
	* 
	* @param context
	* @return true if enabled.
	*/
	@SuppressLint("NewApi")
	@SuppressWarnings({ "deprecation", "unused" })
	private boolean isAirplaneModeOn() {
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Log.i(TAG, "AIR plane mode message "+Settings.System.getInt(context.getContentResolver(), 
	                Settings.System.AIRPLANE_MODE_ON, 0));
	        return Settings.System.getInt(context.getContentResolver(), 
	                Settings.System.AIRPLANE_MODE_ON, 0) != 0;  
		} else {    
			Log.i(TAG, "AIR plane mode message "+Settings.Global.getInt(context.getContentResolver(), 
			           Settings.Global.AIRPLANE_MODE_ON, 0));
			return Settings.Global.getInt(context.getContentResolver(), 
		           Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	    }
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean isConnectedToInternet(){
	    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	      if (connectivity != null)
	      {
	          NetworkInfo[] info = connectivity.getAllNetworkInfo();
	          if (info != null)
	        	  
	              for (int j = 0; j < info.length; j++){
	            	  Log.i(TAG, "NETWORK CONNECTION : "+ info[j].getState() +" Connected STATE :" + NetworkInfo.State.CONNECTED);
	                  if (info[j].getState().equals(NetworkInfo.State.CONNECTED))
	                  {
	                      return true;
	                  }
	              }

	      }
	      return false;
	} 
	
	private boolean networkAndAirplaneModeCheck(){
		String title = "ARO";
		String message =  "";
		boolean networkChecker = true;
		/*if(isAirplaneModeOn()){
			
			message = "Your phone is in Airplane Mode and ARO doesn't support Airplane mode";
			//popup dialog
			showInfoDialog(title, message);
			networkChecker = false;
		} else */
		if(!isConnectedToInternet()){
			message = "No network connection in your phone, Connect to network and start again";
			//popup dialog
			showInfoDialog(title, message);
			networkChecker = false;
		}
			
		return networkChecker;
		
	}

}
