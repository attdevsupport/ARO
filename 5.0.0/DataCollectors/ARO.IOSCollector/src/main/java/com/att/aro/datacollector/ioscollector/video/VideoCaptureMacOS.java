/*
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

package com.att.aro.datacollector.ioscollector.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import com.android.ddmlib.IDevice;
import com.att.aro.core.ILogger;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.fileio.impl.FileManagerImpl;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.impl.VideoWriterImpl;
import com.att.aro.core.video.pojo.QuickTimeOutputStream;
import com.att.aro.core.video.pojo.QuickTimeOutputStream.VideoFormat;
import com.att.aro.datacollector.ioscollector.IScreenCapture;
import com.att.aro.datacollector.ioscollector.ImageSubscriber;

public class VideoCaptureMacOS extends Thread implements IVideoCapture {
	private ILogger log = new LoggerImpl("IOSCollector");// ContextAware.getAROConfigContext().getBean(ILogger.class);
	private List<ImageSubscriber> subscribers;
	private List<IVideoImageSubscriber> vImageSubscribers = new ArrayList<IVideoImageSubscriber>();
	
	private IVideoWriter videowriter = new VideoWriterImpl();

	private volatile boolean stop = false;
	private volatile boolean hasQuit = false;

	private String workingFolder = "";

	private QuickTimeOutputStream qos;

	private Date videoStartTime;

	IScreenCapture capt = null;
	ScreenshotManager smanage = null;

	int videoWidth = 0;
	int videoHeight = 0;

	public VideoCaptureMacOS(File file) throws IOException {
		subscribers = new ArrayList<ImageSubscriber>();
		((VideoWriterImpl) this.videowriter).setFileManager(new FileManagerImpl());
		this.videowriter.init(file.getAbsolutePath(), VideoFormat.JPG, 0.2f, 10);

		//		qos = new QuickTimeOutputStream(file,
		//				QuickTimeOutputStream.VideoFormat.JPG);
		//		qos.setVideoCompressionQuality(0.2f); // orig 1f
		//		qos.setTimeScale(10);

	}

	//for use in unit test
	public VideoCaptureMacOS(QuickTimeOutputStream qt, IScreenCapture screencapture) {
		subscribers = new ArrayList<ImageSubscriber>();
		this.qos = qt;
		this.capt = screencapture;
	}

	public void setWorkingFolder(String folder) {
		this.workingFolder = folder;
		log.info("set working folder: " + this.workingFolder);
	}

	/**
	 * Asynchronous operation will execute doWork() in the background
	 */
	public void run() {
		doWork();
	}

	/**
	 * Synchronous operation that will do the heavy work of capturing screenshot
	 * and compose video. This method should never be called directly, use run()
	 * instead. (created for junit test)
	 */
	public void doWork() {
		stop = false;
		hasQuit = false;
		log.info("Init Screencapture...");
		log.info("workingfolder :"+this.workingFolder);

		smanage = new ScreenshotManager(this.workingFolder);
		smanage.start();
		log.info("started ScreenshotManager.");
		int timeoutcounter = 0;
		while (!smanage.isReady()) {
			try {
				log.info("waiting for ScreenshotManager to be ready");
				Thread.sleep(200);
				timeoutcounter++;
			} catch (InterruptedException e) {
				log.debug("InterruptedException:", e);
			}
			if (timeoutcounter > 30) {//give it 6 seconds to start up
				log.info("Timeout on screenshotmanager");
				break;
			}
		}
		log.info("ScreenshotManager is ready: " + smanage.isReady());

		Date lastFrameTime = this.videoStartTime = new Date();
		int pausecounter = 0;
		log.debug("smanage.isReady() = "+smanage.isReady());
		while (!stop&&smanage.isReady()) {//loop till "stop" is set to true

			try {
				BufferedImage image = smanage.getImage();// ImageHelper.getImageFromByte(data);
				if (image != null) {

					//***********
					Date timestamp = new Date();
					int duration = Math.round((float) (timestamp.getTime() - lastFrameTime.getTime()) * videowriter.getTimeUnits() / 1000f);

			//		log.debug("image H:"+image.getHeight()+", duration :"+duration);
					
					if (duration > 0) {
						videowriter.writeFrame(image, duration);
						lastFrameTime = timestamp;
						callSubscriber(image);
					}
					//***********
					/*
					 * pausecounter = 0; if(videoWidth == 0 && image.getWidth()
					 * > 0){ if(image.getWidth() > image.getHeight()){
					 * //starting up with landscape? videoWidth =
					 * image.getHeight(); videoHeight = image.getWidth(); }else{
					 * videoWidth = image.getWidth(); videoHeight =
					 * image.getHeight(); } } if(image.getWidth() >
					 * image.getHeight()){ //video screen is rotated to
					 * landscape orientation? image =
					 * ImageHelper.rorateImage(image, 90); if(image.getWidth()
					 * != videoWidth || image.getHeight() != videoHeight){ image
					 * = ImageHelper.resize(image, videoWidth, videoHeight); } }
					 * Date timestamp = new Date(); int duration =
					 * Math.round((float) (timestamp.getTime() - lastFrameTime
					 * .getTime()) * qos.getTimeScale() / 1000f); if(duration >
					 * 0){ qos.writeFrame(image, duration); } lastFrameTime =
					 * timestamp; //log.info("Passing image to subscriber");
					 * callSubscriber(image);
					 */
				} else if (!stop) {
					log.info("Failed to get screenshot image, pause for 1/2 second");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						log.debug("InterruptedException:", e);
					}
					pausecounter++;
					if (pausecounter > 20) {
						break;
					}
				}
				if (!stop) {
					//limit the speed to 10 frame per seconds to avoid overhead on mobile device
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						log.debug("InterruptedException:", e);
					}
				}
			} catch (IOException e) {
				log.debug("IOException:", e);
				break;
			}

		}
		try {
			videowriter.close();
		} catch (IOException ioExp) {
			log.warn("Exception closing video output stream", ioExp);
		}
		hasQuit = true;
		stop = false;//signal waiter to stop waiting
		log.info("stopped screencapture");

	}

	public void signalStop() {
		stop = true;
		log.info("signal video capture to stop. no waiting for now");
	}

	public void stopCapture() {
		if (!hasQuit) {//in case video is already stopped
			stop = true;
			log.info("sent signal to stop long running task and now wait");
			int waitcount = 0;
			while (stop) {//run() should reset it to false before it quit
				try {
					log.info("Waiting for videocapture to stop, counter: " + waitcount);
					Thread.sleep(100);
					waitcount++;
					if (waitcount > 20) {
						log.info("Timeout on wait, force exit on counter: " + waitcount);
						break;
					}
				} catch (InterruptedException e) {
					log.debug("InterruptedException:", e);
					break;
				}
			}
		} else {
			log.info("capture engine already quit, proceed to next step");
		}
		if (capt != null) {
			try {
				capt.stopCapture();
			} catch (UnsatisfiedLinkError er) {
			}
			capt = null;
			log.info("disposed screencapture");
		}
		if (smanage != null) {
			try {
				smanage.signalShutdown();
			} catch (IOException e) {
				log.debug("IOException:", e);
			}
			smanage = null;
		}
		//properly close video creator
		try {
			videowriter.close();
		} catch (IOException ioExp) {
			log.warn("Exception closing video output stream", ioExp);
		}
		//		try {
		//			qos.close();
		//			log.info("properly closed video writer.");
		//		} catch (IOException e) {
		//			log.debug("IOException:", e);
		//		}
		log.info("finished video capture");
		System.gc();
	}

	/**
	 * passing image to subscribers
	 * 
	 * @param image
	 */
	private void callSubscriber(final BufferedImage image) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (ImageSubscriber sub : subscribers) {
					sub.receiveImage(image);
				}
				for (IVideoImageSubscriber newSub:vImageSubscribers){
					newSub.receiveImage(image);
				}
			}
		});
	}

	public void addSubscriber(ImageSubscriber sub) {
		this.subscribers.add(sub);
	}
	
	@Override
	public void addSubscriber(IVideoImageSubscriber vImageSubscriber) {
		vImageSubscribers.add(vImageSubscriber);
	}

	/**
	 * Finalizes the VideoCaptureThread object. This method overrides the
	 * java.lang.Object.Finalize method.
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			videowriter.close();
		} catch (IOException ioExp) {
			log.warn("Exception closing video output stream", ioExp);
		}
		//		qos.close();
	}

	/**
	 * Gets the start time of the video capture.
	 * 
	 * @return The start time of the video.
	 */
	public Date getVideoStartTime() {
		return videoStartTime;
	}

	@Override
	public void init(IDevice device, String videoOutputFile) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeviceManufacturer(String deviceManufacturer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRecording() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isVideoCaptureActive() {
		// TODO Auto-generated method stub
		return false;
	}
}//end class

