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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.activities.AROCollectorHomeActivity;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;
import com.flurry.android.FlurryAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

	private static final String USER_INITIAL_SCREEN_TIMEOUT = "userInitialScreenTimeout";
	public static final int USER_SCREEN_TIMEOUT_UNASSIGNED = -10;

	/** Log TAG string for ARO-Data Collector application class */
	private static final String TAG = "ARODataCollector";

	/** The application's internal data path on the device memory. */
	public static final String INTERNAL_DATA_PATH = "/data/data/com.att.android.arodatacollector/";

	/** The path for ffmpeg output file to be written. */
	public static final String FFMPEG_OUTPUT_FILEPATH = INTERNAL_DATA_PATH + "ffmpegout.txt";

	/** The root directory of the ARO Data Collector Trace. */
	public static final String ARO_TRACE_ROOTDIR = "/sdcard/ARO/";

	/** The relative file path for flurry holding api key file */
	public static final String FLURRY_API_KEY_REL_PATH = "flurry/flurry_api_key.properties";
	
	/** Key for Flurry Application API Key */
	public static final String FLURRY_API_KEY_NAME = "FLURRY_API_KEY";

	/**
	 * The error dialog ID string for the Intent filer that is used in Main
	 * Activity. The error dialog appears when the ARO Data Collector
	 * unexpectedly stops.
	 */
	public static final String ERRODIALOGID = "ERRORDIALOGID";
	
	public static final String PROCESS_CPU_MON = "processcpumon.sh";

	/**
	 * The name of native libs to be pushed to internal data path in sequence as
	 * per the resource id in R file
	 */
	private static final String mARODataCollectorNativeExe[] = { "key.db", PROCESS_CPU_MON, "tcpdump" , "tcpdump_pie" };

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean mIsProduction = false;

	/** The name of the tcpdump Time file */
	private static final String TIME_FILE = "time";

	/** The name of video mp4 trace file name */
	private static final String videoMp4 = "video.mp4";

	/** The name of video starts and end time file name */
	private static final String outVideoTimeFileName = "video_time";

	/** The name of shared preference name for local persistence  */
	public static final String PREFS = "AROPrefs";
	
	/** The ARO alert menu notification id used with notification manager */
	public static final int NOTIFICATION_ID = 1;

	/** The error ID for an "tcpdump stop due to unexpected" error. */
	public static final int TCPDUMPSTOPPED = 1;

	/**
	 * The error ID for an "SD card full" error that occurs during mid-trace or
	 * before the start of a trace.
	 */
	public static final int SDCARDERROR = 2;

	/**
	 * The error ID for an "SD Card mount" error that occurs before the start of
	 * a trace.
	 */
	public static final int SDCARDMOUNTED = 4;

	/** The error ID for an "SD Card mount" error that occurs during mid-trace. */
	public static final int SDCARDMOUNTED_MIDTRACE = 5;
	
	/** The error ID for an "Airplane Mode Enabled" error that occurs during mid-trace. */
	public static final int AIRPLANEMODEENABLED_MIDTRACE = 6;

	/**
	 * The ARO-Data Collector application version String which is read from the
	 * manifest file
	 */
	private String mApplicationVersion;

	/**
	 * Flurry Analytics API Key.  This is associated to the Application specified
	 * at https://dev.flurry.com under a specified login and Company AT&T.  This is the
	 * default value meant for ATT Developers.  Internal ATT users will override this value
	 * via a file input. 
	 */
	public String app_flurry_api_key = "5WYSN3ZBDP476WD6VHDY";
	
	/** Event properties logged during ARO application for use by Flurry Analytics. */
	public Map<String, String> flurryVideoTaken = new HashMap<String, String>();

	/** Event properties logged during ARO application for use by Flurry Analytics. Application context scope. */
	public Map<String, String> flurryError = new HashMap<String, String>();
	
	/** The ARO Data Collector trace folder name */
	private String mTraceFolderName;

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
	public Notification mAROnotification;

	/** Stores the value Video recording selected option */
	private boolean mVideoRecroding;

	private int TraceStopDuration;
	
	/** Stores the value ARO Collector launch status from Analyzer */
	private boolean mDCLaunchfromAnalyzer;
	
	/** Stores the value ARO RQM Collector launch status from Analyzer */
	private boolean mDCRQMLaunchfromAnalyzer;
	
	/** OutputStaream for video_time file */
	private OutputStream mTraceVideoTimeStampFile;

	/** BufferedWriter for video_time file */
	private BufferedWriter mTraceVideoTimeStampWriter;

	/** tcpdump start time which is read from TIME file */
	private Double pcapStartTime;

	/** tcpdump stop time which is read from TIME file */
	private Double pcapStopTime;

	/** Variable to hold value */
	private boolean isVideoCaptureFailed = false;

	/** Variable to hold value */
	private boolean isUSBVideoCaptureON = false;
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

	/** ARO Data Collector is wifi lost flag */
	private boolean requestDataCollectorStop = false;
		
	/** Stores the value to find if launch of collector from Analyzer */
	private boolean mDataCollectorStopDisable = false;
	
	/** Indicates if the collector launch from the analyzer is in progress, 
	 * (ie. waiting on the legal page or for the user to start it).
	 * This will be set to false either when the launch timeout expired, or
	 * the collector has started*/
	private static boolean isAnalyzerLaunchInProgress = false;
	
	
	
	/**
	 * Handles processing when an ARODataCollector object is created. Overrides
	 * the android.app.Application#onCreate method.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		AROLogger.d(TAG, "ARODataCollector.onCreate() called");
		super.onCreate();
		registerAnalyzerTimeoutReceiver();
	}
	
	/**
	 * Initializes ARO Data Collector application variables, and copies the
	 * native libraries that are used by the ARO Data Collector.
	 * 
	 * @throws IOException
	 */
	public void initARODataCollector() {
		boolean pieMode = android.os.Build.VERSION.RELEASE.equals("5.0");
		try {
			for (int resId = 0; resId < mARODataCollectorNativeExe.length; resId++) {
				if ((pieMode && resId == 2 ) || (!pieMode && resId == 3 )){
					continue;
				}
				PushDataCollectorExeToNative(resId, mARODataCollectorNativeExe[resId]);
			}
			PushDataCollectorFFmpegToNative();
				
		} catch (IOException e) {
			AROLogger.e(TAG, "Exception in initARODataCollector", e);
		}
		mDataCollectorBearerChange = false;
		isVideoCaptureFailed = false;
		mSelectTaskKillerAllTask = false;
		mAROVideoCaptureStartTime = null;
	}

	private HashMap<String, Process> pids = new HashMap<String, Process>();
	
	public void addPid(String key, Process pid){
		pids.put(key, pid);
	}
	public Process getPid(String key){
		return pids.get(key);
	}
	
	public Process popPid(String key) {
		return pids.remove(key)	;
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
			
			getARONotification();
		}
	}

	/**
	 * Starts the ARO Data Collector alert menu notification.
	 */
	public void triggerAROAlertNotification() {
		initARONotification();
		
		mAROnotificationManager.notify(NOTIFICATION_ID, mAROnotification);
	}
	
	public Notification getARONotification(){
		if (mAROnotification == null){
			mAROnotification = new Notification(R.drawable.icon,
					getString(R.string.aro_aleartnotification), System.currentTimeMillis());
			
			final CharSequence mTitle = getResources().getString(R.string.app_name);
			final CharSequence mMessage = getResources().getString(R.string.app_alertmenulauchtext);
			final Intent notificationIntent = new Intent(this, AROCollectorHomeActivity.class);
			final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
					0);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mAROnotification.setLatestEventInfo(this, mTitle, mMessage, pendingIntent);
			mAROnotification.flags = Notification.FLAG_ONGOING_EVENT;
		}
		
		return mAROnotification;
	}

	/**
	 * Clears the ARO Data Collector alert menu notification.
	 */
	public void cancleAROAlertNotification() {
		if (AROLogger.logDebug) {
			AROLogger.d(TAG, "cancleAROAlertNotification=" + mAROnotificationManager);
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
				AROLogger.e(TAG, "exception in getVersion", e);
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
	private void PushDataCollectorExeToNative(int resourceId, String exetuableName) throws IOException {
		
		InputStream myInput = null;
		OutputStream myOutput = null;
		try {
			myInput = this.getResources().openRawResource(R.raw.key + resourceId);
			final File file = new File(INTERNAL_DATA_PATH + exetuableName);
			if (file.exists()) {
				//this is needed to fix the issue of the new tcpdump is not picked up 
				//when the new aro version is installed on top of the old one.
				file.delete();
			}
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
			if (bufferReader != null) {
				bufferReader.close();
			}

			if (ffmpegOutFile != null) {
				ffmpegOutFile.close();
			}
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
		return (ARO_TRACE_ROOTDIR + getDumpTraceFolderName() + "/");
	}

	/**
	 * Sets the trace folder name for the ARO Data Collector to the specified
	 * string.
	 * 
	 * @param traceFolderName A string value that is the trace folder name.
	 */
	public void setTcpDumpTraceFolderName(String traceFolderName) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("TraceFolderName", traceFolderName);
		editor.commit();
	}

	/**
	 * Gets the ARO Data Collector trace folder name.
	 * 
	 * @return A string that is the ARO Data Collector trace folder name.
	 */
	public String getDumpTraceFolderName() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mTraceFolderName = prefs.getString("TraceFolderName", null);
		return (mTraceFolderName);
	}

	/**
	 * setTraceStopDuration
	 * @param duration
	 */
	public void setTraceStopDuration(int duration) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("TraceDuration", duration);
		editor.commit();
	}
	
	/**
	 * getTraceStopDuration
	 * @return
	 */
	public int getTraceStopDuration() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		TraceStopDuration = prefs.getInt("TraceDuration", 0);
		return TraceStopDuration;
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("VideoRecordFlag", videoRecording);
		editor.commit();
	}

	/**
	 * Gets the type of the currently connected network.
	 * 
	 * @return A boolean value that indicates whether or not the Video Recording
	 *         collection option is enabled.
	 */
	public boolean getCollectVideoOption() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mVideoRecroding = prefs.getBoolean("VideoRecordFlag", false);
		return (mVideoRecroding);
	}

	
	/**
	 * Sets the flag is ARO Data Collector was launch from ARO Analyzer.
	 *  
	 */
	public void setCollectorLaunchfromAnalyzer(boolean dcfromAnalyzer) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("DCfromAnalyzer", dcfromAnalyzer);
		editor.commit();
	}
	
	/**
	 * Gets the status if ARO Data Collector was started from ARO Analyzer. 
	 * 
	 * @return A boolean that is status if collector was launched from analyzer.
	 */
	public boolean isCollectorLaunchfromAnalyzer() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mDCLaunchfromAnalyzer = prefs.getBoolean("DCfromAnalyzer", false);
		return (mDCLaunchfromAnalyzer);
	}
	
	
	/**
	 * Sets the flag is ARO Data Collector was launch from ARO Analyzer.
	 *  
	 */
	public void setRQMCollectorLaunchfromAnalyzer(boolean dcRQMfromAnalyzer) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("DCRQMfromAnalyzer", dcRQMfromAnalyzer);
		editor.commit();
	}
	
	/**
	 * Gets the status if ARO Data Collector was started from ARO Analyzer. 
	 * 
	 * @return A boolean that is status if collector was launched from analyzer.
	 */
	public boolean isRQMCollectorLaunchfromAnalyzer() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mDCRQMLaunchfromAnalyzer = prefs.getBoolean("DCRQMfromAnalyzer", false);
		return (mDCRQMLaunchfromAnalyzer);
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isTcpdumpStarted", flag);
		editor.commit();
	}

	/**
	 * Sets a flag that indicates whether the ARO Data Collector is in progress.
	 * 
	 * @param flag
	 *            A boolean value that is true if the ARO Data Collector is in
	 *            progress, and false if it is not.
	 */
	public void setDataCollectorInProgressFlag(boolean flag) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isDataCollectorStartInProgress", flag);
		editor.commit();
	}

	/**
	 * Gets the flag that indicates if the ARO Data Collector is in progress.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector is in progress.
	 */
	public boolean getDataCollectorInProgressFlag() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean isDataCollectorStartInProgress = prefs.getBoolean("isDataCollectorStartInProgress", false);
		return isDataCollectorStartInProgress;
	}
	
	/**
	 * setUserInitialScreenTimeout
	 * @param timeoutVal
	 */
	public void setUserInitialScreenTimeout(int timeoutVal){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(USER_INITIAL_SCREEN_TIMEOUT, timeoutVal);
		editor.commit();
	}
	/**
	 * getUserInitialScreenTimeout
	 * @return screenTimeoutVal 
	 */
	public int getUserInitialScreenTimeout(){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final int screenTimeoutVal = prefs.getInt(USER_INITIAL_SCREEN_TIMEOUT, -1);
		return screenTimeoutVal;
	}

	
	/**
	 * setDeviceScreenHeight
	 * @param screenheight
	 */
	public void setDeviceScreenHeight(int screenheight){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("ScreenHeight", screenheight);
		editor.commit();
	}
	/**
	 * getDeviceScreenHeight
	 * @return screenHeight 
	 */
	public int getDeviceScreenHeight(){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final int screenHeight = prefs.getInt("ScreenHeight", -1);
		return screenHeight;
	}
	
	/**
	 * setDeviceScreenWidth
	 * @param screenwidth
	 */
	public void setDeviceScreenWidth(int screenwidth){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("ScreenWidth", screenwidth);
		editor.commit();
	}
	/**
	 * getDeviceScreenWidth
	 * @return screenWidth 
	 */
	public int getDeviceScreenWidth(){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final int screenWidth = prefs.getInt("ScreenWidth", -1);
		return screenWidth;
	}
	
	
	/**
	 * Gets the flag that indicates if the tcpdump has started.
	 * 
	 * @return A boolean value that indicates whether or not the tcpdump has
	 *         started.
	 */
	public boolean getTcpDumpStartFlag() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean isTcpdumpStarted = prefs.getBoolean("isTcpdumpStarted", false);
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isARODataCollectorStopped", flag);
		editor.commit();
	}

	/**
	 * Gets the flag that indicates whether the ARO Data Collector is stopped.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector is stopped.
	 */
	public boolean getARODataCollectorStopFlag() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean isARODataCollectorStopped = prefs.getBoolean("isARODataCollectorStopped", false);
		return isARODataCollectorStopped;
	}

	/**
	 * Gets the elapsedTimeStartTime which is the start time of the timer on the home page.
	 * 
	 * @return A long integer that represents when the trace started for the home page timer.
	 */
	public long getElapsedTimeStartTime() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final long elapsedTimeStartTime = prefs.getLong("elapsedTimeStartTime", 0);
		return elapsedTimeStartTime;
	}
		
	/**
	 * Sets elapsedTimeStartTime which is the start time of the timer on the home page.
	 * 
	 * @param paramElapsedTimeStartTime
	 *            A long integer that represents when the trace started for the home page timer.
	 */
	public void setElapsedTimeStartTime(long paramElapsedTimeStartTime) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("elapsedTimeStartTime", paramElapsedTimeStartTime);
		editor.commit();
	}
	
	
	/**
	 * Gets the flag that indicates whether the SD card is mounted during
	 * mid-trace.
	 * 
	 * @return A boolean value that indicates whether or not the SD card is
	 *         mounted during mid-trace.
	 */
	public boolean getAROMediaMountedMidTraceFlag() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean mMidTraceMediaMounted = prefs.getBoolean("mMidTraceMediaMounted", false);
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("mMidTraceMediaMounted", flag);
		editor.commit();
	}
	
	/**
	 * Gets the flag that indicates if Airplane mode was enabled
	 * mid-trace.
	 * 
	 * @return A boolean value that indicates if Airplane mode was enabled mid-trace.
	 */
	public boolean getAirplaneModeEnabledMidAROTrace() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean mMidTraceMediaMounted = prefs.getBoolean("mMidTraceAirplaneModeEnabled", false);
		return mMidTraceMediaMounted;
	}
	
	/**
	 * Sets a flag that indicates Airplane was enabled
	 * mid-trace.
	 * 
	 * @param flag
	 *            A boolean value that is "true" if Airplane Mode was enabled
	 *            during mid-trace, and "false" if it is not.
	 */
	public void setAirplaneModeEnabledMidAROTrace(boolean flag) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("mMidTraceAirplaneModeEnabled", flag);
		editor.commit();
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isVideoCaptureRunning", flag);
		editor.commit();
	}

	/**
	 * Gets the flag that indicates if the video capture is running during the
	 * trace cycle.
	 * 
	 * @return A boolean value that indicates whether or not the video capture
	 *         is running during the trace.
	 */
	public boolean getAROVideoCaptureRunningFlag() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		final boolean isVideoCaptureRunning = prefs.getBoolean("isVideoCaptureRunning", false);
		return isVideoCaptureRunning;
		
	}

	/**
	 * Gets a flag that indicates if the video capture failed.
	 * 
	 * @return A boolean value that indicates whether or not the video capture
	 *         failed.
	 */
	public boolean getVideoCaptureFailed() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		isVideoCaptureFailed = prefs.getBoolean("isVideoCaptureFailed", false);
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isVideoCaptureFailed", flag);
		editor.commit();
	}

	/**
	 * Sets a flag that indicated whether the USB Video Capture has started 
	 * @param flag
	 */
	public void setUSBVideoCaptureON(boolean flag){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("isUSBVideoCaptureON", flag);
		editor.commit();
	}
	
	/**
	 * Gets a flag that indicated whether the USB Video Capture has started 
	 * isUSBVideoCaptureON
	 * @return
	 */
	public boolean isUSBVideoCaptureON(){
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		isUSBVideoCaptureON = prefs.getBoolean("isUSBVideoCaptureON", false);
		return isUSBVideoCaptureON; 
	}
	
	/**
	 * Sets a flag that indicate if Data Collector stop button is enabled 
	 * @param flag
	 */
	public void setDataCollectorStopEnable(boolean flag) {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("mDataCollectorStopDisable", flag);
		editor.commit();
	}

	/**
	 * 
	 * Gets a flag that indicate if Data Collector stop button is enabled 
	 * @return
	 */
	public boolean getDataCollectorStopEnable() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mDataCollectorStopDisable = prefs.getBoolean("mDataCollectorStopDisable", false);
		return mDataCollectorStopDisable;
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
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("mDataCollectorBearerChange", flag);
		editor.commit();
	}

	/**
	 * Gets a flag that indicates if the ARO Data Collector data bearer has
	 * changed.
	 * 
	 * @return A boolean value that indicates whether or not the ARO Data
	 *         Collector data bearer changed during the trace cycle.
	 */
	public boolean getDataCollectorBearerChange() {
		final SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		mDataCollectorBearerChange = prefs.getBoolean("mDataCollectorBearerChange", false);
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
	 * Returns a value that indicates whether or not the 
	 * video.mp4 file is created in the current trace folder.
	 * 
	 * @return A boolean value that is true if the video.mp4 file exists in the 
	 * 		   current trace folder and is larger than 0 bytes. Otherwise, the value is false.
	 */
	public boolean isVideoFileExisting() {
		final String videoAbsolutePath = ARODataCollector.ARO_TRACE_ROOTDIR
				+ this.getDumpTraceFolderName() + "/" + videoMp4;

		if (AROLogger.logDebug) {
			AROLogger.d(TAG, "isVideoFileExisting()--videoPath: " + videoAbsolutePath);
		}
		final File videoFile = new File(videoAbsolutePath);
		if (videoFile.isFile() && videoFile.length() > 0) {
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "isVideoFileExisting(): " + "returning true" + "-videoFile.isFile():"
						+ videoFile.isFile() + "-videoFile.length():" + videoFile.length());
			}
			return true;
		} else {
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "isVideoFileExisting(): " + "returning false" + "-videoFile.isFile():"
						+ videoFile.isFile() + "-videoFile.length():" + videoFile.length());
			}
			return false;
		}
	}
	/**
	 * Sets a flag that indicates whether a request to stop data collection has
	 * been issued.
	 * 
	 * @param requestDataCollectorStop
	 *           A boolean value that is true if a request 
	 *           to stop data collection has been made, and is false otherwise.
	 */
	public void setRequestDataCollectorStop(boolean requestDataCollectorStop) {
		this.requestDataCollectorStop = requestDataCollectorStop;
	}

	/**
	 * Returns a value that indicates whether a request to stop data collection has been issued. 
	 * 
	 * @return A boolean value that is true if a request to stop data collection 
	 * 		   has been made, and is false otherwise.
	 */
	public boolean isRequestDataCollectorStop() {
		return requestDataCollectorStop;
	}
		
	/**
	 * Writing to a Flurry hashmap but and log the event.  T
	 * @param hashMapToWriteTo Hashmap to write event data to.
	 * @param mapKey Map Key to write for an event that uses a map
	 * @param mapValue Map Value to write for an event
	 * @param eventName Event Name to show up in Flurry Analytics
	 * @param isTimedEvent True if event is a timed event.  False otherwise
	 */
	public void writeToFlurryAndLogEvent(Map<String, String> hashMapToWriteTo, String mapKey, String mapValue, String eventName, boolean isTimedEvent) {
		if (hashMapToWriteTo != null) {
			hashMapToWriteTo.put(mapKey, mapValue);
			
			if (!isTimedEvent) {
				FlurryAgent.logEvent(eventName, hashMapToWriteTo);
			} else  {
				FlurryAgent.logEvent(eventName, hashMapToWriteTo, true);
			}
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "logged flurry Event: " + eventName + "-hashmap key: " + mapKey + "-hashmap value: " + 
						mapValue + "-timedEvent: " + isTimedEvent);
			}
		}
		else if (eventName != null) {
			FlurryAgent.logEvent(eventName);
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "logged flurry Event: " + eventName);
			}
		}	
	}
	
	/**
	 * Writing to a Flurry hashmap if state changes but not log the event.  The event should be logged
	 * at a later time.
	 * @param hashMapToWriteTo Hashmap to write event data to.
	 * @param mapKey Map Key to write for an event that uses a map.
	 * @param mapValue Map Value to write for an event
	 * @param comment Used by debug message.  Set to empty string if not needed.
	 * @param existingState used to compare states, if it is the same as last state, hashmap is not updated.  Displayed for logging.
	 * @param currentState Current state of Event.  Displayed for logging in this method.  
	 */
	public void writeToFlurry(Map<String, String> hashMapToWriteTo, String mapKey, String mapValue, String comment, String existingState, String currentState) {
		if (hashMapToWriteTo != null) {

			hashMapToWriteTo.put(mapKey, mapValue);
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "writeToFlurry()" + "hashMapToWriteTo()-wrote flurry" + "-hashmap: " + comment + "-key: " + mapKey + "-hashmap value: " + mapValue + "-existingState: " + existingState);
			}
		} else {
			if (AROLogger.logDebug) {
				AROLogger.d(TAG, "writeToFlurry()" + "hashMapToWriteTo()-" + comment + " not updated due to-currentState: " + currentState + "-existingState: " + existingState);
			}
		}
	}

	/**
	 * check whether the device has root access
	 */
	public boolean hasRootAccess() {
		Process sh = null;
		DataOutputStream os = null;
		int exitValue = -1;
		boolean hasRootAccess = false;
		try {
			sh = Runtime.getRuntime().exec("su"); AROLogger.e(TAG, "hasRootAccess - su pid = "+sh);
			os = new DataOutputStream(sh.getOutputStream());
			
			String command = "exit\n";
			os.writeBytes(command);
			os.flush();
			
			exitValue = sh.waitFor();
			
			if (AROLogger.logDebug){
				AROLogger.d(TAG, "exitValue=" + exitValue);
			}
			if (exitValue == 0){
				//successful return value, has root access
				hasRootAccess = true;
			}
			else {
				AROLogger.e(TAG, "root access denied");
				hasRootAccess = false;
			}
			
		} catch (Exception e){
			AROLogger.e(TAG, "does not have root access", e);
		}
		
		return hasRootAccess;
	}

	public static boolean isAnalyzerLaunchInProgress() {
		return isAnalyzerLaunchInProgress;
	}

	public static void setAnalyzerLaunchInProgress(boolean b) {
		ARODataCollector.isAnalyzerLaunchInProgress = b;
	}
	
	/**
	 * Need to put the receiver here so that we always receive this event and reset
	 * the analyzerLaunchWaiting variable. Putting this receiver in the activity
	 * will not always work since the broadcast wont be received if the user exits
	 * the activity and the activity gets destroyed.
	 */
	private BroadcastReceiver analyzerTimeoutReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context ctx, Intent intent) {
	    	AROLogger.d(TAG, "received analyzerTimeoutIntent at " + System.currentTimeMillis());
	    	if (ARODataCollector.isAnalyzerLaunchInProgress()){
	    		//timed out, analyzer launch cancelled, no longer waiting
	    		AROLogger.d(TAG, "analyzer timeout expired, setAnalyzerLaunchInProgress(false)");
	    		ARODataCollector.setAnalyzerLaunchInProgress(false);
	    	}
	    }
	};
	
	/**
	 * method to register the receiver that listens to analyzer timeout
	 */
	private void registerAnalyzerTimeoutReceiver() {
		AROLogger.d(TAG, "registering analyzerTimeOutReceiver");
		registerReceiver(analyzerTimeoutReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_TIMEOUT_SHUTDOWN_INTENT));
	}

}


/**
 * Class to hold Flurry object states.  For example, peripheral usage are logged
 * only when there is a change.
 */
class FlurryEvent {
	private String eventName = AROCollectorUtils.EMPTY_STRING;
	private int counter = 0;
	private Map<String, String> mapToWrite = null;
	private String state = AROCollectorUtils.EMPTY_STRING;

	public FlurryEvent(String eventName, int eventCounter, 
			Map<String, String> mapToWrite, String state) {
		this.eventName = eventName;
		this.counter = eventCounter;
		this.mapToWrite = mapToWrite;
		this.state = state;
	}

	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Map<String, String> getMapToWrite() {
		return mapToWrite;
	}
	public void setMapToWrite(Map<String, String> mapToWrite) {
		this.mapToWrite = mapToWrite;				
	}
	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public int incrementCounter() {
		setCounter(getCounter() + 1);
		return getCounter();
	}

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
