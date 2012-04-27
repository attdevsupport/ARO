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


package com.att.aro.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.SyncService.SyncResult;
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

	private static final Logger logger = Logger
			.getLogger(DatacollectorBridge.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private static final String TCPDUMP = rb.getString("Name.tcpdump");
	private static final String KEYDB = rb.getString("Name.keyevent");
	private static final String TRACE_ROOT = "/sdcard/ARO/";
	private static final int TCPDUMP_PORT = 50999;
	private static final String[] mDataCollectortraceFileNames = {
			TraceData.CPU_FILE, TraceData.APPID_FILE, TraceData.APPNAME_FILE,
			TraceData.TIME_FILE, TraceData.USER_EVENTS_FILE,
			TraceData.PCAP_FILE };

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

	/**
	 * Local PC time when tcpdump was stopped
	 */
	private long tcpdumpEndTime;

	/**
	 * Duration between tcpdump start and stop.
	 */
	private long dataTimeDuration;

	/**
	 * Timer that is used to constantly check available SD card space on the
	 * emulator
	 */
	private Timer checkSDCardSpace;

	/**
	 * Used to track progress window
	 */
	private DataCollectorProgressDialog progress;

	/**
	 * Initializes a new instance of the DatacollectorBridge class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * 
	 * @param mApp
	 *            – The ApplicationResourceOptimizer parent application
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
		if (checkAROEmulatorBridge() != null) {
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
	 * @param tracefoldername
	 *            - The name of the folder in which the ARO Data Collector trace
	 *            files should be stored.
	 * 
	 * @param mRecordTraceVideo
	 *            – A boolean value that indicates whether to record video for
	 *            this trace or not.
	 */
	public synchronized void startARODataCollector(
			final String traceFolderName, boolean mRecordTraceVideo) {

		// Make sure proper status of bridge
		if (status == Status.READY) {

			// Check that valid device is connected
			this.mAndroidDevice = checkAROEmulatorBridge();
			if (mAndroidDevice == null) {
				return;
			}

			// Initialize member variables for trace
			this.traceFolderName = traceFolderName;
			this.mARORecordTraceVideo = mRecordTraceVideo;
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("mac") >= 0) {
				this.localTraceFolder = new File(
						rb.getString("Emulator.localtracepathmac")
								+ File.separator + traceFolderName);
			} else {
				this.localTraceFolder = new File(
						rb.getString("Emulator.localtracepath")
								+ File.separator + traceFolderName);
			}

			this.deviceTracePath = TRACE_ROOT + traceFolderName;
			try {

				// Make sure the root ARO trace directory exists on SD CARD
				mAndroidDevice.executeShellCommand("mkdir " + TRACE_ROOT,
						new ShellOutputReceiver());
				ShellOutputReceiver shelloutPut = new ShellOutputReceiver();
				mAndroidDevice.executeShellCommand("mkdir " + deviceTracePath,
						shelloutPut);
				if (shelloutPut.shellError || localTraceFolder.exists()) {

					// Prompt user for overwrite of trace folders
					Object[] options = { rb.getString("jdialog.option.yes"),
							rb.getString("jdialog.option.no") };
					int confirmSelected = JOptionPane.showOptionDialog(
							mAROAnalyzer,
							rb.getString("Error.tracedirexists"),
							MessageFormat.format(
									rb.getString("aro.title.short"), ""),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);
					if (confirmSelected == JOptionPane.YES_OPTION) {

						// Delete all the trace files from the folders
						if (localTraceFolder.isDirectory()) {
							for (File f : localTraceFolder.listFiles()) {
								f.delete();
							}
						}
						localTraceFolder.delete();
						mAndroidDevice.executeShellCommand("rm "
								+ deviceTracePath + "/*", shelloutPut);
					} else if ((confirmSelected == JOptionPane.NO_OPTION)) {

						// Re-prompt for trace folder name
						setStatus(Status.READY);
						new DataCollectorStartDialog(mAROAnalyzer, this,
								traceFolderName, mRecordTraceVideo)
								.setVisible(true);
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
				this.progress = new DataCollectorProgressDialog(mAROAnalyzer,
						rb.getString("Message.startcollector"));
				progress.setVisible(true);

				// Worker thread that starts collector
				new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() {

						// Start the data collector
						return startDataCollectorOnEmulator();
					}

					@Override
					protected void done() {
						super.done();
						progress.dispose();
						try {
							// Check for startup error
							String result = get();
							if (result != null) {
								logger.log(Level.SEVERE,
										"startDataCollectorOnEmulator :: "
												+ result);
								MessageDialogFactory.showErrorDialog(
										mAROAnalyzer, result);
								setStatus(Status.READY);
							}
						} catch (ExecutionException e) {
							MessageDialogFactory.showUnexpectedExceptionDialog(
									mAROAnalyzer, e);
							setStatus(Status.READY);
						} catch (InterruptedException e) {
							MessageDialogFactory.showUnexpectedExceptionDialog(
									mAROAnalyzer, e);
							setStatus(Status.READY);
						}
					}

				}.execute();
			} catch (IOException e) {
				logger.log(Level.WARNING,
						"Unexpected IOException starting data collector", e);
				MessageDialogFactory.showErrorDialog(mAROAnalyzer,
						MessageFormat.format(
								rb.getString("Error.withretrievingsdcardinfo"),
								e.getLocalizedMessage()));
			}
		} else if (status == Status.STARTING
				&& traceFolderName.equals(this.traceFolderName)
				&& mRecordTraceVideo == mARORecordTraceVideo) {
			// Selected to start with same arguments
			return;
		} else {
			throw new IllegalStateException(
					rb.getString("Error.datacollectostart"));
		}
	}

	/**
	 * Stops the ARO Data Collector process threads. This method should be run
	 * on the UI thread, because error messages may be displayed. Bridge status
	 * updates will be reported to the ApplicationResourceOptimizerARO parent
	 * instance that is associated with this class through the constructor.
	 */
	public synchronized void stopARODataCollector() {

		if (status == Status.STARTED) {
			// Display progress dialog
			this.progress = new DataCollectorProgressDialog(mAROAnalyzer,
					rb.getString("Message.stopcollector"));
			progress.setVisible(true);

			new SwingWorker<String, Object>() {

				@Override
				protected String doInBackground() {

					try {
						stopTcpDump();
					} catch (IOException e) {
						logger.log(Level.WARNING,
								"Unexpected IOException stopping tcpdump", e);
					}
					return null;
				}
			}.execute();
		} else if (status == Status.STOPPING || status == Status.STOPPED) {
			// Ignore
		} else {
			throw new IllegalStateException(
					rb.getString("Error.datacollectostop"));
		}

	}

	/**
	 * Pulls trace files from the device emulator SD card to the local drive.
	 * This method should be run on the UI thread, because error messages may be
	 * displayed.
	 */
	public synchronized void startPullAROTraceFiles() {
		if (status == Status.STOPPED) {

			setStatus(Status.PULLING);

			// Pull trace files from emulator in worker thread
			new SwingWorker<String, Object>() {

				@Override
				protected String doInBackground() throws IOException {

					try {
						BufferedWriter devicedetailsWriter = new BufferedWriter(
								new FileWriter(new File(localTraceFolder,
										TraceData.DEVICEDETAILS_FILE)));
						try {
							// Writing device details in file.
							final String eol = System
									.getProperty("line.separator");
							final String collector = rb.getString(
									"Emulator.datacollectorpath").substring(
									rb.getString("Emulator.datacollectorpath")
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
							devicedetailsWriter.write(mAndroidDevice
									.getProperty("ro.build.version.release")
									+ eol);
							devicedetailsWriter.write(" " + eol);

							final int deviceNetworkType = rb.getString(
									"bridge.network.UMTS").equalsIgnoreCase(
									mAndroidDevice
											.getProperty("gsm.network.type")) ? 3
									: -1;
							devicedetailsWriter.write(deviceNetworkType + eol);
						} finally {
							devicedetailsWriter.close();
						}
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								mAROAnalyzer, e);
						logger.log(Level.SEVERE,
								"Error writing device details file", e);
					}

					// Pull files from emulator one at a time
					final SyncService service = mAndroidDevice.getSyncService();
					if (service != null) {
						for (String file : mDataCollectortraceFileNames) {
							SyncResult result = service.pullFile(
									deviceTracePath + "/" + file, new File(
											localTraceFolder, file)
											.getAbsolutePath(), SyncService
											.getNullProgressMonitor());
							if (result.getCode() != SyncService.RESULT_OK) {
								return result.getMessage();
							}
						}
					}
					return null;
				}

				@Override
				protected void done() {

					// Close progress dialog
					super.done();

					try {

						// Check for errors
						String result = get();
						if (result != null) {
							MessageDialogFactory
									.showErrorDialog(
											mAROAnalyzer,
											MessageFormat.format(
													rb.getString("Error.withretrievingsdcardinfo"),
													result));
						}

						removeEmulatorData();

						AROEmulatorTraceSummary summaryPanel = new AROEmulatorTraceSummary(
								localTraceFolder.getAbsolutePath(),
								mARORecordTraceVideo ? rb
										.getString("Emulator.dataValue") : rb
										.getString("Emulator.noDataValue"),
								getTraceDuration());

						progress.dispose();
						Object[] options = { rb.getString("Button.ok"),
								rb.getString("Button.open") };
						if (JOptionPane.showOptionDialog(mAROAnalyzer,
								summaryPanel, rb.getString("confirm.title"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.INFORMATION_MESSAGE, null, options,
								options[0]) != JOptionPane.YES_OPTION) {
							mAROAnalyzer.openTrace(localTraceFolder
									.getAbsoluteFile());
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE,
								"Unexpected exception pulling traces", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(
								mAROAnalyzer, e);
					} finally {
						setStatus(Status.READY);
					}
				}
			}.execute();
			// }
		} else if (status == Status.PULLING) {
			// Ignore
		} else {
			throw new IllegalStateException(
					rb.getString("Error.datacollectorpull"));
		}
	}

	/**
	 * Verifies if we have single emulator instance running and returns a handle
	 * to the emulator devices
	 * 
	 * @return Emulator device that can be used for data collector trace
	 */
	private IDevice checkAROEmulatorBridge() {
		AndroidDebugBridge dataCollectorEmulatorbridge = AndroidDebugBridge
				.createBridge();

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
				MessageDialogFactory.showErrorDialog(mAROAnalyzer,
						rb.getString("Error.emulatorconnection"));
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
				MessageDialogFactory.showErrorDialog(mAROAnalyzer,
						rb.getString("Error.deviceconnection"));
				return null;
			}

			// Check to make sure if emulator instance is connected
			if (!result.isEmulator()) {
				MessageDialogFactory.showErrorDialog(mAROAnalyzer,
						rb.getString("Error.emulatorconnection"));
				return null;
			}

			// Make sure the emulator SD card is ready and has enough space
			if (!checkEmulatorSDCard(result)) {
				return null;
			}
		}

		// Check result
		if (result == null) {
			MessageDialogFactory.showErrorDialog(mAROAnalyzer,
					rb.getString("Error.emulatorconnection"));
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

				// No SD card found
				MessageDialogFactory.showMessageDialog(mAROAnalyzer,
						rb.getString("Error.sdcardnotavailable"));
				return false;
			} else if (!shellCheckSDCard
					.doesSDCardHaveEnoughSpace(AROSDCARD_MIN_SPACEBYTES)) {

				// Not enough free space on SD card
				MessageDialogFactory.showMessageDialog(mAROAnalyzer,
						rb.getString("Error.sdcardnotenoughspace"));
				return false;
			}

			// SD card is ready
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException accessing device SD card", e);
			MessageDialogFactory.showMessageDialog(
					mAROAnalyzer,
					MessageFormat.format(
							rb.getString("Error.withretrievingsdcardinfo"),
							e.getLocalizedMessage()));
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
					if (!shellCheckSDCard
							.doesSDCardHaveEnoughSpace(AROSDCARD_MIN_SPACEKBYTES_TO_COLLECT)) {

						// Not enough remaining SD card space
						MessageDialogFactory.showMessageDialog(mAROAnalyzer,
								rb.getString("Error.sdcardnospacetocollect"));

						// Stop the data collection
						stopARODataCollector();
						return;
					}
				} catch (IOException e) {
					logger.log(Level.WARNING,
							"IOException occurred checking SD card space", e);
				}
			}
		}, 60000, 60000);
	}

	/**
	 * Pull the given file name from Jar and write to the local drive for use on
	 * emulator
	 * 
	 * @param filename
	 */
	private File getAroCollectorFilesFromJar(String filename)
			throws IOException {
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
	 * Pushes the tcpdump executable to emulator and Starts it on emulator
	 * shell. Since this is run in a background thread, there is no UI
	 * interaction. If an error occurs, the error message is returned
	 * 
	 * @return Error message indicating error occurred starting data collector
	 *         or null on success
	 */
	private String startDataCollectorOnEmulator() {
		try {

			// Make sure another tcpdump is not already running
			stopTcpDump();

			// Copy tcpdump executable to emulator
			final String tcpdumpPath = rb
					.getString("Emulator.datacollectorpath") + "/" + TCPDUMP;
			SyncService mService = mAndroidDevice.getSyncService();
			File tcpdump = getAroCollectorFilesFromJar(TCPDUMP);
			SyncService.SyncResult tcpdumpushResult = mService.pushFile(
					tcpdump.getAbsolutePath(), tcpdumpPath,
					SyncService.getNullProgressMonitor());
			tcpdump.delete();
			if (tcpdumpushResult.getCode() == SyncService.RESULT_OK) {
				ShellOutputReceiver mShellOutput = new ShellOutputReceiver();
				mAndroidDevice.executeShellCommand("chmod 777 " + tcpdumpPath,
						mShellOutput);
			} else {
				return rb.getString("Error.withtcpdumppush");
			}

			// Copy keydb file to data collector
			File keydb = getAroCollectorFilesFromJar(KEYDB);
			SyncService.SyncResult keyDbpushResult = mService.pushFile(
					keydb.getAbsolutePath(),
					rb.getString("Emulator.datacollectorpath") + "/" + KEYDB,
					SyncService.getNullProgressMonitor());
			keydb.delete();
			if (keyDbpushResult.getCode() == SyncService.RESULT_OK) {

				// Start worker thread that will start data collector components
				new SwingWorker<Object, Object>() {

					@Override
					public Object doInBackground() throws IOException {

						// tcpdump command to be executed on emulator
						String strTcpDumpCommand = tcpdumpPath + " -w "
								+ traceFolderName + " not port 5555";
						ShellOutputReceiver tcpreceiver = new ShellOutputReceiver();

						// Video is recorded directly into local trace directory
						if (mARORecordTraceVideo) {
							mVideoCapture = new VideoCaptureThread(
									mAndroidDevice, new File(localTraceFolder,
											TraceData.VIDEO_MOV_FILE));
							mVideoCapture.start();
						} else {
							mVideoCapture = null;
						}

						// This shell command will block until tcpdump is
						// stopped
						tcpdumpStartTime = System.currentTimeMillis();
						mAndroidDevice.executeShellCommand(strTcpDumpCommand,
								tcpreceiver);

						return null;
					}

					/**
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						super.done();

						// Close progress dialog if open
						progress.dispose();

						// tcpdump has exited. Make sure everything else is
						// stopped
						synchronized (DatacollectorBridge.this) {
							setStatus(Status.STOPPING);

							try {

								// Check for exceptions
								try {
									get();
								} catch (InterruptedException e1) {
									MessageDialogFactory
											.showUnexpectedExceptionDialog(
													mAROAnalyzer, e1);
									logger.log(Level.SEVERE,
											"Error starting data collector", e1);
								} catch (ExecutionException e1) {
									MessageDialogFactory
											.showUnexpectedExceptionDialog(
													mAROAnalyzer, e1);
									logger.log(Level.SEVERE,
											"Error starting data collector", e1);
								}

								// Stop video if necessary
								if (mVideoCapture != null) {
									mVideoCapture.stopRecording();
									try {
										BufferedWriter videoTimeStampWriter = new BufferedWriter(
												new FileWriter(
														new File(
																localTraceFolder,
																TraceData.VIDEO_TIME_FILE)));
										try {
											// Writing a video time in file.
											videoTimeStampWriter
													.write(Double
															.toString(mVideoCapture
																	.getVideoStartTime()
																	.getTime() / 1000.0));
											if (tcpdumpStartTime > 0) {
												videoTimeStampWriter
														.write(" "
																+ Double.toString(tcpdumpStartTime / 1000.0));
											}
										} finally {
											videoTimeStampWriter.close();
										}
									} catch (IOException e) {
										MessageDialogFactory
												.showUnexpectedExceptionDialog(
														mAROAnalyzer, e);
										logger.log(
												Level.SEVERE,
												"Error writing video time file",
												e);
									} finally {
										mVideoCapture = null;
									}
								}
							} finally {
								dataTimeDuration = Math.round(tcpdumpEndTime
										- tcpdumpStartTime);
								setStatus(Status.STOPPED);
								startPullAROTraceFiles();
							}
						}
					}
				}.execute();

				// Check to see that tcpdump is started
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (DatacollectorBridge.this) {
					if (status != Status.STARTING) {
						return rb.getString("Error.datacollectostart");
					}
					checkSDCardStatus();
					setStatus(Status.STARTED);
				}
				JOptionPane.showMessageDialog(mAROAnalyzer,
						rb.getString("Message.datacollectorrunning"),
						rb.getString("aro.title.short"),
						JOptionPane.INFORMATION_MESSAGE);
				return null;
			} else {
				return rb.getString("Error.withemulatorkeybexecution");
			}
		} catch (IOException e) {
			String msg = rb.getString("Error.withemulatorioexecution");
			logger.log(Level.SEVERE, msg, e);
			return msg;
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
	 * This function connects to the server socket started by tcpdump and ends
	 * the tcpdump. The thread in which tcpdump is running will then be allowed
	 * to complete which will close the rest of the data collector.
	 */
	private void stopTcpDump() throws IOException {

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
			tcpdumpEndTime = System.currentTimeMillis();
			emulatorSocket.close();
		}
	}

	/**
	 * Class to get the output from the native emulator process and check it for
	 * errors.
	 */
	private static class ShellOutputReceiver extends MultiLineReceiver {
		private static final Pattern FAILURE = Pattern.compile("failed");
		private static final Pattern SDCARDFULL = Pattern
				.compile("No space left on device");
		private static final Pattern ERROR = Pattern.compile("error");

		private boolean shellError;
		private boolean sdcardFull;

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				logger.info(line);
				if (line.length() > 0) {
					Matcher sdcardfull = SDCARDFULL.matcher(line);
					if (sdcardfull.find()) {
						sdcardFull = true;
						return;
					}
					Matcher failureMatcher = FAILURE.matcher(line);
					if (failureMatcher.find() && !sdcardFull) {
						shellError = true;
					}
					Matcher errorMatcher = ERROR.matcher(line);
					if (errorMatcher.find()) {
						shellError = true;
					}
				}
			}
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

	}

	/**
	 * Class to get the output from the native process and display when
	 * determining emulator SD card available space.
	 */
	private static class ShellCommandCheckSDCardOutputReceiver extends
			MultiLineReceiver {

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
		public ShellCommandCheckSDCardOutputReceiver(IDevice device)
				throws IOException {
			device.executeShellCommand("df", this);
		}

		@Override
		public void processNewLines(String[] lines) {
			for (String oneLine : lines) {
				if ((oneLine.contains(rb.getString("Emulator.dfsdcardoutput")))) { // Checks
																					// the
																					// SDCard
																					// line
																					// from
																					// 'DF'
																					// command
																					// output

					// We find SD card is attached to emulator instance
					emualatorSDCardAttached = true;

					String strFileSize = null;
					String strValues[] = oneLine.split("\\s+");
					try {
						if (oneLine.contains(rb
								.getString("Emulator.availablespace"))) {
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
						logger.log(Level.SEVERE,
								"Error parsing SD card df output", e);
					}

					if (strFileSize != null) {
						try {

							// Checks if the available size in KB/MB/GB, we
							// convert the SD
							// card available space in KB before checking for
							// minimum 5 MB space requirement on sd card
							long iFileSizeInKB = -1;
							if (strFileSize.contains(K)) {
								final String iFileSizeKB[] = strFileSize
										.split(K);
								iFileSizeInKB = Long.valueOf(iFileSizeKB[0]);
							} else if (strFileSize.contains(M)) {
								final String iFileSizeMB[] = strFileSize
										.split(M);
								iFileSizeInKB = Long.valueOf(iFileSizeMB[0]);
								// Converting to KB
								iFileSizeInKB = iFileSizeInKB * 1024;
							} else if (strFileSize.contains(G)) {
								final String iFileSizeGB[] = strFileSize
										.split(G);
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
		// TODO looking for short shell commend to delete trace folder.
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.APPID_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.APPNAME_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.CPU_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.TIME_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.PCAP_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rm " + deviceTracePath + "/"
				+ TraceData.USER_EVENTS_FILE, shelloutPut);
		mAndroidDevice.executeShellCommand("rmdir " + deviceTracePath,
				shelloutPut);
	}

	/**
	 * Returns trace duration.
	 * 
	 * @return traceDuration
	 */
	private String getTraceDuration() {
		long appTimeR, appUpHours, appUpMinutes, appUpSeconds;
		dataTimeDuration = dataTimeDuration / 1000;
		appTimeR = dataTimeDuration % 3600;
		appUpHours = dataTimeDuration / 3600;
		appUpMinutes = (appTimeR / 60);
		appUpSeconds = appTimeR % 60;

		String formatedTraceDuration = (appUpHours < 10 ? "0" : "")
				+ appUpHours + ":" + (appUpMinutes < 10 ? "0" : "")
				+ appUpMinutes + ":" + (appUpSeconds < 10 ? "0" : "")
				+ appUpSeconds;
		return formatedTraceDuration;
	}

}
