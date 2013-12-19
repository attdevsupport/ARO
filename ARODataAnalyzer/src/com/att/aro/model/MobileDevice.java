/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Logger;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.att.aro.util.Util;

public class MobileDevice {
	private static final Logger logger = Logger.getLogger(MobileDevice.class.getName());
	IDevice devices[] = null;
	UDIDReader reader = null;
	AdbService adbservice = null;
	AndroidDebugBridge adb = null;
	
	public MobileDevice(){
		reader = new UDIDReader();
		adbservice = new AdbService();
	}
	public MobileDevice(IDevice devices[], UDIDReader reader, AdbService adbservice){
		this.devices = devices;
		this.reader = reader;
		this.adbservice = adbservice;
	}
	
	/**
	 * type of Mobile device corrently connected to the machine. IOS or Android or NoDeviceConnected
	 * @return MobileDeviceType enum
	 * @throws IOException 
	 */
	public MobileDeviceType getDeviceType() throws IOException{
		MobileDeviceType type = MobileDeviceType.NO_DEVICE_CONNECTED;
		if(Util.IsMacOS()){
			String serial = reader.getSerialNumber();
			if(serial != null && serial.trim().length() > 1){
				type = MobileDeviceType.IOS;
			}
		}
		if(type == MobileDeviceType.NO_DEVICE_CONNECTED){
			//might be an Android
			adbservice.ensureADBServiceStarted();
			int count = getTotalAndroidDevice();
			if(count > 0){
				type = MobileDeviceType.ANDROID;
			}
		}
		return type;
	}
	/**
	 * check if a connected Android device is rooted or not
	 * @throws IOException 
	 */
	public boolean isRootedAndroid() throws IOException{
		if(devices != null && devices.length > 0){
			IDevice device = devices[0];
			ShellOutputReceiver receiver = new ShellOutputReceiver();
			logger.info("executing su command on device");
			device.executeShellCommand("su", receiver);
			return receiver.isRoot();
		}
		return false;
	}
	private int getTotalAndroidDevice(){
		if(devices == null){
			adb = AndroidDebugBridge.createBridge();
			
			int count = 0;
			while (adb.hasInitialDeviceList() == false) {
				try {
					Thread.sleep(100);
					count++;
				} catch (InterruptedException e) {
				}
				// let's not wait > 3 sec.
				if (count > 30) {
					return 0;
				}
			}
			
			// Find a connected emulator
			devices = adb.getDevices();
		}
		return devices.length;
	}
	/**
	 * get the first Android deviced found in the list of devices connected to the machines.
	 * @return IDevice
	 */
	public IDevice getFirstAndroidDevice(){
		int total = getTotalAndroidDevice();
		if(total < 1){
			return null;
		}
		return devices[0];
	}
	
	class ShellOutputReceiver implements IShellOutputReceiver{
		String data = "";
		boolean iscancell = false;
		@Override
		public void addOutput(byte[] buff, int offset, int length) {
			Charset charset = Charset.forName("ISO-8859-1");
			data = new String(buff, offset, length, charset);
			logger.info("Got data: "+data);
			logger.info("offset: "+offset+", position: "+length);
			iscancell = true;
		}
		
		//Result return from executing adb shell su varies depending on device type
		
		//non-rooted
		//su: not found
		///system/bin/sh: su: not found
		
		//rooted device
		//root@android:/ #
		
		//emulator
		//root@generic:/ #
		public boolean isRoot(){
			if(data.contains("root@") && data.contains("#")){
				return true;
			}
			return false;
		}

		@Override
		public void flush() {
			logger.info("flush() is called and now set iscancell to true");
			iscancell = true;
		}

		@Override
		public boolean isCancelled() {
			return iscancell;
		}
		
	}
}//end class
