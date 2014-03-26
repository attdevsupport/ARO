/*
 Copyright [2012] [AT&T]
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.att.aro.videocapture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.att.aro.interfaces.ImageSubscriber;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.TraceData;

/**
 * Represents a process thread for capturing raw images from the Android
 * Emulator. The class contains methods for initiating and ending the process of
 * capturing raw images to create a video.
 * 
 */
public class VideoCaptureThread extends Thread {

	private static final Logger logger = Logger
			.getLogger(VideoCaptureThread.class.getName());
	private static final int MAX_FETCH_EXCEPTIONS = 5;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private QuickTimeOutputStream qos;
	private IDevice device;
	private boolean allDone;
	private Date videoStartTime;
	private TraceData traceData;
	public boolean usbDisconnected = false;
	
	private List<ImageSubscriber> subscribers;

	public boolean isUsbDisconnected() {
		return usbDisconnected;
	}

	public void setUsbDisconnected(boolean usbDisconnected) {
		this.usbDisconnected = usbDisconnected;
	}

	/**
	 * Initializes a new instance of the VideoCaptureThread class using the
	 * specified Android device interface, and the specified output file.
	 * 
	 * @param device
	 *            An Android device interface.
	 * @param file
	 *            The video output file.
	 */
	public VideoCaptureThread(IDevice device,TraceData traceData, File file) throws IOException {
		allDone = false;
		this.device = device;
		this.traceData = traceData;
		qos = new QuickTimeOutputStream(file,
				QuickTimeOutputStream.VideoFormat.JPG);
		qos.setVideoCompressionQuality(1f);
		qos.setTimeScale(10);
		
		subscribers = new ArrayList<ImageSubscriber>();
	}
	/**
	 * add client who wants to get video frame
	 * @param sub
	 */
	public void addSubscriber(ImageSubscriber sub){
		subscribers.add(sub);
	}

	/**
	 * Process to capture the image of emulator and pass the raw image to create
	 * a video.
	 */
	public void run() {
		RawImage rawImage;
		BufferedImage image = null;
		int iExceptionCount = 0;
		IOException savedException = null;
		allDone = false;
		Date lastFrameTime = this.videoStartTime = new Date();
		setUsbDisconnected(false);
		
		while (!allDone) {
			try {
				// Screen shot is captured from the emulator.
				synchronized (device) {
					rawImage = device.getScreenshot();
				}
				if (rawImage != null) {
					Date timestamp = new Date();
					int duration = Math
							.round((float) (timestamp.getTime() - lastFrameTime
									.getTime()) * qos.getTimeScale() / 1000f);
					if (duration > 0) {
						if (image == null) {
							image = new BufferedImage(
									rawImage.width,
									rawImage.height,
									BufferedImage.TYPE_INT_RGB);
						}
						convertImage(rawImage, image);
						qos.writeFrame(image, duration);
						lastFrameTime = timestamp;
						callSubscriber(image);
					}
				}
				try{
					if (traceData != null) {
						final String deviceMake= traceData.getDeviceMake().toLowerCase();
						if(deviceMake.contains(rb.getString("Message.devicemakeHTC"))){
							//We would sleep for 1 sec for lower frame rate video on HTC devices via USB bridge 
							Thread.sleep(1000);
						}else{
							//Delay for 300 for > 2fps video
							Thread.sleep(300);
						}
					}
					
				}catch(InterruptedException e){
					e.printStackTrace();
					logger.info("Error: "+e.getMessage());
				}
			} catch (IOException e) {
				iExceptionCount++;
				if (e.getMessage().contains("device not found"))
				{
					e.printStackTrace();
					setUsbDisconnected(true);
					
				}
				if (iExceptionCount > MAX_FETCH_EXCEPTIONS) {
					allDone = true;
				}
				savedException = e;
			}
		}
		try {
			qos.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Exception closing video output stream",
					e);
			//e.printStackTrace();			
		}
		if (iExceptionCount > 0) {
			logger.warning((new StringBuilder())
					.append("One or Mores Exceptions fetching image: ")
					.append(savedException.toString()).toString());		
			
		}
	}
	/**
	 * passing image to subscribers
	 * @param image
	 */
	private void callSubscriber(final BufferedImage image){
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				for(ImageSubscriber sub: subscribers){
					sub.receiveImage(image);
				}
			}});
	}

	/**
	 * Finalizes the VideoCaptureThread object. This method overrides the java.lang.Object.Finalize method.
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		qos.close();
	}

	/**
	 * Stops the process of capturing images.
	 */
	public void stopRecording() {
		this.allDone = true;
	}

	/**
	 * Gets the start time of the video capture.
	 * 
	 * @return The start time of the video.
	 */
	public Date getVideoStartTime() {
		return videoStartTime;
	}

	/**
	 * Converts raw image in to buffered image object which will be provided to
	 * quickstream for creating video {@link QuickTimeOutputStream}
	 * 
	 * @param rawImage
	 *            {@link RawImage} object which is captured from the emulator
	 *            device.
	 * @param image
	 *            {@link BufferedImage} object which is used in quickstream
	 *            output
	 */
	private void convertImage(RawImage rawImage, BufferedImage image) {
		int index = 0;
		int indexInc = rawImage.bpp >> 3;
		// RawImage is redrawn in to BufferedImage.
		for (int y = 0; y < rawImage.height; y++) {
			for (int x = 0; x < rawImage.width; x++, index += indexInc) {
				int value = rawImage.getARGB(index);
				image.setRGB(x, y, value);
			}
		}
	}

}
