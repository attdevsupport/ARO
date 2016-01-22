/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.mobiledevice.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.android.ddmlib.IDevice;
import com.att.aro.core.ILogger;
import com.att.aro.core.android.pojo.ShellOutputReceiver;
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.mobiledevice.pojo.RootCheckOutputReceiver;
import com.att.aro.core.model.InjectLogger;

public class AndroidDeviceImpl implements IAndroidDevice {

	@InjectLogger
	private ILogger logger;
	private RootCheckOutputReceiver receiverSU;
	private IDevice lastDevice;

	@Autowired
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	protected void setLastDevice(IDevice lastDevice) {
		this.lastDevice = lastDevice;
	}

	public RootCheckOutputReceiver makeRootCheckOutputReceiver() {
		return new RootCheckOutputReceiver();
	}

	/**
	 * Check Android device for SELinux enforcement
	 * 
	 * @param device - a real device or emulator
	 * @return true if SELinux-Enforced, false if permissive
	 * @throws Exception
	 */
	@Override
	public boolean isSeLinuxEnforced(IDevice device) throws Exception {

		if (device == null) {
			throw new Exception("device is null");
		}

		ShellOutputReceiver shSELinux = new ShellOutputReceiver();
		device.executeShellCommand("getenforce", shSELinux);

		boolean seLinuxEnforced = shSELinux.isSELinuxEnforce();
		logger.info("--->seLinuxEnforced:" + seLinuxEnforced);

		return seLinuxEnforced;
	}
	
	/**
	 * Check if a connected Android device is rooted or not.
	 * <p>performs 'su -c id' on Android<br>
	 * a response containing "uid=0(root) gid=0(root)" is considered rooted</p>
	 * 
	 * @throws IOException
	 */
	@Override
	public boolean isAndroidRooted(IDevice device) throws Exception {

		if (device == null) {
			throw new Exception("device is null");
		}

		if (!device.equals(lastDevice)) {

			setLastDevice(device);

			receiverSU = makeRootCheckOutputReceiver();
			logger.info("executing su command on device");
			try {
				// timeout is defined here DdmPreferences.getTimeOut();
				device.executeShellCommand("su -c id", receiverSU);
			} catch (Exception e) {
				logger.info("executing su command on device failed :"+e.getMessage());
				throw new Exception("device.executeShellCommand FAILED:", e);
			}
		}
		return receiverSU.isRootId();
	}

}
