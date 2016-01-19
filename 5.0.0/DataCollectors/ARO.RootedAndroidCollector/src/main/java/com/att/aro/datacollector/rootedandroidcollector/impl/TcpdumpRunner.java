package com.att.aro.datacollector.rootedandroidcollector.impl;

import com.android.ddmlib.IDevice;
import com.att.aro.core.android.IAndroid;

/**
 * run Tcpdump in background
 * 
 * @author Borey Sao
 * @Date March 6, 2015
 */
public class TcpdumpRunner implements Runnable {
	private IDevice device;
	private String traceName;
	IAndroid android;
	private boolean seLinuxEnforced = false;

	/**
	 * 
	 * @param device
	 * @param traceName
	 * @param android
	 * @param seLinuxEnforced
	 *            true if device is SELinux enforced
	 */
	public TcpdumpRunner(IDevice device, String traceName, IAndroid android, boolean seLinuxEnforced) {
		this.device = device;
		this.traceName = traceName;
		this.android = android;
		this.seLinuxEnforced = seLinuxEnforced;
	}

	@Override
	public void run() {
		android.startTcpDump(device, seLinuxEnforced, traceName);
	}

}
