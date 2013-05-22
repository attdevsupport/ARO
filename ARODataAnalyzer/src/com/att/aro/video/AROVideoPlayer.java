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

package com.att.aro.video;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.CannotRealizeException;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.MediaTimeSetEvent;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.images.Images;
import com.att.aro.main.AROAdvancedTabb;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.TraceData;

/**
 * Displays the ARO Video Player UI, and provides Video player handling for the
 * Play and Pause functions. The AROVideoPlayer class provides methods for
 * refreshing the video frames when new traces are loaded in the ARO Data
 * Analyzer.
 */
public class AROVideoPlayer extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final float PLAYBACK_RATE = 1.0f; // 5.0f
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String operatingSystem = System.getProperty("os.name");
	private static Logger logger = Logger.getLogger(AROVideoPlayer.class
			.getName());
	private static final String outVideoTimeFileName = "exVideo_time";
	private BufferedWriter mTraceVideoTimeStampWriter;
	private OutputStream mTraceVideoTimeStampFile;
	private Component visualComponent;
	private Component controlComponent;
	private double videoOffset;
	private AROAdvancedTabb aroAdvancedTab;
	private JPanel aroVideoPanel;
	private JLabel jVideoLabel;
	private Player videoPlayer;
	private boolean isMediaConversionError = false;
	private JButton jButton;
	private boolean syncVideoClicked =false;
	private TraceData traceData;
	private  Time pauseVideoTime;
	private boolean videoStarted = false;
	private boolean showInfoMsg = true;
	private int controllerState;
	private boolean showPauseNotification;
	boolean isVideoPlayerStopped = false;
	public enum FileTypeEnum {
		QT, WMV, WMA, MPEG, _3GP, ASF, AVI, ASf, DV, MKV, MPG, RMVB, VOB, MOV, MP4;
		
	}
	private File existingExternalVideoFile = null;

	/**
	 * Runnable used to keep diagnostic chart in sync with video
	 */
	private Runnable syncThread = new Runnable() {

		private double seconds;
		private double userPausedPos;
		private double prevSeconds;
		private double timeAdjustment;

		@Override
		public void run() {

			// Run this in a loop if the video is started
			int state;
			do {

				// Get information from video player in this synchronized block
				// in case video is cleared while running.
				Time currentVideoTime;
				synchronized (AROVideoPlayer.this) {
					if (videoPlayer != null) {
						currentVideoTime = videoPlayer.getMediaTime();
						state = videoPlayer.getState();
					} else {
						break;
					}
				}

				// Check to see if video time has changed
				if (currentVideoTime != null
						&& currentVideoTime.getSeconds() != seconds) {
					if (aroAdvancedTab != null) {
						seconds = currentVideoTime.getSeconds();

						try {

							// Update diagnostics on AWT thread
							SwingUtilities.invokeAndWait(new Runnable() {

								@Override
								public void run() {
									// Sync external video and traces, in case of native video normal behavior is retained.
									if (syncVideoClicked || traceData.getExVideoStatus()){
										userPausedPos = videoOffset;
										if ( seconds >= userPausedPos ){
											timeAdjustment = (seconds-userPausedPos);
											aroAdvancedTab.setTimeLineLinkedComponents(timeAdjustment);
											if(syncVideoClicked){
												syncVideoClicked = false;
											}else if(traceData.getExVideoStatus()){
												traceData.setExVideoStatus(false);
											}
											prevSeconds = seconds;
										}
									}else{
										 //In case of native video , fall back on the native track.
										if(!syncVideoClicked && !(traceData.getExVideoStatus()) && traceData.isNativeVideo()){
											prevSeconds = 0.0;
										}
										if(prevSeconds > 0.0){
											aroAdvancedTab.setTimeLineLinkedComponents((seconds - prevSeconds)- timeAdjustment );
											}
										else{
											aroAdvancedTab.setTimeLineLinkedComponents(seconds+videoOffset);
										}
										
									}

								}

							});
						} catch (InterruptedException e) {
							logger.log(Level.SEVERE, "InterruptedException", e);
						} catch (InvocationTargetException e) {
							logger.log(Level.SEVERE,
									"InvocationTargetException", e);
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "InterruptedException", e);
				}
			} while (state == Controller.Started);
		}

	};

	/**
	 * Initializes a new instance of the AROVideoPlayer class, and displays the
	 * Video player panel using the methods in the specified AROAdvancedTabb
	 * object.
	 * 
	 * @param advancedTab
	 *            The Diagnostic tab screen where the Video player will be
	 *            displayed.
	 */
	public AROVideoPlayer(AROAdvancedTabb advancedTab) {
		super();
		this.aroAdvancedTab = advancedTab;
		setTitle(rb.getString("aro.videoTitle"));
		this.setIconImage(Images.ICON.getImage());
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		Dimension frameDim = new Dimension(350, 500);
		setMinimumSize(frameDim);
		setResizable(true);
		jVideoLabel = new JLabel(Images.NO_VIDEO_AVAILABLE.getIcon());
		jButton  = new JButton("Sync Video");
		jButton.setBackground(Color.WHITE);
		jButton.addActionListener(this);
		jButton.setPreferredSize(new Dimension(20,20));
		jButton.setVisible(false);
		aroVideoPanel = new JPanel();	
		aroVideoPanel.setLayout(new BorderLayout());
		aroVideoPanel.add(jButton,BorderLayout.NORTH);
		aroVideoPanel.add(jVideoLabel, BorderLayout.CENTER);
		setContentPane(aroVideoPanel);
	}
	
	/**
	 * Action performed on Sync video button click event.
	 * @param ActionEvent
	 */
	public void actionPerformed(ActionEvent e){
		   
		double externalVideoStartTime;
		File file;
		/*
		 * Check if exvideo_time file exist
		 * if yes, ask user if he wants to re-sync the video
		 * if yes, 
		 * 			rollback/reset the videoOffset,
		 * 			delete the existing exvideo_time file and recreate it with new sync time,
		 * 			stop the video player,
		 * 			reset the blue line and the video slider .
		 * */
		
		if(traceData.isPcapFile()){
			 file = new File(traceData.getTraceDir().getParentFile(), outVideoTimeFileName);
		}else{
			 file = new File(traceData.getTraceDir(), outVideoTimeFileName);
		}
		if (file.exists()){
			if(MessageDialogFactory.showConfirmDialog(
		    		AROVideoPlayer.this,
		    		"Video is already Synched.Do you want to Re-Sync again?", "Information",
		    		JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				// delete the exVideo_time file.
				try {
					file.delete();
				}catch(Exception ex){
					logger.log(Level.WARNING, "Exception in deleting exVideo_time file. " + ex);
				}
				showInfoMsg = true;
				double videoStartTime =traceData.getPcapTime0();
				this.videoOffset = videoStartTime > 0.0 ? videoStartTime
						- ((double) traceData.getTraceDateTime().getTime() / 1000)
						: 0.0;
				traceData.setExVideoTimeFileStatus(true);
				
				videoPlayer.stop();
				isVideoPlayerStopped = true;
				setMediaDisplayTime(0.0);
				aroAdvancedTab.setTimeLineLinkedComponents(0.0);
				pauseVideoTime = null;
				videoStarted=false;
				
			}
		}else{
			if (videoPlayer != null) {
				if (videoStarted){
					
					if(isVideoPlayerStopped){
						pauseVideoTime = null;
						isVideoPlayerStopped = false;
					}
					if (pauseVideoTime == null){
						MessageDialogFactory.showMessageDialog(
								AROVideoPlayer.this,
								"Pause the video before syncing.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
			    	}
			   }else{
			   	   //Ask the user to start the video and pause it.
			  	   if (pauseVideoTime == null){
			  		   MessageDialogFactory.showMessageDialog(
			  				   AROVideoPlayer.this,
			  				   "Start the video and Pause where the traces should start Syncing.", "Error",
			  				   JOptionPane.ERROR_MESSAGE);
			  		   return;
			  	   }
			    }
					
				//File does not exist,first time.
				syncVideoClicked = true;
				showPauseNotification=false;
				externalVideoStartTime = (pauseVideoTime.getSeconds() + ((double) traceData.getTraceDateTime().getTime() / 1000));
				aroAdvancedTab.setTimeLineLinkedComponents(0.0);
				this.videoOffset = pauseVideoTime.getSeconds();
				setMediaDisplayTime(0.0);
				
				//Write video_time file.
				try {
					writeVideoTraceTime(Double.toString(externalVideoStartTime));
				    closeVideoTraceTimeFile();
					} catch (IOException ex) {
						logger.log(Level.WARNING, "IOException in writing External video start time - " + ex);
					}
			}
		}
	}

	/**
	 * Refreshes the Video player display using the specified analysis data.
	 * 
	 * @param analysisData
	 *            The analysis data that is used to refresh the video.
	 * @throws IOException
	 */
	public synchronized void refresh(TraceData.Analysis analysisData)
			throws IOException {
		clear();
		String mediaUrl;
		if (analysisData == null
				|| (mediaUrl = getMediaUrl(analysisData.getTraceData()
						.getTraceDir())) == null) {
			setVideoNotAvailableImage(true);
			return;
		}
		traceData = analysisData.getTraceData();
		double videoStartTime = traceData.getVideoStartTime();
		MediaLocator mlr = new MediaLocator(mediaUrl);
		try {
			videoPlayer = Manager.createRealizedPlayer(mlr);
		} catch (CannotRealizeException e) {
			try {
				isMediaConversionError = true;
				mediaUrl = getMediaUrl(analysisData.getTraceData()
						.getTraceDir());
				MediaLocator mlr1 = new MediaLocator(mediaUrl);
				videoPlayer = Manager.createRealizedPlayer(mlr1);
			} catch (CannotRealizeException e1) {
				MessageDialogFactory.showUnexpectedExceptionDialog(this, e1);
				return;
			} catch (NoPlayerException e2) {
				MessageDialogFactory.showUnexpectedExceptionDialog(this, e2);
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (NoPlayerException e) {
			MessageDialogFactory.showUnexpectedExceptionDialog(this, e);
			return;
		}
		this.videoOffset = videoStartTime > 0.0 ? videoStartTime
				- ((double) traceData.getTraceDateTime().getTime() / 1000)
				: 0.0;
		videoPlayer.setRate(PLAYBACK_RATE);
		setVideoNotAvailableImage(false);

		// This is to turn off plug-in settings on vidoe info window, plugin
		// tab
		Control controls[] = videoPlayer.getControls();
		for (int i = 0; i < controls.length; i++) {
			String strControlName = controls[i].toString();
			if (strControlName.contains("BasicJMD")) {
				Component basicJMDComp = controls[i].getControlComponent();
				if (basicJMDComp.getParent() != null) {
					basicJMDComp.getParent().setVisible(false);
				}
				basicJMDComp.setVisible(false);
			}
		}

		controlComponent = videoPlayer.getControlPanelComponent();
		if (controlComponent != null) {
			controlComponent.setVisible(true);
			aroVideoPanel.add(controlComponent, BorderLayout.SOUTH);
		}

		visualComponent = videoPlayer.getVisualComponent();
		if (visualComponent != null) {
			aroVideoPanel.add(visualComponent, BorderLayout.CENTER);
			visualComponent.setVisible(true);
		}

		videoPlayer.addControllerListener(new ControllerListener() {
			@Override
			public synchronized void controllerUpdate(ControllerEvent evt) {
				if (evt instanceof StartEvent
						|| evt instanceof MediaTimeSetEvent) {

					if ((evt instanceof StartEvent) && showInfoMsg
							&& traceData.getExVideoTimeFileStatus()) {
						MessageDialogFactory
								.showMessageDialog(
										AROVideoPlayer.this,
										"The Analyzer loaded an external video. The video may not be in Sync with"
												+ " the traces. Pause the video at the trace starting point and " +
														"click Sync Video button.",
										"Information", 1);
						videoStarted = true;
						showInfoMsg = false;
						showPauseNotification=true;
						
					}
					new Thread(syncThread).start();
				}
				if (evt instanceof StopEvent) {
					if (videoPlayer!=null){
						controllerState = videoPlayer.getState();
						if(!showInfoMsg && (controllerState == 600) && showPauseNotification){
							MessageDialogFactory.showMessageDialog(
						    		AROVideoPlayer.this,
						    		"Pause the video to sync.", "Error",
						    		JOptionPane.ERROR_MESSAGE);
							isVideoPlayerStopped = false;
						    		return;
						}else{
							pauseVideoTime = videoPlayer.getMediaTime();
						}
					}
				}
			}
		});
		setMediaDisplayTime(0.0);

		setVisible(true);
		if(!traceData.isNativeVideo()){
			jButton.setVisible(true);
		}
		videoStarted = false;
		pauseVideoTime = null;	
	}
	/**
	 * Helper function which resets the sync button during trace reload.
	 * 
	*/
	public void  updateSyncButton() {
		jButton.setVisible(false);
	}
	/**
	 * Helper function which returns the status of video playing in the viewer.
	 * 
	*/	
	public boolean getVideoStatus() {
		return videoStarted; 
	}
	/**
	 * Helper function which returns the status of showInfoMsg.
	 * 
	*/	
	public boolean getShowInfoMsg() {
		return showInfoMsg;
	}
	/**
	 * Helper function which resets the showInfoMsg and sync button
	 * 	which is called during the reload of the trace file.
	 * 
	*/	
	public void setShowInfoMsg(boolean status){
		showInfoMsg = status;
		jButton.setVisible(false);
	}
	
	/**
	 * Helper function to return the instance of tracedata.
	 * 
	*/	
	public TraceData getTraceData() {
		return traceData;
	}
	/**
	 * Writes the video_time file to the trace directory.
	 * 
	 * @param externalVideoTime
	 *            The start of the external Video time.
	 * @throws IOException
	 */
	public void writeVideoTraceTime(String timestamp) throws IOException {
		initVideoTraceTime();
		writeTimetoFile(mTraceVideoTimeStampWriter, timestamp);
	}
	/**
	 * Initializes the FileOutputStream and BufferedWriter.
	 * 
	 * @param 
	 *        
	 * @throws FileNotFoundException.
	 */
	public void initVideoTraceTime() throws FileNotFoundException {

		// Check if we have loaded the Pcap file. Get its directory and write
		// the video_time file there.
		if (isPcaPFile(traceData.getTraceDir())) {
			mTraceVideoTimeStampFile = new FileOutputStream((traceData
					.getTraceDir().getParentFile().getAbsolutePath())
					+ File.separator + outVideoTimeFileName);
			mTraceVideoTimeStampWriter = new BufferedWriter(
					new OutputStreamWriter(mTraceVideoTimeStampFile));

		} else {
			mTraceVideoTimeStampFile = new FileOutputStream(
					(traceData.getTraceDir().getAbsolutePath())
							+ File.separator + outVideoTimeFileName);
			mTraceVideoTimeStampWriter = new BufferedWriter(
					new OutputStreamWriter(mTraceVideoTimeStampFile));
		}
	}
	/**
	 * Writes the video_time file with externalVideoStartTIme.
	 * 
	 * @param outputfilewritter, timestamp
	 * 			The BufferedWritter handle and externalVideoStartTime
	 *        
	 * @throws IOException.
	 */
	public void writeTimetoFile(BufferedWriter outputfilewriter, String timestamp)throws IOException {
		final String eol = System.getProperty("line.separator");
		outputfilewriter.write(timestamp + eol);
	}
	
	/**
	 * Closes the FileOutputStream and  BufferedWritter handles.
	 * 
	 * @param 
	 *        
	 * @throws IOException.
	 */
	public void closeVideoTraceTimeFile() throws IOException {
		mTraceVideoTimeStampWriter.close();
		mTraceVideoTimeStampFile.close();
	}
	/**
	 * Sets the current time position in the video. The video content, at the
	 * new current time position, is displayed in the Video player. The
	 * specified current time is validated to be between 0 and the length of the
	 * video.
	 * 
	 * @param dCurrentTimeInSeconds
	 *            The time position, in seconds, at which the video should be
	 *            displayed. The value must be in the range between 0 and the
	 *            length of the video.
	 */
	public synchronized void setMediaDisplayTime(double dCurrentTimeInSeconds) {
		if ((videoPlayer != null) && (videoPlayer.getDuration() != null)) {
			double videoTime = dCurrentTimeInSeconds - this.videoOffset;

			if (videoTime < 0.0) {
				videoPlayer.setMediaTime(new Time(0.0));
				return;
			}
			if (videoTime > videoPlayer.getDuration().getSeconds()) {
				videoPlayer.setMediaTime(new Time(videoPlayer.getDuration()
						.getSeconds()));
				return;
			}
			videoPlayer.setMediaTime(new Time(videoTime));
		}
	}

	/**
	 * Method to enable the default video player image as per the provided
	 * visible state.
	 * 
	 * @param bVisible
	 *            true enables the default image content in video player; else
	 *            video content displayes.
	 */
	private void setVideoNotAvailableImage(boolean bVisible) {
		jVideoLabel.setVisible(bVisible);
	}

	/**
	 * Clears the video player and its components.
	 * 
	 */
	public synchronized void clear() {

		// Make sure to remove the components before closing the video player
		if (visualComponent != null) {
			aroVideoPanel.remove(visualComponent);
			visualComponent = null;
		}
		if (controlComponent != null) {
			aroVideoPanel.remove(controlComponent);
			controlComponent = null;
		}

		// Close the video player
		if (videoPlayer != null) {
			if (videoPlayer.getState() == Controller.Started) {
				videoPlayer.stop();
			}
			videoPlayer.deallocate();
			videoPlayer.close();
			videoPlayer = null;
			videoOffset= 0.0;
		}
		syncVideoClicked = false;
	}

	/**
	 * Retrieves the media url from provided trace directory file after
	 * converting the file type from MP4 to MOV.
	 * 
	 * @param traceDirectory
	 *            {@link File} object that contains the trace directory.
	 * @return URL in {@link String} format.
	 * @throws IOException
	 */
	private synchronized String getMediaUrl(File traceDirectory)
			throws IOException {
		String result = null;
		if (((traceDirectory != null) && (traceDirectory.isDirectory()))
				|| isPcaPFile(traceDirectory)) {
			if (isPcaPFile(traceDirectory)) {
				traceDirectory = traceDirectory.getParentFile();
			}
			File videoFile = new File(traceDirectory,
					rb.getString("video.videoDisplayFile"));
			int totalVideoFiles = getVideoFilesCount(traceDirectory);
			if ((totalVideoFiles > 0) || !videoFile.exists()
					|| isMediaConversionError) {
				File videoFileFromDevice = new File(traceDirectory,
						rb.getString("video.videoFileOnDevice"));

				if (totalVideoFiles > 1) { // if more then one media file exists
											// in directory
					// alert message to user # of video file exists.
					MessageDialogFactory.showMessageDialog(this,
							rb.getString("video.error.multipleVideoFiles"));
					
				} else if(totalVideoFiles == 1) {
					// convert the file to .mov file
					File exVideoFile = new File(traceDirectory,
							rb.getString("video.exVideoDisplayFile"));					
					convertVideoToMOV(traceDirectory, existingExternalVideoFile, exVideoFile);
					if(exVideoFile.canRead()){
						result = "file:" + exVideoFile.getAbsolutePath();
					}

				} else if ((videoFileFromDevice.exists())
						|| (totalVideoFiles == 0)) {

					File exVideoMov = getExVideoMovIfPresent(traceDirectory);
					if (exVideoMov != null && exVideoMov.canRead()) {
						result = "file:" + exVideoMov.getAbsolutePath();
					} else {
						// No external converted MOV present, so convert the
						// native video source.
						convertVideoToMOV(traceDirectory, videoFileFromDevice,
								videoFile);
					}
				}
			}
			if ((videoFile.canRead())&&(totalVideoFiles == 0)){
				result = "file:" + videoFile.getAbsolutePath();
			}
		}
		return result;
	}
	/**
	 * It checks if we already have exVideo.Mov, returns the file if present.
	 * 
	 * @param traceDirectory
	 *            directory of pcap
	 * @return File exvideo.Mov
	 */
	private File getExVideoMovIfPresent(File traceDirectory) {
		File exVideoFileMatch[] = traceDirectory
				.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().equals("exvideo.mov");
					}
				});
		if (exVideoFileMatch.length > 0)
			return exVideoFileMatch[0];
		else
			return null;
	}

	/**
	 * This method return true if the input file is PCAP.
	 * 
	 * @param pcapFilename
	 *            pcapFilename.
	 * @return boolean true if pcap else false.
	 */

	private boolean isPcaPFile(File pcapFilename) {
		String extension = "";

		String filePath = pcapFilename.getPath();
		int dot = filePath.lastIndexOf(".");
		if (dot > 0) {
			extension = filePath.substring(dot + 1);
		}
		if ((extension.equals("cap")) || (extension.equals("pcap"))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Converts the external video source, MP4,WMA to MOV format.
	 *  @param traceDirectory,source,destination
	 *            {@link File} object that contains the trace directory.
	 *            source: video file to be converted.
	 *            destination: Converted video file with .MOV format.
	 * @throws IOException
	 */
	
	private void convertVideoToMOV(File traceDirectory,
		File source, File destination) throws IOException {
		String strProgramName = rb.getString("video.converter.programName");
		if (operatingSystem.startsWith("Mac")) {
			strProgramName = rb.getString("video.converter.programNameMac");
		}
		File fileFullPathFFMPEGProgram = new File(traceDirectory,
				strProgramName);

		// Overwrite any existing ffmpeg file in trace
		if (fileFullPathFFMPEGProgram.exists()) {
			fileFullPathFFMPEGProgram.delete();
		}
		InputStream is = AROVideoPlayer.class.getClassLoader()
				.getResourceAsStream(strProgramName);
		FileOutputStream fos = new FileOutputStream(fileFullPathFFMPEGProgram);
		try {
			byte[] buf = new byte[2048];
			int i;
			while ((i = is.read(buf)) > 0) {
				fos.write(buf, 0, i);
			}
			buf = null;
		} finally {
			fos.close();
		}
		if (!fileFullPathFFMPEGProgram.canExecute()) {
			fileFullPathFFMPEGProgram.setExecutable(true);
		}

		try {
			convertMp4ToMov(source, destination,
					fileFullPathFFMPEGProgram.getAbsolutePath());
		} finally {
			fileFullPathFFMPEGProgram.delete();
		}

	}
	/**
	 * This method returns external video files count.
	 *  @param traceDirectory,source,destination
	 *            {@link File} object that contains the trace directory.
	 * @return count of external video files.
	 */
	private int getVideoFilesCount(File traceDirectory) {
		int totalVideoFile = 0;
		if ((traceDirectory != null) && (traceDirectory.isDirectory())) {
			File[] files = traceDirectory.listFiles();

			for (File file : files) {
				if (!file.isDirectory()) {
					String fileExtension = getExtension(file.getName());
					if (fileExtension != null) {
						try {
							if(fileExtension.equals("3gp")){
								fileExtension = "_3GP";
							}
							switch (FileTypeEnum.valueOf(fileExtension.toUpperCase())) {
							case QT:
							case WMV:
							case WMA:
							case MPEG:
							case _3GP:
							case ASF:
							case AVI:
							case ASf:
							case DV:
							case MKV:
							case MPG:
							case RMVB:
							case VOB:
							case MOV:
							case MP4:
								if(!file.getName().equals("video.mp4") && !file.getName().equals("video.mov")  && !file.getName().equals("exvideo.mov")  ) {
									existingExternalVideoFile = file;
									totalVideoFile++;
								}
								break;
							default:
								break;
							}
						} catch (IllegalArgumentException iAEx) {
							
							continue;
						}
					} // end of if (fileExtension != null)
				}
			}
		}// end of if ((traceDirectory != null)
		return totalVideoFile;
	}
	/**
	 * Returns file extension.
	 *  @param fileName
	 *  @return extension.
	 */
	private String getExtension(String fileName) {
		int extensionIndex = fileName.lastIndexOf(".");
		if (extensionIndex == -1) {
			return null;
		}
		return fileName.substring(extensionIndex + 1, fileName.length());
	}
	
	
	/**
	 * Convert the MP4 file into Mov format.
	 * 
	 * @param videoInputFile
	 * @param videoOutputFile
	 * @param strFullPathConvertProgram
	 * @throws IOException
	 */
	private void convertMp4ToMov(File videoInputFile, File videoOutputFile,
			String strFullPathConvertProgram) throws IOException {

		if (!videoInputFile.exists()) {
			logger.fine(rb.getString("video.error.inputFileDoesNotExist")
					+ " " + videoInputFile.toString());
			return;
		} else if (videoInputFile.isDirectory()) {
			logger.fine(rb.getString("video.error.inputFileIsADirectory")
					+ " " + videoInputFile.toString());
			return;
		}

		String stdErrLine;
		if (videoOutputFile.exists()) {
			if (!videoOutputFile.isDirectory()) {
				if (videoOutputFile.canWrite()) {
					System.gc();
					boolean bSuccess = videoOutputFile.delete();
					if (!bSuccess) {
						logger.warning("Failed in deletion of output file "
								+ videoOutputFile.toString());
						return;
					}
				} else {
					logger.warning(rb
							.getString("video.error.lackPermissionToWriteToConvertFile")
							+ " " + videoOutputFile.toString());
					return;
				}
			} else {
				logger.warning(rb
						.getString("video.error.outputFileIsADirectory")
						+ videoOutputFile.toString());
				return;
			}
		}

		if (videoOutputFile.exists()) {
			logger.warning(rb
					.getString("video.error.priorFileVersionCannotBeDeleted")
					+ " " + videoOutputFile.toString());
			return;
		}

		String[] aConvertProgramParameters = rb.getString(
				"video.converter.programParameters").split(" ");
		String[] aArgs = new String[aConvertProgramParameters.length + 4];
		aArgs[0] = strFullPathConvertProgram;
		aArgs[1] = "-i";
		aArgs[2] = videoInputFile.getAbsolutePath();
		for (int iIdx = 0; iIdx < aConvertProgramParameters.length; iIdx++) {
			aArgs[3 + iIdx] = aConvertProgramParameters[iIdx];
		}
		aArgs[3 + aConvertProgramParameters.length] = videoOutputFile
				.getAbsolutePath();
		BufferedReader bufReaderInput = null;
		try {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Converting MP4 to MOV: ");
				for (int i = 0; i < aArgs.length; i++) {
					logger.log(Level.FINE, "    {0}", aArgs[i]);
				}
			}
			Process p = Runtime.getRuntime().exec(aArgs);

			InputStream stderr = p.getErrorStream();
			bufReaderInput = new BufferedReader(new InputStreamReader(stderr));
			while ((stdErrLine = bufReaderInput.readLine()) != null) {
				if (stdErrLine.contains("not permitted")
						|| stdErrLine.contains("atom not found")) {
					MessageDialogFactory.showMessageDialog(this,
							rb.getString("video.error.conversionFailed"),
							rb.getString("Error.title"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		} finally {
			logger.log(Level.FINE, "Converting MP4 to MOV was completed");
			if (bufReaderInput != null) {
				bufReaderInput.close();
			}
		}
	}
}
