/**
 * 
 */
package com.att.aro.core.video;

import java.io.IOException;
import java.util.Date;

import com.android.ddmlib.IDevice;
import com.att.aro.core.datacollector.IVideoImageSubscriber;

/**
 * @author Harikrishna 
 *
 */
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
