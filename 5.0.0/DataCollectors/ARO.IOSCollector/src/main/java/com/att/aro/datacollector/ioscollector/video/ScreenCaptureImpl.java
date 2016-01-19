/*
 * Copyright 2012 AT&T
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

package com.att.aro.datacollector.ioscollector.video;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.IScreenCapture;

public class ScreenCaptureImpl implements IScreenCapture {
	private ILogger log = new LoggerImpl("IOSCollector");
	static { 
		System.loadLibrary("ScreencaptureBridge");
	}
	private static volatile boolean isAlreadyInit = false;
	private static volatile boolean isRunning = false;
	/**
	 * create screen capture service
	 */
	native String startService();
	/**
	 * capture image and return it as byte array
	 * @return
	 */
	native byte[] captureScreen();
	/**
	 * stop the service and clean up as necessary
	 */
	native void stopService();
	
	
	/* (non-Javadoc)
	 * @see com.att.aro.libimobiledevice.Screencapture#initService()
	 */
	@Override
	public String initService(){
		if(!isAlreadyInit){
			log.info("initService()");
			isAlreadyInit = true;
			isRunning = true;
			return this.startService();
		}else{
			log.info("skip initService(). It is already done");
		}
		return "SUCCESS";
	}
	/* (non-Javadoc)
	 * @see com.att.aro.libimobiledevice.Screencapture#getScreenImage()
	 */
	@Override
	public byte[] getScreenImage(){
		return this.captureScreen();
	}
	/* (non-Javadoc)
	 * @see com.att.aro.libimobiledevice.Screencapture#stopCapture()
	 */
	@Override
	public void stopCapture(){
		if(isRunning){
			try{
				this.stopService();
				log.info("stopCapture()");
			}catch(Exception ex){
				log.error("Error stopping screencapture service: "+ex.getMessage());
			}
			isRunning = false;
			isAlreadyInit = false;
			
		}else{
			log.info("skip stopCapture(), it was already stopped");
		}
	}
}
