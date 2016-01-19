/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.video.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.ILogger;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.util.ImageHelper;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.pojo.QuickTimeOutputStream.VideoFormat;

/**
 * @author Harikrishna Yaramachu
 * Modified by Borey Sao on March 10, 2015
 * - added init() and correctly stop and save video
 */
public class VideoCaptureImpl implements IVideoCapture {

	@InjectLogger
	private static ILogger logger;
	
	private List<IVideoImageSubscriber> vImageSubscribers = new ArrayList<IVideoImageSubscriber>();
	
	private static final int MAX_FETCH_EXCEPTIONS = 5;
	private IVideoWriter videowriter;
	private IDevice device;

	private boolean allDone = false;

	private Date videoStartTime;
	private IThreadExecutor threadpool;
	private String deviceManufacturer = "";	
	private volatile boolean hasExited = false;
	private int iExceptionCount=0;

	private boolean videoCaptureActive;
	
	@Override
	public boolean isVideoCaptureActive() {
		return videoCaptureActive;
	}

	public boolean isAllDone() {
		return allDone;
	}

	public int getiExceptionCount() {
		return iExceptionCount;
	}

	public void setDeviceManufacturer(String deviceManufacturer) {
		this.deviceManufacturer = deviceManufacturer;
	}

	@Autowired
	public void setVideoWriter(IVideoWriter writer){
		this.videowriter = writer;
	}
	
	@Autowired
	public void setThreadExecutor(IThreadExecutor threadpool){
		this.threadpool = threadpool;
	}
	
	public void setDevice(IDevice device){
		this.device = device;
	}
	
	@Override
	public void init(IDevice device, String videoOutputFile) throws IOException {
		this.device = device;
		this.videowriter.init(videoOutputFile, VideoFormat.JPG, 0.2f,10);
		this.videoCaptureActive = true;
	}
	/**
	 * add client who wants to get video frame
	 * @param vImageSubscriber
	 */
	public void addSubscriber(IVideoImageSubscriber vImageSubscriber){
		vImageSubscribers.add(vImageSubscriber);
	}
	
	
	/**
	 * Process to capture the image of emulator and pass the raw image to create
	 * a video.
	 * 
	 */
	public void run(){
		
		if(device != null){
			
			RawImage rawImage;
			BufferedImage image = null;
			iExceptionCount = 0;
			
			allDone = false;
			Date lastFrameTime = this.videoStartTime = new Date();
			hasExited = false;
			while (!allDone) {
				try {
					// Screen shot is captured from the emulator.
					synchronized (device) {
						rawImage = device.getScreenshot();
					}
					if (rawImage != null) {
						Date timestamp = new Date();
						int duration = Math.round((float) (timestamp.getTime() - lastFrameTime.getTime()) * videowriter.getTimeUnits() / 1000f);
						if (duration > 0) {
							if (image == null) {
								image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_RGB);
							}
							ImageHelper.convertImage(rawImage, image);
							
							videowriter.writeFrame(image, duration);
							lastFrameTime = timestamp;
							callSubscriber(image);
						}
					}
					try {
						if (deviceManufacturer != null) {
							if (deviceManufacturer.contains("htc")) { // Is it necessary now?
								//We would sleep for 1 sec for lower frame rate video on HTC devices via USB bridge 
								Thread.sleep(1000);
							} else {
								//Delay for 100 for > 2fps video
								Thread.sleep(100);
							}
						}
	
					} catch (InterruptedException interruptedExp) {
						logger.error(interruptedExp.getMessage());
		
					} 
					
				} catch (IOException ioExp) {
					iExceptionCount++;	
					logger.error("IOException ", ioExp);
				} catch (TimeoutException timeOutExp) {
					iExceptionCount++;
					logger.error("Time out exception ", timeOutExp);
				} catch (AdbCommandRejectedException adbExp) {
					logger.error("Device Offline!", adbExp);
					iExceptionCount++;
				}
				if (iExceptionCount > MAX_FETCH_EXCEPTIONS) {
					allDone = true;
					logger.error("Too many exceptions caught, now exiting video capture");
				}
			}
			
			try {
				videowriter.close();
			} catch (IOException ioExp) {
				logger.warn("Exception closing video output stream", ioExp);
			}

			hasExited = true;
		}
		
	}
	
	
	/**
	 * passing image to subscribers
	 * @param image
	 */
	private void callSubscriber(final BufferedImage image){
		if(!vImageSubscribers.isEmpty()){
		threadpool.execute(new Runnable(){

			@Override
			public void run() {
				for(IVideoImageSubscriber sub: vImageSubscribers){
					sub.receiveImage(image);
				}
			}});
		}
	}
	
	/**
	 * Finalizes the VideoCaptureThread object. This method overrides the java.lang.Object.Finalize method.
	 * @see java.lang.Object#finalize()
	 * 
	 */
	@Override
	protected void finalize() throws Throwable {
		videowriter.close();
		super.finalize();
	}
	
	/**
	 * Stops the process of capturing images.
	 */
	public void stopRecording() {
		this.allDone = true;
		int count = 0;
		while (!hasExited && count < 20) {
			count++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		if (!hasExited) {
			try {
				videowriter.close();
				videoCaptureActive = false;
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	/**
	 * Gets the start time of the video capture.
	 * @return
	 */
	public Date getVideoStartTime() {
		return videoStartTime;
	}

}
