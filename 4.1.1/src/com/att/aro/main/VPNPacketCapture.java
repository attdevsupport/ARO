package com.att.aro.main;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.att.aro.util.ApkUtil;
import com.att.aro.util.ShellReceiver;
import com.att.aro.util.Util;

public class VPNPacketCapture extends Thread{
	
	private static final Logger logger = Logger.getLogger(VPNPacketCapture.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String COLLECTORVPNAPK = rb.getString("Name.collectorvpnapk");
	private Date pcapStartTime;
	private boolean cancelFlag;
	private boolean setupFlag;
	private IDevice androidDevice;
	private String tracePath = null;
	private boolean adbConnection;

	/**
	 * Start capturing the trace
	 * 
	 * Extract, Install and Launch VPNCollector APK, 
	 * 
	 * @param androidDevice
	 * @param tracePath
	 */
	public void startPacketCapture(IDevice androidDevice, String tracePath) {

		this.androidDevice = androidDevice;
		this.tracePath = tracePath;
		
		// extract AROCollector from jar into trace directory
		File collectorVpnApk = null;
		try {
			collectorVpnApk = ApkUtil.getAroCollectorFilesFromJar(androidDevice, COLLECTORVPNAPK, tracePath);
		} catch (IOException e) {
			logger.severe("Failed to extract APK from jar"+e.getMessage());
			e.printStackTrace();
		}
		// Installs application version collection apk in emulator.
		try {
			//TODO: find a better to detect old version and uninstall it.
			androidDevice.uninstallPackage(rb.getString("Emulator.vpnPackageName"));
			androidDevice.installPackage(collectorVpnApk.getPath(), true);
			adbConnection = true;
		} catch (IOException e) {
			logger.severe("Failed to install APK to device"+e.getMessage());
			adbConnection = false;
			//26:21 E/Device: Unable to open sync connection! reason: Unable to upload file: timeout
			e.printStackTrace();
		}
		
		// make sure collector is not running
		forceStopAPK();
		
		// Starts collector application on device.
		ShellReceiver shelloutPut = new ShellReceiver();
		String shellCmd = MessageFormat.format(rb.getString("Emulator.startDeviceVpnApk"), tracePath);
		try {
			androidDevice.executeShellCommand(shellCmd, shelloutPut);
			this.pcapStartTime = new Date();
			collectorVpnApk.delete();
		} catch (IOException e) {
			logger.severe("Failed to launch collector on device"+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * force stop the apk
	 * @return
	 */
	public boolean forceStopAPK() {

		logger.info("uninstall");
		ShellReceiver shelloutPut = new ShellReceiver();
		String shellCmd = rb.getString("Emulator.forceStopVpnApk");
		logger.info("run "+shellCmd);
		try {
			androidDevice.executeShellCommand(shellCmd, shelloutPut);
			logger.info("result :" + shelloutPut.getResponseString());
		} catch (IOException e) {
			logger.severe("Failed to uninstall apk, error:" + e.getMessage());
			if (e.getMessage().contains("device not found")) {
				adbConnection = false;
				//TODO dialog about no connection
			} else {
				e.printStackTrace();
			}
		}

		return (shelloutPut.getResponseString() != null);
	}

	/**
	 * check through ADB to see if device has tun0 turned on
	 * @return true if tun0 is active with correct address
	 */
	public boolean isVpnActivated() {

		logger.info("check for vpn");
		ShellReceiver shelloutPut = new ShellReceiver(rb.getString("Emulator.vpn_tunnel_match"));
		String shellCmd = rb.getString("Emulator.vpn_tunnel_check");
		try {
			androidDevice.executeShellCommand(shellCmd, shelloutPut);
			logger.info("result :" + shelloutPut.getResponseString());
		} catch (IOException e) {
			logger.severe("Failed to query device for tun0, error:" + e.getMessage());
			if (e.getMessage().contains("device not found")) {
				adbConnection = false;
				//TODO dialog about no connection
			} else {
				e.printStackTrace();
			}
		}

		return (shelloutPut.getResponseString() != null);
	}
	
	/**
	 * switch Android phone view to 
	 */
	public void gotoAndroidHomeScreen() {

		ShellReceiver shelloutPut = new ShellReceiver();
		String shellCmd;
		ShellReceiver tracePath = new ShellReceiver();
		shellCmd = MessageFormat.format("am start -a android.intent.action.MAIN -c android.intent.category.HOME", tracePath);
		try {
			androidDevice.executeShellCommand(shellCmd, shelloutPut);
		} catch (IOException e) {
			logger.severe("Failed to go home"+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				sleep(500);
			} catch (InterruptedException e) {
				logger.severe("InterruptedException, error:"+e.getMessage());
				e.printStackTrace();
			}

			if (isVpnActivated()) {
				gotoAndroidHomeScreen();
				break;
			}
		}
	}

    /**
     * Execute the command for stopping the packet capture
     */
	public void stopPacketCapture() {
		
		// Verifiy Collector apk is running
		ShellReceiver shellOutput = new ShellReceiver("com.att.arocollector");
		String shellCmd = "ps"; 
		try {
			androidDevice.executeShellCommand(shellCmd, shellOutput);
			String resp = shellOutput.getResponseString();
			// Samsung S4 9ea10795		{u0_a219   20091 208   885372 32376 ffffffff 00000000 S com.att.arocollector}
			// LG						{app_130   11964 183   496392 53008 ffffffff 00000000 S com.att.arocollector}
			// Samsung S4 1589d5f2		{u0_a196   4077  213   874280 46264 ffffffff 00000000 S com.att.arocollector}
			// Nexus 5 05ad2440f0dc050b	{u0_a109   12132 180   898832 39648 ffffffff 00000000 S com.att.arocollector}
			logger.info("result :"+resp);
		} catch (IOException e) {
			logger.severe("Failed to executeShellCommand, error:"+e.getMessage());
			e.printStackTrace();
		}

		try {
			// message CaptureVpnService to terminate VPN capture
			String command = rb.getString("Emulator.closeVpnService"); // am broadcast -a arovpndatacollector.service.close
			shellOutput.setCompareText("Broadcast completed:");
			androidDevice.executeShellCommand(command, shellOutput);
			logger.log(Level.INFO, "broadcast to close vpn service sent");

			// message Home Activity to finish()
			command = rb.getString("Emulator.closeHomeActivity"); // am broadcast -a arodatacollector.home.activity.close
			androidDevice.executeShellCommand(command, shellOutput);
			logger.log(Level.INFO, "broadcast to close home activity sent");
		} catch (IOException e) {
			logger.severe("Failed to executeShellCommand, error:"+e.getMessage());
			e.printStackTrace();
		}

	}


	/**
	 * Copy all files from Android trace folder
	 */
	public void pullTraceData() {
		if (adbConnection) {
			String command = "ls /sdcard/ARO/";
			ShellReceiver shellOutput = new ShellReceiver();
			try {
				androidDevice.executeShellCommand(command, shellOutput);
			} catch (IOException e) {
				logger.info("IOException:" + e.getMessage());
			}
			String[] responseList = shellOutput.getResponseStrings();
			if (responseList != null) {
				for (String lineItem : responseList) {
					logger.info(lineItem.toString());
				}
			}
		}else{
			logger.info("Cannot pull trace data, ADB connection was lost");
		}

	}

	/**
	 * Delete Android trace folder and all files within
	 */
	public void deleteTraceData() {
		
		String command = "rm -fr /sdcard/ARO/";
		ShellReceiver shellOutput = new ShellReceiver();
		try {
			androidDevice.executeShellCommand(command, shellOutput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void execute(String stopCmd) {
		// TODO Auto-generated method stub
		
	}

	public Date getPcapStartTime() {
        return this.pcapStartTime;
	}
	
    /**
     * When not connected to vpn
     * @return
     */
    public boolean isCancelFlag() {
        return cancelFlag;
    }

	/**
	 * 
	 * @return
	 */
	public String getResourceHome() {

		// String resourcePath = Util.getCurrentRunningDir() + Util.FILE_SEPARATOR + "bin\\";
		String resourcePath = Util.getCurrentRunningDir();

		File workspace = new File(resourcePath);
		// resourcePath = workspace.getParent() + Util.FILE_SEPARATOR + "AROLib\\bin" + Util.FILE_SEPARATOR;
		resourcePath = workspace.getParent() + Util.FILE_SEPARATOR + "bin" + Util.FILE_SEPARATOR;
		try {
			resourcePath = URLDecoder.decode(resourcePath, "UTF-8"); // To avoid the %20(space) problem in windows relative path
		} catch (UnsupportedEncodingException uex) {
			uex.printStackTrace();
		}
		// String currentDir = System.getProperty("user.dir");
		// String resourcePath = currentDir + Util.FILE_SEPARATOR + "AROResources\\bin\\";
		return resourcePath;

	}

	public IDevice getAndroidDevice() {
		return androidDevice;
	}

	public String getTracePath() {
		// TODO Auto-generated method stub
		return tracePath;
	}


}
