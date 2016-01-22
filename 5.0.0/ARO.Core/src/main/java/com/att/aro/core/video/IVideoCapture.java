/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.video;

import java.io.IOException;
import java.util.Date;

import com.android.ddmlib.IDevice;
import com.att.aro.core.datacollector.IVideoImageSubscriber;

public interface IVideoCapture extends Runnable {
	
	/**
	 * Initializes a new instance of the VideoCapture class using the
	 * specified Android device interface, and the specified output file.
	 * 
	 * @param device
	 * 			An Android device interface.
	 * 
	 * @param videoOutputFile
	 * 			Video output file name.
	 * @throws IOException 
	 */
	void init(IDevice device, String videoOutputFile) throws IOException;
	
	void setDeviceManufacturer(String deviceManufacturer);
		
	/**
	 * add client who wants to get video frame
	 * @param vImageSubscriber
	 */
	void addSubscriber(IVideoImageSubscriber vImageSubscriber);

	/**
	 * Stops the process of capturing images.
	 */
	void stopRecording();
	
	/**
	 * Gets the start time of the video capture.
	 * @return
	 */
	Date getVideoStartTime();
	
	/**
	 * Returns the status of Video Capture
	 * @return true if capture is active, false if not active
	 */
	boolean isVideoCaptureActive();
}
