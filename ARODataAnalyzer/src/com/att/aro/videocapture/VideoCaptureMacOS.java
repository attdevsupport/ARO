package com.att.aro.videocapture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.interfaces.ImageSubscriber;
import com.att.aro.libimobiledevice.Screencapture;
import com.att.aro.libimobiledevice.ScreencaptureImpl;
import com.att.aro.util.ImageHelper;

public class VideoCaptureMacOS extends Thread{
	private static final Logger logger = Logger.getLogger(VideoCaptureMacOS.class.getName());
	private List<ImageSubscriber> subscribers;
	
	private volatile boolean stop = false;
	
	private String workingFolder = "";
	
	private QuickTimeOutputStream qos;
	
	private Date videoStartTime;
	
	Screencapture capt = null;
	int videoWidth = 0;
	int videoHeight = 0;
	public VideoCaptureMacOS(File file) throws IOException{
		//capt = new ScreencaptureImpl();
		subscribers = new ArrayList<ImageSubscriber>();
		qos = new QuickTimeOutputStream(file,
				QuickTimeOutputStream.VideoFormat.JPG);
		qos.setVideoCompressionQuality(1f);
		qos.setTimeScale(10);
	}
	//for use in unit test
	public VideoCaptureMacOS(QuickTimeOutputStream qt, Screencapture screencapture){
		subscribers = new ArrayList<ImageSubscriber>();
		this.qos = qt;
		this.capt = screencapture;
	}
	public void setWorkingFolder(String folder){
		this.workingFolder = folder;
		logger.info("set working folder: "+this.workingFolder);
	}
	/**
	 * Asynchronous operation will execute doWork() in the background
	 */
	public void run(){
		doWork();
	}
	/**
	 * Synchronous operation that will do the heavy work of capturing screenshot and compose video.
	 * This method should never be called directly, use run() instead. (created for junit test)
	 */
	public void doWork(){
		stop = false;
		logger.info("Start Screencapture...");
		if(capt == null){
			capt = new ScreencaptureImpl();
		}
		String res = "";
		try{
			res = capt.initService();
		}catch(UnsatisfiedLinkError er){
			MessageDialogFactory.showErrorDialog(null, "Failed to load external library libimobiledevie for capturing video. "+er.getMessage());
			return;
		}
		catch(Exception ex){
			MessageDialogFactory.showErrorDialog(null, "Error: "+ex.getMessage());
			return;
		}
		if(res != null){
			res = res.trim();
		}
		if(res.contains("SUCCESS")){
			logger.info("Started screencapture service.");
			
		}else{
			logger.info("Failed to start screencapture service. Result: "+res);
			//return?
		}
		Date lastFrameTime = this.videoStartTime = new Date();
		while(!stop){//loop till "stop" is set to true
			byte[] data = capt.getScreenImage();
			if(data == null){
				logger.info("FAiled to get screenshot. Exiting...");
				break;
			}else{
				//logger.info("Received an image from screencapture service.");
			}
			try {
				BufferedImage image = ImageHelper.getImageFromByte(data);
				if(image != null){
					if(videoWidth == 0 && image.getWidth() > 0){
						if(image.getWidth() > image.getHeight()){
							//starting up with landscape?
							videoWidth = image.getHeight();
							videoHeight = image.getWidth();
						}else{
							videoWidth = image.getWidth();
							videoHeight = image.getHeight();
						}
					}
					if(image.getWidth() > image.getHeight()){
						//video screen is rotated to landscape orientation?
						image = ImageHelper.rorateImage(image, 90);
						if(image.getWidth() != videoWidth || image.getHeight() != videoHeight){
							image = ImageHelper.resize(image, videoWidth, videoHeight);
						}
					}
					Date timestamp = new Date();
					int duration = Math.round((float) (timestamp.getTime() - lastFrameTime
									.getTime()) * qos.getTimeScale() / 1000f);
					if(duration > 0){
						qos.writeFrame(image, duration);
					}
					lastFrameTime = timestamp;
					//logger.info("Passing image to subscriber");
					callSubscriber(image);
				}
				//limit the speed to 10 frame per seconds to avoid overhead on mobile device
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.severe(e.getMessage());
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe(e.getMessage());
			}
			
		}
		
		stop = false;//signal waiter to stop waiting
		logger.info("stopped screencapture");
		
	}
	
	public void stopCapture(){
		if(!stop){//in case video is already stopped
			stop = true;
			logger.info("sent signal to stop long running task and now wait");
			int waitcount = 0;
			while(stop){//run() should reset it to false before it quit
				try {
					logger.info("Waiting for videocapture to stop, counter: "+waitcount);
					Thread.sleep(500);
					waitcount++;
					if(waitcount > 5){
						logger.info("Timeout on wait, force exit on counter: "+waitcount);
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.severe(e.getMessage());
					break;
				}
			}
		}
		if(capt !=  null){
			try{
				capt.stopCapture();
			}catch(UnsatisfiedLinkError er){
			}
			capt = null;
		}
		//properly close video creator
		try {
			qos.close();
			logger.info("properly closed video writer.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("finished video capture");
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
	public void addSubscriber(ImageSubscriber sub){
		this.subscribers.add(sub);
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
	 * Gets the start time of the video capture.
	 * 
	 * @return The start time of the video.
	 */
	public Date getVideoStartTime() {
		return videoStartTime;
	}
}//end class
