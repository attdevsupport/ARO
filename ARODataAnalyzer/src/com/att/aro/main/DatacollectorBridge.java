/*
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

package com.att.aro.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.SyncService.SyncResult;
import com.att.aro.commonui.AROProgressDialog;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.TraceData;
import com.att.aro.videocapture.VideoCaptureThread;

/**
 * Provides a bridge to a device emulator so that ARO can start, stop, and
 * access traces on a device emulator.
 */
public class DatacollectorBridge {

	/**
	 * Enumeration that defines valid states for this data collector bridge
	 */
	public enum Status {
		/**
		 * The Data Collector is ready to run on the device emulator.
		 */
		READY,
		/**
		 * The Data Collector is starting to run on the device emulator.
		 */
		STARTING,
		/**
		 * The Data Collector has started running on the device emulator.
		 */
		STARTED,
		/**
		 * The Data Collector is preparing to stop running on the device
		 * emulator.
		 */
		STOPPING,
		/**
		 * The Data Collector has stopped running on the device emulator.
		 */
		STOPPED,
		/**
		 * The Data Collector is pulling trace data from the sdcard of the
		 * device emulator.
		 */
		PULLING
	}
	private static final Logger logger = Logger.getLogger(DatacollectorBridge.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String TCPDUMP = rb.getString("Name.tcpdump");
	private static final String APPVERAPK = rb.getString("Name.appverapk");
	private static final String KEYDB = rb.getString("Name.keyevent");
	private static final String EMULATORSTARBATCH = rb.getString("Name.startemul");
	private static final String WAITDEVICESBATCH = rb.getString("Name.waitfordevices");
	private static final String TRACE_ROOT = "/sdcard/ARO/";
	private static final int TCPDUMP_PORT = 50999;
	private static final String[] mDataDeviceCollectortraceFileNames = {
			TraceData.CPU_FILE, TraceData.APPID_FILE, TraceData.APPNAME_FILE,
			TraceData.TIME_FILE, TraceData.USER_EVENTS_FILE,
			TraceData.ACTIVE_PROCESS_FILE, TraceData.BATTERY_FILE,
			TraceData.BLUETOOTH_FILE, TraceData.CAMERA_FILE,
			TraceData.DEVICEDETAILS_FILE, TraceData.DEVICEINFO_FILE,
			TraceData.GPS_FILE, TraceData.NETWORKINFO_FILE,
			TraceData.PROP_FILE, TraceData.RADIO_EVENTS_FILE,
			TraceData.SCREEN_STATE_FILE, TraceData.SCREEN_ROTATIONS_FILE,
			TraceData.TIME_FILE, TraceData.USER_INPUT_LOG_EVENTS_FILE,
			TraceData.VIDEO_TIME_FILE, TraceData.WIFI_FILE, TraceData.PCAP_FILE };
	
	private static final String[] mDataEmulatorCollectortraceFileNames = { TraceData.CPU_FILE,
		TraceData.APPID_FILE, TraceData.APPNAME_FILE, TraceData.TIME_FILE,
		TraceData.USER_EVENTS_FILE, TraceData.PCAP_FILE,
		};

	private static final int AROSDCARD_TIMERCHECK_FREQUENCY= 2000;
	private static final int AROSDCARD_MIN_SPACEBYTES = 5120; // 5MB Minimum
																// Space
																// required to
																// start the
																// ARO-Data
																// Collector
																// Trace
	private static final int AROSDCARD_MIN_SPACEKBYTES_TO_COLLECT = 2048; // 2MB
																			// minimum
																			// space
																			// required
																			// to
																			// continue
																			// to
																			// collect
																			// traces
																			// while
																			// ARO-Data
																			// Collector
																			// trace.
	/**
	 *
	 * For the delay setting for the synchronization between the device and the file
	 * */
	private static final long DELAY_TO_FINISH_STORE_FILES_ON_DEVICE = 20000; //TODO: Will replace with a device communication to notify on traces stop.  

	/**
	 * Time in seconds to wait for the collector to start
	 */
	private static final int WAIT_TO_START_COLLECTOR = 30;
	
	private static final int WAIT_FOR_EMULATOR_READY = 52000;
	/**
	 * Timer to stop data collector
	 */
	private final Timer aroCollectorStopTimeTimer = new Timer();
	
	/**
	 * Indicates status of bridge
	 */
	private Status status = Status.READY;

	/**
	 * Currently selected trace folder name
	 */
	private String traceFolderName;

	/**
	 * Indicates whether video is being collected with any trace running on this
	 * bridge
	 */
	private boolean mARORecordTraceVideo;

	/**
	 * Indicates local directory where trace results will be stored
	 */
	private File localTraceFolder;

	/**
	 * Indicates path on emulator device where tcpdump output will reside
	 */
	private String deviceTracePath;

	/**
	 * ARO analyzer instance that is to be notified of data collector status
	 * updates
	 */
	private ApplicationResourceOptimizer mAROAnalyzer;

	/**
	 * Currently selected emulator device
	 */
	private IDevice mAndroidDevice;

	/**
	 * Thread that is used to collect video
	 */
	private VideoCaptureThread mVideoCapture;
	
	/**
	 * Local PC time when tcpdump was started
	 */
	private long tcpdumpStartTime;

	//String to handle the shellOutput 
	String shellLineOutput = null;
	
	/**
	 * Indicates if Data Collector was launched from command line argument
	 */
	
	private boolean usbDisconnectedFlag = false;
	private boolean isAroNotOnTheDevice = false;
	public boolean isAroNotOnTheDevice() {
		return isAroNotOnTheDevice;
	}

	public void setAroNotOnTheDevice(boolean isAroNotOnTheDevice) {
		this.isAroNotOnTheDevice = isAroNotOnTheDevice;
	}

	public boolean isUsbDisconnectedFlag() {
		return usbDisconnectedFlag;
	}

	public void setUsbDisconnectedFlag(boolean usbDisconnectedFlag) {
		this.usbDisconnectedFlag = usbDisconnectedFlag;
	}

	/**
	 * Timer that is used to constantly check available SD card space on the
	 * emulator
	 */
	private Timer checkSDCardSpace;
	
	private boolean multipleDevice = false;

	/**
	 * Used to track progress window
	 */
	private AROProgressDialog progress;

	private SwingWorker<Object, Object> collectorSwingWorker;
	
	/**
	 * Initializes a new instance of the DatacollectorBridge class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * 
	 * @param mApp
	 *            The ApplicationResourceOptimizer parent application
	 *            instance.
	 */
	public DatacollectorBridge(ApplicationResourceOptimizer mApp) {
		super();
		mAROAnalyzer = mApp;
	}

	/**
	 * Prompts the user for ARO Data Collector information, and starts the ARO
	 * Data Collector. This method should be run on the UI thread, because error
	 * messages may be displayed. Bridge status updates will be reported to the
	 * ApplicationResourceOptimizer parent instance that is associated with this
	 * class through the constructor.
	 */
	public void startARODataCollector() {
		if (checkAROEmulatorBridge(false) != null) {
			new DataCollectorStartDialog(mAROAnalyzer, this).setVisible(true);
		}
	}
	
	/**
	 * Initializes the video capture thread and starts the ARO Data Collector
	 * traces. This method should be run on the UI thread, because error
	 * messages may be displayed. Bridge status updates will be reported to the
	 * ApplicationResourceOptimizer parent instance that is associated with this
	 * class through the constructor.
	 * 
	 * @param traceFolderName
	 *            The name of the folder in which the ARO Data Collector trace
	 *            files should be stored.
	 * 
	 * @param mRecordTraceVideo
	 *            A boolean value that indicates whether to record video for
	 *            this trace or not.
	 */
	public synchronized void startARODataCollectorGUI(final String traceFolderName,
			boolean mRecordTraceVideo) {
		
		if (getStatus() == Status.READY) {
			
			checkDeviceAndInitializeVars(traceFolderName, mRecordTraceVideo);
			
			try {
				ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
				if (mAndroidDevice.isEmulator()) {
					// Make sure the root ARO trace directory exists on SD CARD
					mAndroidDevice.executeShellCommand("mkdir " + TRACE_ROOT,
							new ShellOutputReceiver());
					mAndroidDevice.executeShellCommand("mkdir "
							+ deviceTracePath, shelloutPut);
				
					if (shelloutPut.shellError) {
						MessageDialogFactory.showErrorDialog(mAROAnalyzer,
								rb.getString("Error.mkdirfail"));
					}
				}
				
				if (localTraceFolder.exists()) {
					// Prompt user for overwrite of trace folders
					Object[] options = { rb.getString("jdialog.option.yes"),
							rb.getString("jdialog.option.no") };
					int confirmSelected = 100;
					confirmSelected = JOptionPane.showOptionDialog(mAROAnalyzer,
							rb.getString("Error.tracedirexists"), rb.getString("aro.title.short"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);	
					
					if (confirmSelected == JOptionPane.YES_OPTION) {
						TraceData traceData = mAROAnalyzer.getTraceData();
						if (traceData != null && traceData.getTraceDir().equals(localTraceFolder)) {

							// Prompt user to clear current trace
							int currentTraceSelected = JOptionPane.showOptionDialog(mAROAnalyzer,
									rb.getString("Error.clearcurrenttrace"),
									rb.getString("aro.title.short"), JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							if (currentTraceSelected == JOptionPane.YES_OPTION) {
								mAROAnalyzer.clearTrace();
								deleteTraceFolderData(shelloutPut);
							} else {
								// Re-prompt for trace folder name
								setStatus(Status.READY);
								new DataCollectorStartDialog(mAROAnalyzer, this, traceFolderName,
										mRecordTraceVideo).setVisible(true);
								return;
							}
						} else {
							deleteTraceFolderData(shelloutPut);
						}
					} else if ((confirmSelected == JOptionPane.NO_OPTION )) {
						// Re-prompt for trace folder name
						setStatus(Status.READY);
						new DataCollectorStartDialog(mAROAnalyzer, this, traceFolderName,
								mRecordTraceVideo).setVisible(true);
						return;
					} else {
						setStatus(Status.READY);
						return;
					}
				} else if (shelloutPut.sdcardFull) {
					// SD Card is full
					MessageDialogFactory.showErrorDialog(mAROAnalyzer,
							rb.getString("Error.sdcardfull"));
					return;
				}

				// Show progress dialog that indicates
				setStatus(Status.STARTING);
				localTraceFolder.mkdirs();

				if (mAndroidDevice.isEmulator()){
					this.progress = new AROProgressDialog(mAROAnalyzer,
							rb.getString("Message.startcollector"));
				} else {	
					this.progress = new AROProgressDialog(mAROAnalyzer,
							rb.getString("Message.startcollectorOnDevice"));
				}
				progress.setVisible(true);

				// Worker thread that starts collector
				new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() {

						// Start the data collector
						return startDataCollector();
					}

					@Override
					protected void done() {
						super.done();
						progress.dispose();
						try {
							// Check for startup error
							String result = get();
							if (result != null) {
								logger.log(Level.SEVERE, "startDataCollectorOnEmulator :: "
										+ result);
								MessageDialogFactory.showErrorDialog(mAROAnalyzer, result);
								setStatus(Status.READY);
							}
						} catch (ExecutionException e) {
							MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
							setStatus(Status.READY);
						} catch (InterruptedException e) {
							MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
							setStatus(Status.READY);
						}
					}

				}.execute();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unexpected IOException starting data collector", e);
				if (mAndroidDevice.isEmulator()){
					MessageDialogFactory.showErrorDialog(
							mAROAnalyzer,
							MessageFormat.format(rb.getString("Error.withretrievingsdcardinfo"),
									e.getLocalizedMessage()));
				} else {
					MessageDialogFactory.showErrorDialog(
							mAROAnalyzer,
							MessageFormat.format(rb.getString("Error.withretrievingdevicesdcardinfo"),
									e.getLocalizedMessage()));
				}
			}
		} else if (getStatus() == Status.STARTING && traceFolderName.equals(this.traceFolderName)
				&& mRecordTraceVideo == mARORecordTraceVideo) {
			// Selected to start with same arguments
			return;
		} else {
			throw new IllegalStateException(rb.getString("Error.datacollectostart"));
		}
	}

	/**
	 * In case of command line arguments, it initializes the video capture thread and starts 
	 * the ARO Data Collector traces. This method should be run on the UI thread, because 
	 * error messages may be displayed. Bridge status updates will be reported to the
	 * ApplicationResourceOptimizer parent instance that is associated with this class 
	 * through the constructor.
	 * 
	 * @param traceFolderName
	 *            The name of the folder in which the ARO Data Collector trace
	 *            files should be stored.
	 * 
	 * @param mRecordTraceVideo
	 *            A boolean value that indicates whether to record video for
	 *            this trace or not.
	 */
	public synchronized void startARODataCollectorCmd(final String traceFolderName,
			boolean mRecordTraceVideo) {
		
		if (getStatus() == Status.READY) {
			
			if(checkDeviceAndInitializeVars(traceFolderName, mRecordTraceVideo) == null) {
				return;
			}
			CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.traceFolderInPropFile"), this.localTraceFolder.toString());
			
			try {
				ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
				if (mAndroidDevice.isEmulator()) {
					// Make sure the root ARO trace directory exists on SD CARD
					mAndroidDevice.executeShellCommand("mkdir " + TRACE_ROOT,
							new ShellOutputReceiver());
					mAndroidDevice.executeShellCommand("mkdir "
							+ deviceTracePath, shelloutPut);
				
					if (shelloutPut.shellError) {
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.mkdirfail"));
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						MessageDialogFactory.showErrorDialog(mAROAnalyzer,
								rb.getString("Error.mkdirfail"));
						return;
					}
				}
				
				if (localTraceFolder.exists()) {
					// If trace directory already exists.
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.traceFolderExists"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer,
							rb.getString("cmdline.traceFolderExists"));
					return;
				} else if (shelloutPut.sdcardFull) {
					// If SD Card is full.
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.sdcardfull"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer,
							rb.getString("Error.sdcardfull"));
					return;
				}

				updateDataCollectorMenuItem(false, false);
				// Show progress dialog that indicates
				setStatus(Status.STARTING);
				localTraceFolder.mkdirs();

				if (mAndroidDevice.isEmulator()){
					this.progress = new AROProgressDialog(mAROAnalyzer,
							rb.getString("Message.startcollector"));
				} else {	
					this.progress = new AROProgressDialog(mAROAnalyzer,
							rb.getString("Message.startcollectorOnDevice"));
				}
				progress.setVisible(true);

				// Worker thread that starts collector
				new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() {

						// Start the data collector
						return startDataCollector();
					}

					@Override
					protected void done() {
						super.done();
						progress.dispose();
						try {
							// Check for startup error
							String result = get();
							if (result != null) {
								logger.log(Level.SEVERE, "startDataCollectorOnEmulator :: "
										+ result);
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), result);
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
								setStatus(Status.READY);
							}
						} catch (ExecutionException e) {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getMessage());
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							setStatus(Status.READY);
						} catch (InterruptedException e) {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getMessage());
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							setStatus(Status.READY);
						}
					}

				}.execute();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unexpected IOException starting data collector", e);
				if (mAndroidDevice.isEmulator()){
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), 
							rb.getString("Error.withretrievingsdcardinfo") + e.getLocalizedMessage());
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					return;
				} else {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), 
							rb.getString("Error.withretrievingdevicesdcardinfo") + e.getLocalizedMessage());
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					return;
				}
			}
		} else if (getStatus() == Status.STARTING && traceFolderName.equals(this.traceFolderName)
				&& mRecordTraceVideo == mARORecordTraceVideo) {
			// Selected to start with same arguments
			return;
		} else {
			throw new IllegalStateException(rb.getString("Error.datacollectostart"));
		}
	}
	
	/**
	 * Checks that valid device is connected and initialize member variables for trace.
	 */
	IDevice checkDeviceAndInitializeVars(final String traceFolderName,
			boolean mRecordTraceVideo) {
		
		// Check that valid device is connected
		this.mAndroidDevice = checkAROEmulatorBridge(false);
		if (mAndroidDevice == null) {
			return null;
		}

		setUsbDisconnectedFlag(false);
		
		// Initialize member variables for trace
		this.traceFolderName = traceFolderName;
		this.mARORecordTraceVideo = mRecordTraceVideo;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0) {
			this.localTraceFolder = new File(rb.getString("Emulator.localtracepathmac")
					+ File.separator + traceFolderName);
		} else {
			this.localTraceFolder = new File(rb.getString("Emulator.localtracepath")
					+ File.separator + traceFolderName);
		}

		this.deviceTracePath = TRACE_ROOT + traceFolderName;
		return this.mAndroidDevice;
	}
	
	/**
	 * Stops the ARO Data Collector process threads. This method should be run
	 * on the UI thread, because error messages may be displayed. Bridge status
	 * updates will be reported to the ApplicationResourceOptimizerARO parent
	 * instance that is associated with this class through the constructor.
	 */
	public synchronized void stopARODataCollector() {
		if (getStatus() == Status.STARTED) {
			
			if(CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
				updateDataCollectorMenuItem(false, false);
			}
			
			// Display progress dialog
			this.progress = new AROProgressDialog(mAROAnalyzer,
					rb.getString("Message.stopcollector"));
			progress.setVisible(true);
			
			collectorSwingWorker.cancel(true);
			
			new SwingWorker<String, Object>() {

				@Override
				protected String doInBackground() {
					try {
						stopTcpDump();
						appendAppVersion();
						
						// Wait until data collector is stopped
						while (getStatus() != Status.STOPPED) {
							Thread.sleep(1000);
						}	
					} catch (IOException e) {
						logger.log(Level.WARNING, "Unexpected IOException stopping tcpdump", e);
					} catch (InterruptedException e) {
						logger.log(Level.WARNING, "Unexpected InterruptedException stopping tcpdump", e);
					}

					/*
					 * Sending a 'ps tcpdump' command to the device to check the collector is actually stopped.
					 * Checking the status of tcpdump with a one minute delay till tcpdump returns empty.
					 * */
					
					try {
						stopDataCollectoronDevice();
					} catch (IOException e) {
						logger.log(Level.WARNING,
								"Unexpected IOException stopping tcpdump", e);
					} 
					// Stop video if necessary
					if (mVideoCapture != null) {
						mVideoCapture.stopRecording();
						try {
							BufferedWriter videoTimeStampWriter = new BufferedWriter(
									new FileWriter(new File(localTraceFolder,TraceData.VIDEO_TIME_FILE)));
							try {
								// Writing a video time in file.
								videoTimeStampWriter.write(Double.toString(mVideoCapture.getVideoStartTime().getTime() / 1000.0));
								if (tcpdumpStartTime > 0) {
									videoTimeStampWriter.write(" "+ Double.toString(tcpdumpStartTime / 1000.0));
								}
							} finally {
								videoTimeStampWriter.close();
							}
						} catch (IOException e) {
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getLocalizedMessage());
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
							logger.log(Level.SEVERE,"Error writing video time file",e);
						} finally {
							mVideoCapture = null;
						}
					}
					return null;
				}
				
				@Override
				protected void done() {
					super.done();
					progress.dispose();
					/*If USB is disconnected*/
					if(isUsbDisconnectedFlag()){
						return;
					}
					startPullAROTraceFiles();					
				}
			}.execute();
		} else if (getStatus() == Status.STOPPING || getStatus() == Status.STOPPED) {
			// Ignore
		} else {
			throw new IllegalStateException(rb.getString("Error.datacollectostop"));
		}
	}

	/**
	 * Pulls trace files from the device emulator SD card to the local drive.
	 * This method should be run on the UI thread, because error messages may be
	 * displayed.
	 */
	public synchronized void startPullAROTraceFiles() {
	
		if (getStatus() == Status.STOPPED) {

			setStatus(Status.PULLING);

			this.progress = new AROProgressDialog(mAROAnalyzer,
					rb.getString("Message.pulltrace"));
			progress.setVisible(true);

			// Pull trace files from emulator in worker thread
			new SwingWorker<String, Object>() {

				@Override
				protected String doInBackground() throws IOException {
					if (mAndroidDevice.isEmulator()) {
						try {
							BufferedWriter devicedetailsWriter = new BufferedWriter(
									new FileWriter(new File(localTraceFolder,
											TraceData.DEVICEDETAILS_FILE)));
							try {
								// Writing device details in file.  
								final String eol = System
										.getProperty("line.separator");
								final String collector = rb
										.getString("Emulator.datacollectorpath")
										.substring(
												rb.getString(
														"Emulator.datacollectorpath")
														.lastIndexOf("/") + 1);
								devicedetailsWriter.write(collector + eol);
								devicedetailsWriter.write(rb
										.getString("bridge.device") + eol);
								String deviceManufacturer = mAndroidDevice
										.getProperty("ro.product.manufacturer");
								devicedetailsWriter
										.write((deviceManufacturer != null ? deviceManufacturer
												: "")
												+ eol);
								devicedetailsWriter.write(rb
										.getString("bridge.platform") + eol);
								devicedetailsWriter
										.write(mAndroidDevice
												.getProperty("ro.build.version.release")
												+ eol);
								devicedetailsWriter.write(" " + eol);

								final int deviceNetworkType = rb
										.getString("bridge.network.UMTS")
										.equalsIgnoreCase(
												mAndroidDevice
														.getProperty("gsm.network.type")) ? 3
										: -1;
								devicedetailsWriter.write(deviceNetworkType
										+ eol);
							} finally {
								devicedetailsWriter.close();
							}
						} catch (IOException e) {
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getLocalizedMessage());
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
							logger.log(Level.SEVERE, "Error writing device details file", e);
						}
					}

					//wait for device to save the traces
					//TODO a better approach ( may be a socket communication to make sure the device has done its part)
					try {
						Thread.sleep(DELAY_TO_FINISH_STORE_FILES_ON_DEVICE);
						} catch (InterruptedException e) {
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getLocalizedMessage());
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
							logger.log(Level.SEVERE,"Error calling sleep", e);
						}
					
					// Pull files from emulator/device one at a time
					// Device and emulator has a separate list since most of the files are not present on the emulator
					final SyncService service = mAndroidDevice.getSyncService();
					if (mAndroidDevice.isEmulator()){
					if (service != null) {
						for (String file : mDataEmulatorCollectortraceFileNames) {
							SyncResult result = service.pullFile(deviceTracePath + "/" + file,
									new File(localTraceFolder, file).getAbsolutePath(),
									SyncService.getNullProgressMonitor());
							if (result.getCode() != SyncService.RESULT_OK) {
								return result.getMessage();
							}
						}						
					}
					}
					else{
					if (service != null) {
						for (String file : mDataDeviceCollectortraceFileNames) {
							SyncResult result = service.pullFile(deviceTracePath + "/" + file,
									new File(localTraceFolder, file).getAbsolutePath(),
									SyncService.getNullProgressMonitor());
							if (result.getCode() != SyncService.RESULT_OK) {
								return result.getMessage();
							}
						}
						//We do need to pull multiple pcap files if they are 
						//available in trace directory (traffic1.cap,traffic2.cap ...)
						for (int index = 1; index < 50; index++) {
								final String fileName = "traffic" + index + ".cap";
								SyncResult result = service.pullFile(
										deviceTracePath + "/" + fileName,
										new File(localTraceFolder, fileName)
												.getAbsolutePath(), SyncService
												.getNullProgressMonitor());
								if (result.getCode() != SyncService.RESULT_OK) {
									System.out.println(result.getMessage());
									return result.getMessage();
								}
							}
					}
					}
					return null;
				}

				@Override
				protected void done() {

					// Close progress dialog
					super.done();
					progress.dispose();
					try {

						// Check for errors
						String result = get();
						if (result != null) {
							if (mAndroidDevice.isEmulator()){
								if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
									MessageDialogFactory.showErrorDialog(mAROAnalyzer, MessageFormat.format(
											rb.getString("Error.withretrievingsdcardinfo"), result));
								} else {
									CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), MessageFormat.format(
											rb.getString("Error.withretrievingsdcardinfo"), result));
									CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
								}
							}
							//TODO : To validate error message as this is misleading even when we are 
									//able to pull good traces. Need to validate. Doing intermin fox for 2.3 release
//							else{
//								
//								MessageDialogFactory.showErrorDialog(mAROAnalyzer, MessageFormat.format(rb.getString("Error.withretrievingdevicesdcardinfo"), result));
//							}
						}
						Double duration = TraceData.readTimes(localTraceFolder).getDuration();
						String durationStr;
						if (duration != null) {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.traceDurationInPropFile"), Integer.toString((int)Math.round(duration)));
							long dataTimeDuration = duration.longValue();
							long appTimeR = dataTimeDuration % 3600;
							long appUpHours = dataTimeDuration / 3600;
							long appUpMinutes = (appTimeR / 60);
							long appUpSeconds = appTimeR % 60;

							durationStr = (appUpHours < 10 ? "0" : "") + appUpHours + ":"
									+ (appUpMinutes < 10 ? "0" : "") + appUpMinutes + ":"
									+ (appUpSeconds < 10 ? "0" : "") + appUpSeconds;
						} else {
							durationStr = rb.getString("aro.unknown");
						}
						AROEmulatorTraceSummary summaryPanel = new AROEmulatorTraceSummary(
								localTraceFolder.getAbsolutePath(),
								mARORecordTraceVideo ? rb.getString("Emulator.dataValue") : rb
										.getString("Emulator.noDataValue"), durationStr);

						Object[] options = { rb.getString("Button.ok"), rb.getString("Button.open") };
						if(CommandLineHandler.getInstance().IsCommandLineEvent() == false) {
							if (JOptionPane.showOptionDialog(mAROAnalyzer, summaryPanel,
									rb.getString("confirm.title"), JOptionPane.YES_NO_OPTION,
									JOptionPane.INFORMATION_MESSAGE, null, options, options[0]) != JOptionPane.YES_OPTION) {
								mAROAnalyzer.openTrace(localTraceFolder.getAbsoluteFile());
							}
						} else {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.passed"));
							
						  	//Opening the trace pulled to local drive when launched from command line
							mAROAnalyzer.openTrace(localTraceFolder.getAbsoluteFile());
						}
					}
					/*Code to handle the USB disconnection issue
					 * Throwing an error dialog box showing device got disconnected.
					 * */
					catch (Exception e) {
						logger.log(Level.SEVERE, "Unexpected exception pulling traces", e);
						if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
							MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
						} else {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getLocalizedMessage());
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						}
						
					} finally {
						//Reset all variables for user for UI usage.
						updateDataCollectorMenuItem(true, false);

						setStatus(Status.READY);
						try {
							//Only deleting from Emulator, not from the device
							if (mAndroidDevice.isEmulator()){
								removeEmulatorData();
							}
						} catch (IOException e) {
							logger.log(Level.SEVERE, "Unexpected exception deleting trace files from emulator device", e);							
						}
					}
				}
			}.execute();
			// }
		} else if (getStatus() == Status.PULLING) {
			// Ignore
}		else {
			throw new IllegalStateException(rb.getString("Error.datacollectorpull"));
		}
	}

	/**
	 * Verifies if we have single emulator instance running and returns a handle
	 * to the emulator devices
	 * 
	 * @return Emulator device that can be used for data collector trace
	 */
	private IDevice checkAROEmulatorBridge(boolean initialCheck) {
		AndroidDebugBridge dataCollectorEmulatorbridge = AndroidDebugBridge.createBridge();

		// Wait for ADB device list to fetch connected devices
		int count = 0;
		while (dataCollectorEmulatorbridge.hasInitialDeviceList() == false) {
			try {
				Thread.sleep(100);
				count++;
			} catch (InterruptedException e) {
				// pass
			}
			// let's not wait > 3 sec.
			if (count > 30) {
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.emulatorconnection"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
				} 
				MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.emulatorconnection"));
				return null;
			}
		}

		// Find a connected emulator
		IDevice devices[] = dataCollectorEmulatorbridge.getDevices();
		IDevice result = null;
		if (dataCollectorEmulatorbridge.getDevices().length >= 1) {

			// Check if multiple mAndroidDevice instances are connected
			if (devices.length == 1) {
				result = devices[0];
			} else if (devices.length > 1) {
				this.multipleDevice = true;
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.deviceconnection"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
				} 
				MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.deviceconnection"));
				return null;
			}

			// Make sure the emulator SD card is ready and has enough space
			if (result.isEmulator()) {
				if (!checkEmulatorSDCard(result)) {
					return null;
				}
			}
		}

		// Check result
		if (result == null) {
			if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
				if (!initialCheck) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.emulatorconnection"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer,
							rb.getString("Error.emulatorconnection"));
				}
			} else {
				MessageDialogFactory.showErrorDialog(mAROAnalyzer,
					rb.getString("Error.emulatorconnection"));
			}
			return null;
		}
		result.createForward(TCPDUMP_PORT, TCPDUMP_PORT);
		return result;
	}

	/**
	 * Checks the device SD card to see that it exists and has enough space to
	 * run the data collector.
	 */
	private boolean checkEmulatorSDCard(IDevice device) {
		try {
			ShellCommandCheckSDCardOutputReceiver shellCheckSDCard = new ShellCommandCheckSDCardOutputReceiver(
					device);
			if (!shellCheckSDCard.isSDCardAttached()) {
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					try {
						ShellCommandCheckSDCardOutputReceiver shellCheckSDCard_Retry = new ShellCommandCheckSDCardOutputReceiver(device);
						if(!shellCheckSDCard_Retry.isSDCardAttached()) {
							if (device.isEmulator()) {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.sdcardconnection"));
								MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("cmdline.sdcardconnection"));
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.sdcardconnection"));
								MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("cmdline.sdcardconnection"));
							}
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							return false;
						} else {
							return true;
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE,"Failed to wait for devices", e);
					}
				}
				
				if (device.isEmulator()) {
					MessageDialogFactory.showMessageDialog(mAROAnalyzer,rb.getString("Error.sdcardnotavailableonemulator"));}
				else {					
					MessageDialogFactory.showMessageDialog(mAROAnalyzer,rb.getString("Error.sdcardnotavailable"));
				}
				return false;
			} else if (!shellCheckSDCard.doesSDCardHaveEnoughSpace(AROSDCARD_MIN_SPACEBYTES)) {

				// Not enough free space on SD card
				if (mAndroidDevice.isEmulator()) {
					if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.sdcardnotenoughspace"));
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.sdcardnotenoughspace"));
					} else {
						MessageDialogFactory.showMessageDialog(mAROAnalyzer,
								rb.getString("Error.sdcardnotenoughspace"));
					}
				} else {
					if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.devicesdcardnotenoughspace"));
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.devicesdcardnotenoughspace"));
					} else {
						MessageDialogFactory.showMessageDialog(mAROAnalyzer,
							rb.getString("Error.devicesdcardnotenoughspace"));
					}
				}
				return false;
			}
			
			if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
				if (progress != null)
					this.progress.dispose();
			}
			// SD card is ready
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException accessing device SD card", e);
			if(mAndroidDevice == null) {
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.sdcardconnection"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));					
				}
				MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("cmdline.sdcardconnection"));
				return false;
			}
			
			if (mAndroidDevice.isEmulator()) {
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), 
							MessageFormat.format(rb.getString("Error.withretrievingsdcardinfo"), e.getLocalizedMessage()));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.withretrievingsdcardinfo"));
				} else {
					MessageDialogFactory.showMessageDialog(
						mAROAnalyzer, MessageFormat.format(rb.getString("Error.withretrievingsdcardinfo"), e.getLocalizedMessage()));
				}
			} else {
				if (CommandLineHandler.getInstance().IsCommandLineEvent()) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), 
							MessageFormat.format(rb.getString("Error.withretrievingdevicesdcardinfo"), e.getLocalizedMessage()));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.withretrievingdevicesdcardinfo"));
				} else {
					MessageDialogFactory.showMessageDialog(
						mAROAnalyzer,MessageFormat.format(rb.getString("Error.withretrievingdevicesdcardinfo"),e.getLocalizedMessage()));
				}
			}
			return false;
		}
	}
	
	/**
	 * Method to check the SD card size during the trace collection time.
	 */
	private synchronized void checkSDCardStatus() {

		// Make sure previous timers are cancelled
		if (checkSDCardSpace != null) {
			checkSDCardSpace.cancel();
		}

		// Set up new timer to check current device
		this.checkSDCardSpace = new Timer();
		checkSDCardSpace.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				logger.info("checkSDCardStatus");
				try {
					ShellCommandCheckSDCardOutputReceiver shellCheckSDCard = new ShellCommandCheckSDCardOutputReceiver(
							mAndroidDevice);
					//To Check SD Card if not mounted. It will set the status to Ready so that once the device is ready to start taking traces.
					if (!shellCheckSDCard.isSDCardAttached()) {
						setStatus(Status.READY);
						return;
					}
					if (!shellCheckSDCard
							.doesSDCardHaveEnoughSpace(AROSDCARD_MIN_SPACEKBYTES_TO_COLLECT)) {

						// Not enough remaining SD card space
						if (mAndroidDevice.isEmulator()) {
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showMessageDialog(mAROAnalyzer,
										rb.getString("Error.sdcardnospacetocollect"));
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.sdcardnospacetocollect"));
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
						} else	{
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showMessageDialog(mAROAnalyzer,
										rb.getString("Error.devicesdcardnospacetocollect"));
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.devicesdcardnospacetocollect"));
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
						}
						// Stop the data collection
						stopARODataCollector();
						return;
					}
				} catch (IOException e) {
					try {
						stopTcpDump();
						//This is to show the user that the usb device got disconnected only once. 
						if ((e.getMessage().contains("device not found")) && (!isUsbDisconnectedFlag())){
							setUsbDisconnectedFlag(true);
							logger.log(Level.SEVERE,"Device got disconnected. Please check the connection");
							
							if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
								MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.devicenotfound"));
							} else {
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.devicenotfound"));
								CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
							}
							
							setStatus(Status.READY);
							checkSDCardSpace.cancel();
							checkSDCardSpace = null;							
						}
						else if (isUsbDisconnectedFlag()){
							logger.log(Level.SEVERE,"Device got disconnected. Please check the connection");							
						}
						else{
							//Ignore
						}
					} catch (IOException e1) {
						setUsbDisconnectedFlag(true);
						setStatus(Status.READY);
						if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
							MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.emulatoradbconnectionerror"));
						} else {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.emulatoradbconnectionerror"));
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						}
						
						logger.log(Level.SEVERE, "Connection to device or emulator is lost. Please wait for sometime before starting data collector.", e);
					}
					
					//Throw the restart the device dialog only of the device is connected
					if (!isUsbDisconnectedFlag()){
						if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
							MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.emulatorunexpectederror"));
						} else {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.emulatorunexpectederror"));
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						}						
						logger.log(Level.SEVERE, "IOException occurred checking SD card space", e);
					}
				}
			}
		}, AROSDCARD_TIMERCHECK_FREQUENCY, AROSDCARD_TIMERCHECK_FREQUENCY);
	}

	/**
	 * Pull the given file name from Jar and write to the local drive for use on
	 * emulator
	 * 
	 * @param filename
	 */
	private File getAroCollectorFilesFromJar(String filename) throws IOException {
		ClassLoader aroClassloader = DatacollectorBridge.class.getClassLoader();
		InputStream is = aroClassloader.getResourceAsStream(filename);
		OutputStream os = null;
		try {
			File result = new File(localTraceFolder, filename);
			if (result.createNewFile()) {
				os = new FileOutputStream(result);
				byte[] buffer = new byte[4096];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}
			return result;
		} finally {
			is.close();
			if (os != null) {
				os.close();
			}
		}
	}

	/**
	 * Starts tcpdump on emulator or Collector on USB connected device
	 * 
	 * For emulator, pushes the tcpdump executable to emulator and starts it on the
	 * shell. 
	 * For USB connected device, attempts to start Since this is run in a background thread, there is no UI
	 * interaction. If an error occurs, the error message is returned
	 * 
	 * @return Error message indicating error occurred starting data collector
	 *         or null on success
	 */
	private String startDataCollector() {
		try {

			// Make sure another tcpdump is not already running
			stopTcpDump();

			//check whether the collector is running on the device already.
			String stopTcpCmd = rb.getString("Emulator.stopTCPDump");
			mAndroidDevice.executeShellCommand(stopTcpCmd,
					new IShellOutputReceiver() {
			
				public boolean isCancelled() {
					return false;
				}
				
				public void flush() {
					
				}
				
				//Taking the length of the stopTCPCommand to make sure it returns empty
				public void addOutput(byte []data, int off, int len) {
					shellLineOutput = new String(data);					
				}			
			});
			
			logger.log(Level.INFO,"Checking whether the collector is not running on the device");
			if (isCollectorRunningInShell(shellLineOutput)) {
				/*
				 * if the collector version is running automatically,
				 * a) Delete the folder from the computer  
				 * b) Show an error dialog box showing Collector is already running on the device
				 * c) Put the status of the Analyzer to Ready
				 * */
				if (localTraceFolder.exists()) {
					deleteLocalTraceFolder();
				}
				
				if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
					MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.collectorisalreadyrunning"));
				} else {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.collectorisalreadyrunning"));
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.collectorisalreadyrunning"));
				}
				setStatus(Status.READY);			
				return null;
			}
			
			SyncService mService = mAndroidDevice.getSyncService();
			final String tcpdumpPath = rb
					.getString("Emulator.datacollectorpath")
					+ "/"
					+ TCPDUMP;
			SyncService.SyncResult keyDbpushResult = null;
			
			if (mAndroidDevice.isEmulator()) {
				// Copy tcpdump executable to emulator
				File tcpdump = getAroCollectorFilesFromJar(TCPDUMP);
				SyncService.SyncResult tcpdumpushResult = mService.pushFile(
						tcpdump.getAbsolutePath(), tcpdumpPath,
						SyncService.getNullProgressMonitor());
				tcpdump.delete();
				if (tcpdumpushResult.getCode() == SyncService.RESULT_OK) {
					ShellOutputReceiver mShellOutput = new ShellOutputReceiver();
					mAndroidDevice.executeShellCommand("chmod 777 "
							+ tcpdumpPath, mShellOutput);
				} else {
					return rb.getString("Error.withtcpdumppush");
				}

				// Copy keydb file to data collector
				File keydb = getAroCollectorFilesFromJar(KEYDB);
				final String keydbPath = mAndroidDevice.isEmulator() ? rb
						.getString("Emulator.datacollectorpath") + "/" + KEYDB
						: deviceTracePath + "/" + KEYDB;
				keyDbpushResult = mService.pushFile(keydb.getAbsolutePath(),
						keydbPath, SyncService.getNullProgressMonitor());
				keydb.delete();
			}
			
			if ((!mAndroidDevice.isEmulator()) || 
					((keyDbpushResult != null) && (keyDbpushResult.getCode() == SyncService.RESULT_OK))) {

				// Start worker thread that will start data collector components
				collectorSwingWorker = new SwingWorker<Object, Object>() {

					@Override
					public String doInBackground() throws IOException {

						// Video is recorded directly into local trace directory
						if (mAndroidDevice.isEmulator()) {
							// tcpdump command to be executed on emulator
							// This shell command will block until tcpdump is
							// stopped
							String strTcpDumpCommand = tcpdumpPath + " -w "
									+ traceFolderName + " not port 5555";
							ShellOutputReceiver tcpreceiver = new ShellOutputReceiver();
							tcpdumpStartTime = System.currentTimeMillis();
							mAndroidDevice.executeShellCommand(
									strTcpDumpCommand, tcpreceiver);
							logger.info("tcpdump stopped");
						} else {
							try {
								
								setUsbDisconnectedFlag(false);
								//TODO handle incorrect Collector version and start failure
								// Starts collector application on device.
								ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
								String shellCmd = MessageFormat.format(rb.getString("Emulator.startDeviceApk"), traceFolderName);
								mAndroidDevice.executeShellCommand(shellCmd, shelloutPut);

								/*
								 * Checking whether ARO collector is installed on the device or not
								 * by checking the shell output
								 * */
								setAroNotOnTheDevice(false);
								if (ShellOutputReceiver.noARO) {
									setAroNotOnTheDevice(true);
									logger.log(Level.SEVERE,"ARO Collector is not installed on the device");
									if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
										MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.nocollector"));
									} else {
										CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.nocollector"));
										CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
										MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.nocollector"));
										setStatus(Status.READY);
										return null;
									}
									setStatus(Status.READY);
									return rb.getString("Error.nocollector");
								}
								//keep thread to prevent cleanup until stop selected
								while (!isUsbDisconnectedFlag()) {
									Thread.sleep(5000);
									//TODO update to check socket for unexpected end of collector
								}								
							}
							catch (InterruptedException e){
								logger.log(Level.SEVERE, "Interrupted Exception - Sleep ");
							}
						}						
						return null;
					}

					/**
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						super.done();

						// tcpdump has exited or device collector has been stopped.
						// Make sure everything else is stopped
						synchronized (DatacollectorBridge.this) {
							if (isUsbDisconnectedFlag())
								return;
							
							setStatus(Status.STOPPING);
							if (isAroNotOnTheDevice())
								return;
							
							try {
									// Check for exceptions
									try {
										get();
									} catch (InterruptedException e1) {
										
										if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
											MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e1);
										} else {
											CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e1.getLocalizedMessage());
											CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
										}										
										logger.log(Level.SEVERE, "Error starting data collector", e1);
									} catch (ExecutionException e1) {
										if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
											MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e1);
										} else {
											CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e1.getLocalizedMessage());
											CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
										}
										logger.log(Level.SEVERE, "Error starting data collector", e1);
									} catch (CancellationException e) {
										//do nothing, USB video cancelled
									}
									
									//if USB device, stop collector
									if (!mAndroidDevice.isEmulator()) {
										ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
										// Starts collector application on device.
										
										String closeHomeCommand = rb.getString("Emulator.closeHomeActivity");
										mAndroidDevice.executeShellCommand(closeHomeCommand, shelloutPut);
										logger.log(Level.INFO, "broadcast to close home activity sent");
										
										String shellCmd = rb.getString("Emulator.stopDeviceApk");
										mAndroidDevice
												.executeShellCommand(
														shellCmd,
														shelloutPut);
										
									}
								} catch (Exception e) {
									e.printStackTrace();
									
									if (CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
										CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), "Exception # " + e.getLocalizedMessage());
										CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
									}
								} finally {
									setStatus(Status.STOPPED);
								}							
						}
					}
				};
				collectorSwingWorker.execute();

				// Check to see that tcpdump/collector is started
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					
					if (CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), "InterruptedException # " + e.getLocalizedMessage());
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					}
				}
				synchronized (DatacollectorBridge.this) {
					
					if (isAroNotOnTheDevice())
						return null;
					
					if (getStatus() != Status.STARTING) {
						return rb.getString("Error.datacollectostart");
					}
					checkSDCardStatus();					
				}
								
				int checkCount = 0;
				do {
					/*Check whether the collector is there on device memory
					 * If it is there raise a dialog asking the user to close it completely.
					 * */	
					if (ShellOutputReceiver.isActivityRunning == true){
						logger.log(Level.INFO,"ARO Collector activity is not started. Its current task has been brought to the front.");
						setStatus(Status.READY);
						return rb.getString("Error.collectoractivityondevice");
					} else {
						logger.log(Level.INFO,"No msg on the shell regarding the activity not there on the forefront");
					}		
					
					/*Checking whether collector is running on device*/
					if (isCollectorRunningOnDevice()) {
						setStatus(Status.STARTED);
						break;
					} else {
						setStatus(Status.STARTING);
					}
					
					checkCount++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE,"Exception while calling sleep");
						if (CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), "InterruptedException # " + e.getLocalizedMessage());
							CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
						}
					}
				} while (checkCount <= WAIT_TO_START_COLLECTOR);

				
				if (Status.STARTING == getStatus())	{
					/*Deletes the local folder from PC*/
					if (localTraceFolder.exists()) {
						deleteLocalTraceFolder();
					}
					
					//To close the collector activity on the device\emulator
					ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
					String shellCmd = rb.getString("Emulator.closeactivity");
					mAndroidDevice
							.executeShellCommand(
									shellCmd,
									shelloutPut);
					
					if (!CommandLineHandler.getInstance().IsCommandLineEvent()) {
						JOptionPane.showMessageDialog(mAROAnalyzer,
								rb.getString("Error.collectortimeout"),
								rb.getString("aro.title.short"),
								JOptionPane.INFORMATION_MESSAGE);
								setStatus(Status.READY);
					} else {
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("Error.collectortimeout"));
						CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
					}										
				}
				
				if (Status.STARTED == getStatus()) {
					if (mARORecordTraceVideo) {
						mVideoCapture = new VideoCaptureThread(mAndroidDevice,mAROAnalyzer.getTraceData(), new File(
								localTraceFolder, TraceData.VIDEO_MOV_FILE));
						mVideoCapture.start();
					} else {
						mVideoCapture = null;
					}


					if (mAndroidDevice.isEmulator()) {
						
						JOptionPane.showMessageDialog(mAROAnalyzer,
							rb.getString("Message.datacollectorrunning"),
							rb.getString("aro.title.short"),
							JOptionPane.INFORMATION_MESSAGE);
						updateDataCollectorMenuItem(false, true);
						
					} else {
						JOptionPane.showMessageDialog(mAROAnalyzer,
							rb.getString("Message.datacollectorrunningOnDevice"),
							rb.getString("aro.title.short"),
							JOptionPane.INFORMATION_MESSAGE);
						updateDataCollectorMenuItem(false, true);													
					}
				}
				
				return null;
			} else {
				return rb.getString("Error.withtcpdumppush");
			}
		} catch (IOException e) {
			if (e.getMessage().contains("device not found"))	{
				//Ignore as the usb device got disconnected message is getting thrown from the video capture thread
				if (CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), "IOException # "+ "device not found");
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
				}
				return null;				
			} else {
				String msg = rb.getString("Error.withemulatorioexecution");
				logger.log(Level.SEVERE, msg, e);
				
				if (CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), "IOException # "+ "device not found");
					CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
				}
				return msg;
			}
		}
	}

	
	/**
	 * Updates the status of the data collector bridge and notifies the ARO
	 * analyzer window.
	 * 
	 * @param status
	 */
	private void setStatus(Status status) {
		this.status = status;
		mAROAnalyzer.dataCollectorStatusCallBack(status);
	}
	
	/**
	 * Updates Data Collector menu items in case of command line arguments.
	 */
	private void updateDataCollectorMenuItem(boolean bStartItem, boolean bStopItem) {
		mAROAnalyzer.dataCollectorStatusCallBack(bStartItem, bStopItem);
	}

	
	/**
	 * Gets the status of the data collector bridge
	 */
	private Status getStatus() {
		return this.status;
	}	
	
	/**
	 * This function sends the STOP command to started ARO Data Collector on device 
	 * connected via USB bridge
	 * @throws IOException 
	 * 
	 * @throws IOException
	 */
	private void stopDataCollectoronDevice() throws IOException {
		//Cancel the SD card check
		if (checkSDCardSpace != null) {
			checkSDCardSpace.cancel();
			checkSDCardSpace = null;
		}
		final String stopTcpCmd = rb.getString("Emulator.stopTCPDump");
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"InterruptedException while sleep");
			}
			mAndroidDevice.executeShellCommand(stopTcpCmd,
					new IShellOutputReceiver() {
						public boolean isCancelled() {
							return false;
						}
						public void flush() {
						}
						public void addOutput(
								byte[] data, int off,
								int len) {
							shellLineOutput = new String(data);
						}
					});
		
		} while (shellLineOutput.contains("arodatacollector"));
	}
	/**
	 * This function connects to the server socket started by tcpdump and ends
	 * the tcpdump. The thread in which tcpdump is running will then be allowed
	 * to complete which will close the rest of the data collector.
	 */
	private void stopTcpDump() throws IOException {

		if (mAndroidDevice.isEmulator()) { //TODO need a check here
			// Cancel the SD card check
			if (checkSDCardSpace != null) {
				checkSDCardSpace.cancel();
				checkSDCardSpace = null;
			}
			Socket emulatorSocket = new Socket("127.0.0.1", TCPDUMP_PORT);
			try {
				OutputStream out = emulatorSocket.getOutputStream();
				if (out != null) {
					out.write("STOP".getBytes("ASCII"));
					out.flush();
					out.close();
				}
				out.close();
			} finally {
				emulatorSocket.close();
			}
		}
	}

	/**
	 * Class to get the output from the native emulator process and check it for
	 * errors.
	 */
	static class ShellOutputReceiver extends MultiLineReceiver {
		private static final Pattern FAILURE = Pattern.compile("failed");
		private static final Pattern SDCARDFULL = Pattern.compile("No space left on device");
		private static final Pattern ERROR = Pattern.compile("error");
		private static final Pattern SEG_ERROR = Pattern.compile("Segmentation fault");
		private static final Pattern NO_ARO = Pattern.compile("does not exist");
		private static final Pattern ACTIVITY_RUNNING = Pattern.compile("current task has been brought to the front");
		private static final Pattern FILE_EXISTS = Pattern.compile("File exists");
		
		private boolean shellError;
		private boolean sdcardFull;
		private static boolean noARO = false; //To check collector is not installed on the device
		private static boolean isActivityRunning = false; //To check collector is still on the memory of the device

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				logger.info(line);
				if (line.length() > 0) {

					noARO = false;
					isActivityRunning = false;
					
					//Check if file already exists
					Matcher file_exists = FILE_EXISTS.matcher(line);
					if (file_exists.find()) {
						return;
					}
					
					// set Android SD card memory full flag
					Matcher sdcardfull = SDCARDFULL.matcher(line);
					if (sdcardfull.find()) {
						sdcardFull = true;
						return;
					}
					
					// set Android shell error flag
					if (setShellError(FAILURE.matcher(line))) {
						return;
					}
					if (setShellError(ERROR.matcher(line))) {
						return;
					}
					if (setShellError(SEG_ERROR.matcher(line))) {
						return;
					}

					if (setShellError(NO_ARO.matcher(line))){
						noARO = true;
						return;
					}
					
					if (setShellError(ACTIVITY_RUNNING.matcher(line))){
						isActivityRunning = true;
						return;
					}
				}
			}
		}
		
		/**
		 * Sets shell error flag in case of a failure.
		 * 
		 * @param errorMatcher
		 * @return
		 */
		private boolean setShellError(Matcher errorMatcher) {
			if (errorMatcher.find()) {
				this.shellError = true;
				logger.log(Level.SEVERE, "Reading from emulator failed: {0}", errorMatcher.pattern().toString());
				return true;
			}
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		public boolean isShellError() {
			return shellError;
		}

		public boolean isSdcardFull() {
			return sdcardFull;
		}

	}

	/**
	 * Class to get the output from the native process and display when
	 * determining emulator SD card available space.
	 */
	private static class ShellCommandCheckSDCardOutputReceiver extends MultiLineReceiver {

		private static final String K = "K";
		private static final String M = "M";
		private static final String G = "G";

		private boolean emualatorSDCardAttached;
		private Long sdCardMemoryAvailable;

		/**
		 * Constructor that runs shell command on specified device to check SD
		 * card
		 * 
		 * @param device
		 * @throws IOException
		 */
		public ShellCommandCheckSDCardOutputReceiver(IDevice device) throws IOException {
			device.executeShellCommand("df", this);
		}

		@Override
		public void processNewLines(String[] lines) {
			for (String oneLine : lines) {
				
				
				//Checks the SDCard line from 'DF' command output
				if ((oneLine.toLowerCase().contains(rb.getString("Emulator.dfsdcardoutput").toLowerCase()))) { 
					// We find SD card is attached to emulator instance
					emualatorSDCardAttached = true;
					
					String strFileSize = null;
					String strValues[] = oneLine.split("\\s+");
					try {
						if (oneLine.contains(rb.getString("Emulator.availablespace"))) {
							strFileSize = strValues[5]; // 5th value in
														// strValues array for
														// 2.1 and 2.2 v of
														// emulator
						} else {
							strFileSize = strValues[3]; // 3rd value in
														// strValues array for
														// 2.3 and above of
														// emulator
						}
					} catch (IndexOutOfBoundsException e) {
						logger.log(Level.SEVERE, "Error parsing SD card df output", e);
					}

					if (strFileSize != null) {
						try {
							// Checks if the available size in KB/MB/GB, we
							// convert the SD
							// card available space in KB before checking for
							// minimum 5 MB space requirement on sd card
							long iFileSizeInKB = -1;
							if (strFileSize.contains(K)) {
								final String iFileSizeKB[] = strFileSize.split(K);
								iFileSizeInKB = Long.valueOf(iFileSizeKB[0]);
							} else if (strFileSize.contains(M)) {
								final String iFileSizeMB[] = strFileSize.split(M);
								iFileSizeInKB = Long.valueOf(iFileSizeMB[0]);
								// Converting to KB
								iFileSizeInKB = iFileSizeInKB * 1024;
							} else if (strFileSize.contains(G)) {
								final String iFileSizeGB[] = strFileSize.split(G);
								iFileSizeInKB = Long.valueOf(iFileSizeGB[0]);
								// Converting to KB
								iFileSizeInKB = iFileSizeInKB * (1024 * 1024);
							}
							if (iFileSizeInKB >= 0) {
								sdCardMemoryAvailable = iFileSizeInKB;
							}
						} catch (Exception e) {
							logger.log(
									Level.SEVERE,
									"ShellCommandCheckSDCardOutputReceiver number format exception",
									e);
						}

					}
				}

				//Checks the SDCard line from 'DF' command output
				if (oneLine.toLowerCase().contains("mnt/shell")) { 
					// We find SD card is attached to emulator instance
					emualatorSDCardAttached = true;
					
					String strFileSize = null;
					String strValues[] = oneLine.split("\\emu+");
					try {
						if (oneLine.contains(rb.getString("Emulator.availablespace"))) {
							strFileSize = strValues[5]; // 5th value in
														// strValues array for
														// 2.1 and 2.2 v of
														// emulator
						} else {
							strFileSize = strValues[3]; // 3rd value in
														// strValues array for
														// 2.3 and above of
														// emulator
						}
					} catch (IndexOutOfBoundsException e) {
						logger.log(Level.SEVERE, "Error parsing SD card df output", e);
					}

					if (strFileSize != null) {
						try {

							// Checks if the available size in KB/MB/GB, we
							// convert the SD
							// card available space in KB before checking for
							// minimum 5 MB space requirement on sd card
							long iFileSizeInKB = -1;
							if (strFileSize.contains(K)) {
								final String iFileSizeKB[] = strFileSize.split(K);
								iFileSizeInKB = Long.valueOf(iFileSizeKB[0]);
							} else if (strFileSize.contains(M)) {
								final String iFileSizeMB[] = strFileSize.split(M);
								iFileSizeInKB = Long.valueOf(iFileSizeMB[0]);
								// Converting to KB
								iFileSizeInKB = iFileSizeInKB * 1024;
							} else if (strFileSize.contains(G)) {
								final String iFileSizeGB[] = strFileSize.split(G);
								iFileSizeInKB = Long.valueOf(iFileSizeGB[0]);
								// Converting to KB
								iFileSizeInKB = iFileSizeInKB * (1024 * 1024);
							}
							if (iFileSizeInKB >= 0) {
								sdCardMemoryAvailable = iFileSizeInKB;
							}
						} catch (Exception e) {
							logger.log(
									Level.SEVERE,
									"ShellCommandCheckSDCardOutputReceiver number format exception",
									e);
						}
					}
				}				
			}			
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		public boolean isSDCardAttached() {
			return emualatorSDCardAttached;
		}

		public boolean doesSDCardHaveEnoughSpace(long kbs) {
			return isSDCardAttached()
					&& (sdCardMemoryAvailable == null || sdCardMemoryAvailable > kbs);
		}
	}

	/**
	 * Removes recently collected trace file and directory.
	 */
	private void removeEmulatorData() throws IOException {
		ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/*", shelloutPut);
		mAndroidDevice.executeShellCommand("rmdir " + deviceTracePath, shelloutPut);
	}

	/**
	 * Deletes the trace data's from the local folder.
	 * 
	 * @param shelloutPut
	 * @throws IOException
	 */
	private void deleteTraceFolderData(ShellOutputReceiver shelloutPut) throws IOException {
		// Delete all the trace files from the folders
		if (localTraceFolder.isDirectory()) {
			for (File f : localTraceFolder.listFiles()) {
				f.delete();
			}
		}
		localTraceFolder.delete();
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/*", shelloutPut);
	}

	/*
	 * Deletes only the trace folder from the local folder
	 * */
	private void deleteLocalTraceFolder() throws IOException	{
		if (localTraceFolder.isDirectory()) {
			for (File f: localTraceFolder.listFiles()) {
				f.delete();
			}
		}
		localTraceFolder.delete();
	}
	/**
	 * Appends application version numbers in the appname trace file.
	 */
	private void appendAppVersion() {
		try {
			if (mAndroidDevice.isEmulator()) {
				// Creates a temporary file having folder name
				File tempFile = new File(this.localTraceFolder + File.separator
						+ rb.getString("Emulator.tempfile"));
				FileWriter fstream = new FileWriter(tempFile);
				BufferedWriter out = new BufferedWriter(fstream);
				try {
					out.write(traceFolderName);
				} finally {
					out.close();
				}

				// Pushing temporary file having trace folder name to emulator.
				mAndroidDevice.getSyncService().pushFile(
						tempFile.getAbsolutePath(),
						TRACE_ROOT + rb.getString("Emulator.tempfile"),
						SyncService.getNullProgressMonitor());
				File verCollectingApp = getAroCollectorFilesFromJar(APPVERAPK);
				// Installs application version collection apk in emulator.
				mAndroidDevice.installPackage(verCollectingApp.getPath(), false);
				Thread.sleep(1000);
				ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
				// Starts application in emulator.
				mAndroidDevice.executeShellCommand(
						rb.getString("Emulator.startapk"), shelloutPut);
				if (shelloutPut.shellError) {
					logger.log(Level.SEVERE,
							rb.getString("Error.apknotabletostart"));
				}
				Thread.sleep(2000);
				// Uninstalls in emulator.
				mAndroidDevice.uninstallPackage(rb
						.getString("Emulator.apkpackage"));
				// Delete local files.
				tempFile.delete();
				verCollectingApp.delete();
			}
		} catch (IOException e1) {
			String msg = rb.getString("Error.withemulatorioexecution");
			MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.adbcollectorfail"));
			logger.log(Level.SEVERE, msg, e1);
		} catch (InterruptedException e) {
			String msg = rb.getString("Error.withemulatorioexecution");
			MessageDialogFactory.showErrorDialog(mAROAnalyzer, rb.getString("Error.adbcollectorfail"));
			logger.log(Level.SEVERE, msg, e);
		}
	}
	
	
	/*Method to check whether the collector is running on the device.
	 * */
	private boolean isCollectorRunningOnDevice() throws IOException	{
		String stopTcpCmd = rb.getString("Emulator.stopTCPDump");
		mAndroidDevice.executeShellCommand(stopTcpCmd,
			new IShellOutputReceiver(){
			
				public boolean isCancelled(){
					return false;
				}
				
				public void flush(){
					
				}
				
				//Taking the length of the stopTCPCommand to make sure it returns empty
				public void addOutput(byte []data, int off, int len)		{
					shellLineOutput = new String(data);
				}
			
			});
		
		logger.log(Level.INFO,"Checking whether the collector is running on the device");
	
		if  (isCollectorRunningInShell(shellLineOutput))
			return true;
		else 
			return false;		
	}

	/*Comparing with shellOuput to make sure arodatacollector running on device*/
	private boolean isCollectorRunningInShell(String shellOutput) {
		logger.log(Level.INFO,"shelloutput: " + shellOutput);
		return shellOutput.contains("arodatacollector");
	}	
}