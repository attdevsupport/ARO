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
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.media.Time;
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
public class AROVideoPlayer extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Double SHORT_SNIPPET_DURATION_IN_SECONDS = 0.1;
	private static final Double SHORT_SNIPPET_DURATION_IN_MILLISECONDS = SHORT_SNIPPET_DURATION_IN_SECONDS * 1000.0;
	private static final int SHORT_SNIPPET_DURATION_MILLISECONDS = SHORT_SNIPPET_DURATION_IN_MILLISECONDS
			.intValue();
	private static final float PLAYBACK_RATE = 1.0f; // 5.0f
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String operatingSystem = System.getProperty("os.name");
	private static Logger logger = Logger.getLogger(AROVideoPlayer.class
			.getName());

	private Component visualComponent;
	private Component controlComponent;
	private double videoOffset;
	private AROAdvancedTabb aroAdvancedTab;
	private JPanel aroVideoPanel;
	private JLabel jVideoLabel;
	private Player videoPlayer;

	/**
	 * Runnable used to keep diagnostic chart in sync with video
	 */
	private Runnable syncThread = new Runnable() {

		private double seconds;
		
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
				if (currentVideoTime != null && currentVideoTime.getSeconds() != seconds) {
					if (aroAdvancedTab != null) {
						seconds = currentVideoTime.getSeconds();
						
						try {
							
							// Update diagnostics on AWT thread
							SwingUtilities.invokeAndWait(new Runnable() {

								@Override
								public void run() {
							
									aroAdvancedTab.setTimeLineLinkedComponents(seconds + videoOffset);
								}
								
							});
						} catch (InterruptedException e) {
							logger.log(Level.SEVERE, "InterruptedException", e);
						} catch (InvocationTargetException e) {
							logger.log(Level.SEVERE, "InvocationTargetException", e);
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
		setResizable(false);
		jVideoLabel = new JLabel(Images.NO_VIDEO_AVAILABLE.getIcon());
		aroVideoPanel = new JPanel();
		aroVideoPanel.setLayout(new BorderLayout());
		aroVideoPanel.add(jVideoLabel, BorderLayout.CENTER);
		setContentPane(aroVideoPanel);
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
				|| (mediaUrl = getMediaUrl(analysisData.getTraceData().getTraceDir())) == null) {
			setVideoNotAvailableImage(true);
			return;
		}

		TraceData traceData = analysisData.getTraceData();
		double videoStartTime = traceData.getVideoStartTime();
		this.videoOffset = videoStartTime > 0.0 ? videoStartTime
				- ((double) traceData.getTraceDateTime().getTime() / 1000)
				: 0.0;

		MediaLocator mlr = new MediaLocator(mediaUrl);
		try {
			videoPlayer = Manager.createRealizedPlayer(mlr);
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
						
						new Thread(syncThread).start();
					}
				}
			});
			setMediaDisplayTime(SHORT_SNIPPET_DURATION_IN_SECONDS);
 
		} catch (NoPlayerException e) {
			MessageDialogFactory.showUnexpectedExceptionDialog(this, e);
			return;
		} catch (CannotRealizeException e) {
			MessageDialogFactory.showUnexpectedExceptionDialog(this, e);
			return;
		} catch (IOException e) {
			MessageDialogFactory.showUnexpectedExceptionDialog(this, e);
			return;
		}
		setVisible(true);
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
			if (videoTime > SHORT_SNIPPET_DURATION_IN_SECONDS) {
				videoTime -= SHORT_SNIPPET_DURATION_IN_SECONDS;
			}
			videoPlayer.setMediaTime(new Time(videoTime));
			videoPlayer.start();
			try {
				Thread.sleep(SHORT_SNIPPET_DURATION_MILLISECONDS);
			} catch (InterruptedException e) {
			}
			videoPlayer.stop();
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
	 * Method to clear the video player and its components
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
		}
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
		if ((traceDirectory != null) && (traceDirectory.isDirectory())) {
			File videoFile = new File(traceDirectory,
					rb.getString("video.videoDisplayFile"));
			if (!videoFile.exists()) {
				File videoFileFromDevice = new File(traceDirectory,
						rb.getString("video.videoFileOnDevice"));
				if (videoFileFromDevice.exists()) {

					String strProgramName = rb
							.getString("video.converter.programName");
					if (operatingSystem.startsWith("Mac")) {
						strProgramName = rb
								.getString("video.converter.programNameMac");
					}
					File fileFullPathFFMPEGProgram = new File(traceDirectory,
							strProgramName);
					if (!fileFullPathFFMPEGProgram.exists()) {
						InputStream is = AROVideoPlayer.class.getClassLoader()
								.getResourceAsStream(strProgramName);
						FileOutputStream fos = new FileOutputStream(
								fileFullPathFFMPEGProgram);
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
					}
					if (!fileFullPathFFMPEGProgram.canExecute()) {
						fileFullPathFFMPEGProgram.setExecutable(true);
					}
					convertMp4ToMov(videoFileFromDevice, videoFile,
							fileFullPathFFMPEGProgram.getAbsolutePath());
					fileFullPathFFMPEGProgram.delete();
				}
			}
			if (videoFile.canRead()) {
				result = "file:" + videoFile.getAbsolutePath();
			}
		}
		return result;
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
			logger.warning(rb.getString("video.error.inputFileDoesNotExist")
					+ " " + videoInputFile.toString());
			return;
		} else if (videoInputFile.isDirectory()) {
			logger.warning(rb.getString("video.error.inputFileIsADirectory")
					+ " " + videoInputFile.toString());
			return;
		}

		String stdErrLine;
		if (videoOutputFile.exists()) {
			if (!videoOutputFile.isDirectory()) {
				if (videoOutputFile.canWrite()) {
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
			if (bufReaderInput != null) {
				bufReaderInput.close();
			}
		}
	}
}
