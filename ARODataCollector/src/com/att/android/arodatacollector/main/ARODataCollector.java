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

package com.att.android.arodatacollector.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.activities.AROCollectorHomeActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Window;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

/**
 * Represents the ARO Data Collector application. The ARO Data Collector class
 * provides methods for storing, getting, and setting data that is used during
 * the life cycle of the application.
 * */

public class ARODataCollector extends Application {

	/** Log TAG string for ARO-Data Collector application class */
	private static final String TAG = "ARODataCollector";

	/** The application's internal data path on the device memory. */
	public static final String INTERNAL_DATA_PATH = "/data/data/com.att.android.arodatacollector/";

	/** The path for ffmpeg output file to be written. */
	private static String FFMPEG_OUTPUT_FILEPATH = "/data/ffmpegout.txt";

	/** The root directory of the ARO Data Collector Trace. */
	public static final String ARO_TRACE_ROOTDIR = "/sdcard/ARO/";

	/**
	 * The error dialog ID string for the Intent filer that is used in Main
	 * Activity. The error dialog appears when the ARO Data Collector
	 * unexpectedly stops.
	 */
	public static final String ERRODIALOGID = "ERRORDIALOGID";

	/**
	 * The name of native libs to be pushed to internal data path in sequence as
	 * per the resource id in R file
	 */
	private static final String mARODataCollectorNativeExe[] = { "key.db", "tcpdump" };

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean mIsProduction = true;

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean DEBUG = !mIsProduction;

	/** The name of the tcpdump Time file */
	private static final String TIME_FILE = "time";

	/** The name of video mp4 trace file name */
	private static final String videoMp4 = "video.mp4";

	/** The name of video starts and end time file name */
	private static final String outVideoTimeFileName = "video_time";

	/** The ARO alert menu notification id used with notification manager */
	private static final int NOTIFICATION_ID = 1;

	/** The error ID for an "tcpdump stop due to unexpected" error. */
	public static final int TCPDUMPSTOPPED = 1;

	/**
	 * The error ID for an "SD card full" error that occurs during mid-trace or
	 * before the start of a trace.
	 */
	public static final int SDCARDERROR = 2;

	/** The error ID for the network bearer change error. */
	public static final int BEARERCHANGEERROR = 3;

	/**
	 * The error ID for an "SD Card mount" error that occurs before the start of
	 * a trace.
	 */
	public static final int SDCARDMOUNTED = 4;

	/** The error ID for an "SD Card mount" error that occurs during mid-trace. */
	public static final int SDCARDMOUNTED_MIDTRACE = 5;

	/** The error ID for the wifi lost change error. */
	public static final int WIFI_LOST_ERROR = 6;

	/**
	 * The ARO-Data Collector application version String which is read from the
	 * manifest file
	 */
	private String mApplicationVersion;

	/**
	 * Boolean variable to hold value true and false to check if tcpdump is
	 * active and taking trace
	 */
	private boolean isARODataCollectorStopped;

	/**
	 * Boolean variable to keep true and false value for SD card mounted during
	 * mid trace
	 */
	private boolean mMidTraceMediaMounted;

	/** The ARO Data Collector trace folder name */
	private String mTraceFolderName;

	/**
	 * The network info class object to store current connected network which is
	 * stored right after trace cycle is started
	 */
	private NetworkInfo mAROCurrentNetworkType;

	/**
	 * The notification manager class to display ARO-Data Collector alert menu
	 * notification
	 */
	private NotificationManager mAROnotificationManager;

	/**
	 * String to keep trace folder name to be edited which has spaces and
	 * special characters in it
	 */
	private String traceFolderNamehasError;

	/** Notification to be used for alert menu */
	private Notification mAROnotification;

	/** Stores the value Video recording selected option */
	private boolean mVideoRecroding;

	/** OutputStaream for video_time file */
	private OutputStream mTraceVideoTimeStampFile;

	/** BufferedWriter for video_time file */
	private BufferedWriter mTraceVideoTimeStampWriter;

	/** tcpdump start time which is read from TIME file */
	private Double pcapStartTime;

	/** tcpdump stop time which is read from TIME file */
	private Double pcapStopTime;

	/** Boolean value to check if the tcpdump has been started */
	private boolean isTcpdumpStarted;

	/** Holds the boolean value for video capture in progress */
	private boolean isVideoCaptureRunning;

	/** Variable to hold value */
	private boolean isVideoCaptureFailed = false;

	/**
	 * Boolean value hold the value in the ARO Data Collector trace start is in
	 * progress this is first 15 sec after start collector is clicked
	 */
	private boolean isDataCollectorStartInProgress;

	/**
	 * Stores true and false value to notify change in network interface bearer
	 * since time of Data Collector launch
	 */
	private boolean mDataCollectorBearerChange = false;

	/** To store value of Select all option on the Task Killer main page */
	private boolean mSelectTaskKillerAllTask = false;

	/** The ffmpeg start time which is recorded as video capture start time */
	private Double mAROVideoCaptureStartTime = null;

	/** ARO Data Collector progress spinner Dialog */
	private Dialog aroProgressDialog;

	/** ARO Data Collector Wifi State */
	private String previousWifiState = "NONE";

	/** ARO Data Collector is wifi lost flag */
	private boolean wifiLost = false;

	/** ARO Data Collector is wifi lost flag */
	private boolean requestDataCollectorStop = false;

	/**
	 * Handles processing when an ARODataCollector object is created. Overrides
	 * the android.app.Application#onCreate method.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		if (DEBUG) {
			Log.d(TAG, "ARODataCollector.onCreate() called");
		}
		super.onCreate();
	}

	/**
	 * Initializes ARO Data Collector application variables, and copies the
	 * native libraries that are used by the ARO Data Collector.
	 * 
	 * @throws IOException
	 */
	public void initARODataCollector() {
		try {
			for (int resId = 0; resId < mARODataCollectorNativeExe.length; resId++) {
				PushDataCollectorExeToNative(resId, mARODataCollectorNativeExe[resId]);
			}
			PushDataCollectorFFmpegToNative();
		} catch (IOException e) {
			Log.e(TAG, "Exception in initARODataCollector", e);
		}
		mDataCollectorBearerChange = false;
		isVideoCaptureFailed = false;
		mSelectTaskKillerAllTask = false;
		mAROVideoCaptureStartTime = null;
	}

	/**
	 * Checks if the current application build is production
	 * 
	 * @return boolean value if production or debug build
	 */
	boolean isProduction() {

		return mIsProduction;
	}

	/**
	 * Initialize the Notification Manager object which ID and icon to be used
	 * to alert menu
	 */
	private void initARONotification() {
		if (mAROnotificationManager == null) {
			mAROnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mAROnotification = new Notification(R.drawable.icon,
					getString(R.string.aro_aleartnotification), System.currentTimeMillis());
		}
	}

	/**
	 * Starts the ARO Data Collector alert menu notification.
	 */
	public void triggerAROAlertNotification() {
		initARONotification();
		final CharSequence mTitle = getResources().getString(R.string.app_name);
		final CharSequence mMessage = getResources().getString(R.string.app_alertmenulauchtext);
		final Intent notificationIntent = new Intent(this, AROCollectorHomeActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				0);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mAROnotification.setLatestEventInfo(this, mTitle, mMessage, pendingIntent);
		mAROnotification.flags = Notification.FLAG_ONGOING_EVENT;
		mAROnotificationManager.notify(NOTIFICATION_ID, mAROnotification);
	}

	/**
	 * Clears the ARO Data Collector alert menu notification.
	 */
	public void cancleAROAlertNotification() {
		if (DEBUG) {
			Log.i(TAG, "cancleAROAlertNotification=" + mAROnotificationManager);
		}
		if (mAROnotificationManager != null) {
			mAROnotificationManager.cancel(NOTIFICATION_ID);
			mAROnotificationManager = null;
		} else {
			mAROnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mAROnotificationManager.cancel(NOTIFICATION_ID);
			mAROnotificationManager = null;
		}
	}

	/**
	 * Gets the ARO Data Collector application version from the manifest file.
	 * 
	 * @return A string that is the application version.
	 */
	public String getVersion() {
		if (mApplicationVersion == null) {
			try {
				final String packageName = getPackageName();
				if (packageName != null) {
					final PackageManager pm = getPackageManager();
					if (pm != null) {
						final PackageInfo info = pm.getPackageInfo(packageName, 0);
						if (info != null) {
							mApplicationVersion = info.versionName;
						}
					}
				}
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "exception in getVersion", e);
			}
			if (mApplicationVersion == null) {
				mApplicationVersion = "";
			} else if (isProduction()) {
				// if running in production, truncate the build number
				final String[] parts = mApplicationVersion.split("\\.");
				if (parts.length > 2) {
					final StringBuilder builder = new StringBuilder();
					builder.append(parts[0]);
					builder.append('.');
					builder.append(parts[1]);
					// skip the minor version as well if it's just a 0
					// (e.g. 1.0.0.x = 1.0, 1.0.1.x = 1.0.1)
					if (!parts[2].equals("0")) {
						builder.append('.');
						builder.append(parts[2]);
					}
					mApplicationVersion = builder.toString();
				}
			}
		}
		return mApplicationVersion;
	}

	/**
	 * Copies the key.db,ssldump and tcpdump executable to native Data Collector
	 * application path
	 * 
	 * @param resourceId
	 *            resource ID to be pushed in native data path
	 * @param exetuableName
	 *            name of the executable
	 * @throws IOException
	 */
	private void PushDataCollectorExeToNative(int resourceId, String exetuableName)
			throws IOException {
		InputStream myInput = null;
		OutputStream myOutput = null;
		try {
			myInput = this.getResources().openRawResource(R.raw.key + resourceId);
			final File file = new File(INTERNAL_DATA_PATH + exetuableName);
			if (file.exists())
				return;
			file.createNewFile();
			final String outFileName = INTERNAL_DATA_PATH + exetuableName;
			myOutput = new FileOutputStream(outFileName);
			final byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
		} finally {
			if (myOutput != null) {
				myOutput.close();
			}
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * Copy the ffmpeg to application data folder
	 * 
	 * @throws IOException
	 * 
	 */
	private void PushDataCollectorFFmpegToNative() throws IOException {
		InputStream myInput = null;
		OutputStream myOutput = null;
		try {
			myInput = this.getResources().openRawResource(R.raw.ffmpeg_splitaa);
			final File file = new File(INTERNAL_DATA_PATH + "ffmpeg");
			if (file.exists())
				return;
			file.createNewFile();
			final String outFileName = INTERNAL_DATA_PATH + "ffmpeg";
			myOutput = new FileOutputStream(outFileName);
			final int size = myInput.available();
			final byte[] buffer = new byte[size];
			int length;
			while ((length = myInput.read(buffer)) != -1) {
				myOutput.write(buffer, 0, length);
			}
			myInput.close();
			myInput = this.getResources().openRawResource(R.raw.ffmpeg_splitab);
			while ((length = myInput.read(buffer)) != -1) {
				myOutput.write(buffer, 0, length);
			}
			myInput.close();
			myInput = this.getResources().openRawResource(R.raw.ffmpeg_splitac);
			while ((length = myInput.read(buffer)) != -1) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
		} finally {
			if (myOutput != null) {
				myOutput.close();
			}
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * Writes the specified timestamp to the trace file using the specified
	 * output file writer.
	 * 
	 * @param outputfilewriter
	 *            The output file writer.
	 * @param timestamp
	 *            The time-stamp.
	 * 
	 * @throws IOException
	 */
	public void writeTimetoFile(BufferedWriter outputfilewriter, String timestamp)
			throws IOException {
		final String eol = System.getProperty("line.separator");
		outputfilewriter.write(timestamp + eol);
	}

	/**
	 * Reads the tcpdump TIME_FILE value to get the start and end time of the
	 * Pcap trace.
	 * 
	 * @throws IOException
	 */

	public void readPcapStartEndTime() throws IOException {
		final File file = new File(getTcpDumpTraceFolderName(), TIME_FILE);
		final BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line;
			// Ignore first line
			br.readLine();
			// Second line is pcap start time
			line = br.readLine();
			if (line != null) {
				pcapStartTime = Double.parseDouble(line);
			}
			// Ignore third line
			br.readLine();
			// forth line is pcap end time
			line = br.readLine();
			if (line != null) {
				pcapStopTime = Double.parseDouble(line);
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Writes the specified time stamp that is used to calculate the video start
	 * and stop time.
	 * 
	 * @param timestamp
	 *            A string value that is the timestamp.
	 * 
	 * @throws IOException
	 */

	public void writeVideoTraceTime(String timestamp) throws IOException {
		writeTimetoFile(mTraceVideoTimeStampWriter, timestamp);
	}

	/**
	 * Closes the video trace file and the file writer.
	 * 
	 * @throws IOException
	 */
	public void closeVideoTraceTimeFile() throws IOException {
		mTraceVideoTimeStampWriter.close();
		mTraceVideoTimeStampFile.close();
	}

	/**
	 * Initializes the time stamp of the video file that is used to record the
	 * trace video. The time stamp Is used to calculate the start and end time
	 * of the video.
	 * 
	 * @throws FileNotFoundException
	 */

	public void initVideoTraceTime() throws FileNotFoundException {
		mTraceVideoTimeStampFile = new FileOutputStream(getTcpDumpTraceFolderName()
				+ outVideoTimeFileName);
		mTraceVideoTimeStampWriter = new BufferedWriter(new OutputStreamWriter(
				mTraceVideoTimeStampFile));
	}

	/**
	 * Reads the start time of the trace video from the ffmpeg output file.
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public void readffmpegStartTimefromFile() throws NumberFormatException, IOException {

		String ffmpegstartTime = null;
		boolean exitFlag = false;
		FileReader ffmpegOutFile = null;
		BufferedReader bufferReader = null;
		try {
			ffmpegOutFile = new FileReader(FFMPEG_OUTPUT_FILEPATH);
			bufferReader = new BufferedReader(ffmpegOutFile);
			String ffmpegStartTimeLine;
			while ((ffmpegStartTimeLine = bufferReader.readLine()) != null || exitFlag) {
				final int indexfound = ffmpegStartTimeLine.indexOf("start:");
				final int exitIndex = ffmpegStartTimeLine.indexOf("frame=");
				if (indexfound > -1) {
					final String[] values = ffmpegStartTimeLine.split("start:");
					ffmpegstartTime = values[1];
					final int commandIndex = ffmpegstartTime.indexOf(',');
					ffmpegstartTime = ffmpegstartTime.substring(0, commandIndex);
					mAROVideoCaptureStartTime = (Double.parseDouble(ffmpegstartTime));
					exitFlag = true;
					break;
				}
				if (exitIndex > -1) {
					mAROVideoCaptureStartTime = 0.0;
					exitFlag = true;
					break;
				}
			}
		} finally {
			// Failed to parse start time from FFmpeg output file
			if (mAROVideoCaptureStartTime == null) {
				mAROVideoCaptureStartTime = 0.0;
			}
			bufferReader.close();
			ffmpegOutFile.close();
		}
	}

	/**
	 * Shows the ARO Data collector progress bar for the specified UI
	 * application context.
	 * 
	 * @param appuicontext
	 *            The UI application context.
	 */
	public void showProgressDialog(Context appuicontext) {
		aroProgressDialog = new Dialog(appuicontext, android.R.style.Theme_Translucent);
		aroProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		aroProgressDialog.setContentView(R.layout.aro_loading_progress);
		aroProgressDialog.setCancelable(false);
		aroProgressDialog.show();
	}

	/**
	 * Hides the ARO Data collector progress bar.
	 */
	public void hideProgressDialog() {
		if (aroProgressDialog != null && aroProgressDialog.isShowing()) {
			aroProgressDialog.cancel();
			aroProgressDialog = null;
		}
	}

	/**
	 * Returns the PCAP start time from TIME file
	 * 
	 * @return pcapStartTime
	 */
	private double getTraceStartTime() {
		return pcapStartTime;
	}

	/**
	 * Returns the PCAP stop time from TIME time
	 * 
	 * @return pcapStopTime
	 */
	private double getTraceEndTime() {
		return pcapStopTime;
	}

	/**
	 * Returns the start time of the FFMPEG video.
	 * 
	 * @return A double that is the start time of the FFMPEG video, in seconds.
	 */
	public double getAROVideoCaptureStartTime() {
		return mAROVideoCaptureStartTime;
	}

	/**
	 * Gets the path name of the ARO Data Collector tcpdump trace folder.
	 * 
	 * @return A string that is the trace folder path name.
	 */
	public String getTcpDumpTraceFolderName() {
		return (ARO_TRACE_ROOTDIR + mTraceFolderName + "/");
	}

	/**
	 * Sets the trace folder name for the ARO Data Collector to the specified
	 * string.
	 * 
	 * @param traceFolderName
	 *            A string value that is the trace folder name.
	 */
	public void setTcpDumpTraceFolderName(String traceFolderName) {
		mTraceFolderName = traceFolderName;

	}

	/**
	 * Gets the ARO Data Collector trace folder name.
	 * 
	 * @return A string that is the ARO Data Collector trace folder name.
	 */
	public String getDumpTraceFolderName() {
		return (mTraceFolderName);
	}

	/**
	 * Sets a value that enables the Video Recording collection option for an
	 * ARO Data Collector trace.
	 * 
	 * @param videoRecording
	 *            A boolean value that is "true" to enable video recording for a
	 *            trace, and "false" otherwise.
	 */
	public void setCollectVideoOption(boolean videoRecording) {
		mVideoRecroding = videoRecording;

	}

	/**
	 * Gets the type of the currently connected network.
	 * 
	 * @return A boolean value that indicates whether or not the Video Recording
	 *         collection option is enabled.
	 */
	public boolean getCollectVideoOption() {
		return (mVideoRecroding);
	}

	/**
	 * Gets the duration of the ARO Data Collector trace.
	 * 
	 * @return A long that is the duration of the trace, in seconds.
	 */
	public long getAppUpTimeinSeconds() {

		return Math.round(getTraceEndTime() - getTraceStartTime());
	}

	/**
	 * Sets the network type of the currently connected network to the specified
	 * value. This value is used to determine changes in the data bearer during
	 * a trace cycle.
	 * 
	 * @param mNetworkType
	 *            A Networkinfo object that indicates the network type.
	 */
	public void setCurrentNetworkType(NetworkInfo mNetworkType) {
		mAROCurrentNetworkType = mNetworkType;
	}

	/**
	 * Gets the types of the currently connected network.
	 * 
	 * @return An int value that indicates the network type. A value of 1
	 *         indicates the current network type is wifi, and a value of 0
	 *         indicates that it is any other mobile network.
	 */
	public int getCurrentNetworkType() {
		// If current network is WIFI
		if (mAROCurrentNetworkType.getType() == 1)
			return 1;
		else
			return 0;
	}

	/**
	 * Sets the name of the trace folder which contains errors.
	 * 
	 * @param tracefoldername
	 *            A string that is the error trace folder name.
	 */
	public void setErrorTraceFoldername(String tracefoldername) {
		traceFolderNamehasError = tracefoldername;
	}

	/**
	 * Gets the ARO Data Collector trace folder name when it contains errors.
	 * 
	 * @return A string that is the trace folder name that contains errors.
	 */
	public String getErrorTraceFoldername() {

		return traceFolderNamehasError;
	}

	/**
	 * Sets a flag that indicates whether the tcpdump is started.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if the tcpdump is started, and
	 *            "false" otherwise.
	 */
	public void setTcpDumpStartFlag(boolean flag) {
		isTcpdumpStarted = flag;
	}

	/**
	 * Sets a flag that indicates whether the ARO Data Collector is in progress.
	 * 
	 * @param flag
	 *            A boolean value that is true if the ARO Data Collector is in
	 *            progress, and false if it is not.
	 */
	public void setDataCollectorInProgressFlag(boolean flag) {
		isDataCollectorStartInProgress = flag;
	}

	/**
	 * Gets the flag that indicates if the ARO Data Collector is in progress.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector is in progress.
	 */
	public boolean getDataCollectorInProgressFlag() {
		return isDataCollectorStartInProgress;
	}

	/**
	 * Gets the flag that indicates if the tcpdump has started.
	 * 
	 * @return A boolean value that indicates whether or not the tcpdump has
	 *         started.
	 */
	public boolean getTcpDumpStartFlag() {
		return isTcpdumpStarted;
	}

	/**
	 * Sets the flag that indicates whether the video capture is running during
	 * the trace.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if video capture is running
	 *            during the trace, and "false" if it is not.
	 */
	public void setARODataCollectorStopFlag(boolean flag) {
		isARODataCollectorStopped = flag;
	}

	/**
	 * Gets the flag that indicates whether the ARO Data Collector is stopped.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector is stopped.
	 */
	public boolean getARODataCollectorStopFlag() {
		return isARODataCollectorStopped;
	}

	/**
	 * Gets the flag that indicates whether the SD card is mounted during
	 * mid-trace.
	 * 
	 * @return A boolean value that indicates whether or not the SD card is
	 *         mounted during mid-trace.
	 */
	public boolean getAROMediaMountedMidTraceFlag() {
		return mMidTraceMediaMounted;
	}

	/**
	 * Sets a flag that indicates whether the SD card is mounted during
	 * mid-trace.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if the SD card is mounted
	 *            during mid-trace, and "false" if it is not.
	 */
	public void setMediaMountedMidAROTrace(boolean flag) {
		mMidTraceMediaMounted = flag;
	}

	/**
	 * Sets the flag that indicates whether the video capture is running during
	 * the trace.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if video capture is running
	 *            during the trace, and "false" if it is not.
	 */
	public void setAROVideoCaptureRunningFlag(boolean flag) {
		isVideoCaptureRunning = flag;
	}

	/**
	 * Gets the flag that indicates if the video capture is running during the
	 * trace cycle.
	 * 
	 * @return A boolean value that indicates whether or not the video capture
	 *         is running during the trace.
	 */
	public boolean getAROVideoCaptureRunningFlag() {
		return isVideoCaptureRunning;
	}

	/**
	 * Gets a flag that indicates if the video capture failed.
	 * 
	 * @return A boolean value that indicates whether or not the video capture
	 *         failed.
	 */
	public boolean getVideoCaptureFailed() {
		return isVideoCaptureFailed;
	}

	/**
	 * Sets a flag that indicates whether the video capture has failed.
	 * 
	 * @param flag
	 *            boolean value that is "true" if the video capture has failed,
	 *            and "false" if it has not.
	 */
	public void setVideoCaptureFailed(boolean flag) {
		isVideoCaptureFailed = flag;
	}

	/**
	 * Sets the flag that indicates if the ARO Data Collector data bearer has
	 * changed.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if the ARO Data Collector data
	 *            bearer has changed, and is "false" otherwise.
	 */
	public void setDataCollectorBearerChange(boolean flag) {
		mDataCollectorBearerChange = flag;
	}

	/**
	 * Gets a flag that indicates if the ARO Data Collector data bearer has
	 * changed.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector data bearer changed during the trace cycle.
	 */
	public boolean getDataCollectorBearerChange() {
		return mDataCollectorBearerChange;
	}

	/**
	 * Sets a flag indicating that all listed tasks in the ARO Data Collector
	 * task killer are selected.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if all listed tasks are
	 *            selected, and "false" otherwise.
	 */
	public void setectTaskKillerAllTasks(boolean flag) {
		mSelectTaskKillerAllTask = flag;
	}

	/**
	 * Gets a value that indicates if all the listed tasks in the ARO Data
	 * Collector task killer have been selected.
	 * 
	 * @return A boolean value that indicates whether or not all of the tasks in
	 *         the ARO Data Collector task killer are selected.
	 */
	public boolean getTaskKillerAllTasksSelected() {
		return mSelectTaskKillerAllTask;
	}

	/**
	 * Determines whether the video.mp4 file is created in the current trace
	 * folder or not.
	 * 
	 * @return <CODE>true</CODE> if the video file exists in the current trace
	 *         folder and bigger than 0 bytes. <CODE>false</CODE> otherwise.
	 */
	public boolean isVideoFileExisting() {
		String videoAbsolutePath = ARODataCollector.ARO_TRACE_ROOTDIR
				+ this.getDumpTraceFolderName() + "/" + videoMp4;

		if (DEBUG) {
			Log.d(TAG, "isVideoFileExisting()--videoPath: " + videoAbsolutePath);
		}
		File videoFile = new File(videoAbsolutePath);
		if (videoFile.isFile() && videoFile.length() > 0) {
			if (DEBUG) {
				Log.d(TAG, "isVideoFileExisting(): " + "returning true" + "-videoFile.isFile():"
						+ videoFile.isFile() + "-videoFile.length():" + videoFile.length());
			}
			return true;
		} else {
			if (DEBUG) {
				Log.d(TAG, "isVideoFileExisting(): " + "returning false" + "-videoFile.isFile():"
						+ videoFile.isFile() + "-videoFile.length():" + videoFile.length());
			}
			return false;
		}
	}

	/**
	 * Sets a flag that indicates the current wifi state.
	 * 
	 * @param currentWifiState
	 *            A NetworkInfo state value for wifi currently in the trace.
	 */
	public void setPreviousWifiState(String currentWifiState) {
		this.previousWifiState = currentWifiState;
	}

	/**
	 * Gets a value that indicates the previous wifi state. It is the current
	 * state if state has not changed.
	 * 
	 * @return A NetworkInfo state value for wifi.
	 */
	public String getPreviousWifiState() {
		return previousWifiState;
	}

	/**
	 * Sets a flag that indicates whether wifi connectivity is lost.
	 * 
	 * @param isWifiLost
	 *            A boolean value to indicate whether wifi connectivity is lost
	 *            during the trace.
	 */
	public void setWifiLost(boolean isWifiLost) {
		wifiLost = isWifiLost;
	}

	/**
	 * Determines whether wifi connectivity is lost during the trace
	 * 
	 * @return <CODE>true</CODE> if wifi is currently not connected and was
	 *         connected previously. <CODE>false</CODE> otherwise.
	 */
	public boolean isWifiLost() {
		return wifiLost;
	}

	/**
	 * Sets a flag that indicates whether a request to stop data collection has
	 * been issued.
	 * 
	 * @param requestDataCollectorStop
	 *            A flag to set whether a request to stop data collection has
	 *            been made.
	 */
	public void setRequestDataCollectorStop(boolean requestDataCollectorStop) {
		this.requestDataCollectorStop = requestDataCollectorStop;
	}

	/**
	 * Determines whether a request to stop data Collection has been issued.
	 * 
	 * @return <CODE>true</CODE> if a request to stop data collection has been
	 *         made. <CODE>false</CODE> otherwise.
	 */
	public boolean isRequestDataCollectorStop() {
		return requestDataCollectorStop;
	}
}
