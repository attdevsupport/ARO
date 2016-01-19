package com.att.aro.datacollector.ioscollector.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDeviceStatus;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.util.GoogleAnalyticsUtil;
import com.att.aro.core.util.Util;
import com.att.aro.datacollector.ioscollector.IOSDeviceStatus;
import com.att.aro.datacollector.ioscollector.ImageSubscriber;
import com.att.aro.datacollector.ioscollector.reader.ExternalDeviceMonitorIOS;
import com.att.aro.datacollector.ioscollector.reader.ExternalProcessRunner;
import com.att.aro.datacollector.ioscollector.reader.UDIDReader;
import com.att.aro.datacollector.ioscollector.utilities.ErrorCodeRegistry;
import com.att.aro.datacollector.ioscollector.utilities.IOSDeviceInfo;
import com.att.aro.datacollector.ioscollector.utilities.RemoteVirtualInterface;
import com.att.aro.datacollector.ioscollector.utilities.XCodeInfo;
import com.att.aro.datacollector.ioscollector.video.VideoCaptureMacOS;

public class IOSCollectorImpl implements IDataCollector, IOSDeviceStatus, ImageSubscriber {

	private IFileManager filemanager;
	private ILogger log = new LoggerImpl("IOSCollector");// ContextAware.getAROConfigContext().getBean(ILogger.class);
	private static ResourceBundle defaultBundle = ResourceBundle.getBundle("messages");
	private volatile boolean running = false;

	/**** time file ****/
	private File timeFile;
	private FileOutputStream timeStream;

	private ExternalDeviceMonitorIOS monitor = null;
	private boolean isDeviceConnected = false;
	private XCodeInfo xcode = null;
	private RemoteVirtualInterface rvi = null;
	private SwingWorker<String, Object> packetworker;
	private VideoCaptureMacOS videoCapture;
	private boolean hasRVI = false;
	private List<IVideoImageSubscriber> videoImageSubscribers = new ArrayList<IVideoImageSubscriber>();
//	private LiveScreenViewDialog liveview;
	private IOSDeviceInfo deviceinfo;
	private SwingWorker<String, Object> videoworker;
	private boolean hasxCodeV = false;
	private String sudoPassword = ""; //sudo password is required for run some commands on Mac

	private String datadir; // dir to save pcap file, video, etc..
	private File videofile;

	/**
	 * Indicates local directory where trace results will be stored
	 */
	private File localTraceFolder;
	private boolean isLiveViewVideo;
	private boolean isCommandLine;
	private boolean isCapturingVideo;
	
	public IOSCollectorImpl() {
		super();
		deviceinfo = new IOSDeviceInfo();
	}

//	/**
//	 * 
//	 */
//	private void initVideo() {
//		liveview = new LiveScreenViewDialog();
//		log.debug("LiveScreenViewDialog done");
//	}

	@Autowired
	public void setFileManager(IFileManager filemanager) {
		this.filemanager = filemanager;
	}

	@Autowired
	public void setLogger(ILogger logger) {
		this.log = logger;
	}

	@Override
	public void onConnected() {
		this.isDeviceConnected = true;
	}

	@Override
	public void onDisconnected() {
		//		showWaitBox();

		this.isDeviceConnected = false;
//		if (liveview != null && liveview.isVisible()) {
//			liveview.setVisible(false);
//		}
		if (packetworker != null) {
			this.stopWorkers();
		}
		if (rvi != null) {
			try {
				rvi.stop();
			} catch (IOException e) {
				log.debug("IOException:", e);
			}
		}
		//		hideWaitBox();
		//		MessageDialogFactory.showErrorDialog(null, "Device disconnected. Please reconnect device.");
	}

	/**
	 * Close down collection processes, Video, RemoteVirtualInterface(tcpdump).
	 * Record start times for Video and tcpdump into video_time file Report stop
	 * times to time file
	 */
	StatusResult stopWorkers() {
		StatusResult status = new StatusResult();
		if (videoCapture != null) {
			videoCapture.signalStop();// no waiting for now
		}

		monitor.stopMonitoring();
		
		if (rvi != null) {
			try {
				rvi.stop();
			} catch (IOException e) {
				log.error("IOException",e);
			}
			recordPcapStartStop();
		}

		if (packetworker != null) {
			packetworker.cancel(true);
			packetworker = null;
			log.info("disposed packetworker");
		}

		if (videoCapture != null) {
			videoCapture.stopCapture();// blocking till video capture engine fully stop

			// create video timestamp file that will sync with pcap file
			BufferedWriter videoTimeStampWriter = null;

			try {
				log.info("Writing video time to file");
				videoTimeStampWriter = new BufferedWriter(new FileWriter(new File(localTraceFolder, TraceDataConst.FileName.VIDEO_TIME_FILE)));
				// Writing a video time in file.
				String timestr = Double.toString(videoCapture.getVideoStartTime().getTime() / 1000.0);
				// append time from tcpdump
				timestr += " " + Double.toString(rvi.getTcpdumpInitDate().getTime() / 1000.0);
				videoTimeStampWriter.write(timestr);

			} catch (IOException e) {
				//				e.printStackTrace();
				log.info("Error writing video time to file: " + e.getMessage());
			} finally {
				try {
					videoTimeStampWriter.close();
				} catch (IOException e) {
					log.debug("IOException:", e);
				}
			}

			if (videoCapture.isAlive()) {
				videoCapture.interrupt();
			}

			videoCapture = null;
			log.info("disposed videoCapture");
			//			closeTimeFile();
		}

		if (videoworker != null) {
			videoworker.cancel(true);
			videoworker = null;
			log.info("disposed videoworker");
		}

		running = false;

		return status;
		//		mainAROAnalyzer.dataCollectorStatusCallBack(Status.READY);
	}

	@Override
	public String getName() {
		return "IOS Data Collector";
	}

	@Override
	public void addDeviceStatusSubscriber(IDeviceStatus arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVideoImageSubscriber(IVideoImageSubscriber subscriber) {
		log.debug("subscribe :" + subscriber.getClass().getName());
		//videoImageSubscribers.add(subscriber);
		videoCapture.addSubscriber(subscriber);
	}

	/**
	 * receive video frame from background capture thread, then forward it to
	 * subscribers
	 */
	@Override
	public void receiveImage(BufferedImage videoimage) {
		log.debug("receiveImage");
		for (IVideoImageSubscriber subscriber : videoImageSubscribers) {
			subscriber.receiveImage(videoimage);
		}
	}

//	@Override
//	public void receiveImage(BufferedImage image) {
//		if (isLiveViewVideo && liveview.isVisible()) {
//			BufferedImage newimg = ImageHelper.resize(image, liveview.getViewWidth(), liveview.getViewHeight());
//			liveview.setImage(newimg);
//
//			// TODO iterate through IVideo subscribers and send them the image 
//			//			BufferedImage newimg = ImageHelper.resize(image, liveview.getViewWidth(),liveview.getViewHeight());
//			//			subscriber.receiveImage(image);
//		}
//		if (!deviceinfo.foundScreensize()) {
//			deviceinfo.updateScreensize(image.getWidth(), image.getHeight());
//			log.info("xxxxxxxxxxxxxx Update screen resolution => " + image.getWidth() + "x" + image.getHeight() + " xxxxxxxxxxxxxxxxxx");
//		}
//	}


	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public String getMinorVersion() {
		return "0.1";
	}

	@Override
	public DataCollectorType getType() {
		return DataCollectorType.IOS;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public void stopRunning() {
		this.running = false;
	}

	@Override
	public StatusResult startCollector(boolean isCommandLine, String tracepath, boolean capturevideo, String passwd) {
		return this.startCollector(isCommandLine
									, tracepath
									, capturevideo
									, false
									, null
									, null
									, passwd);
	}

	@Override
	public StatusResult startCollector(boolean commandLine
										, String folderToSaveTrace
										, boolean captureVideo
										, boolean liveViewVideo
										, String deviceId
										, Hashtable<String, Object> extraParams
										, String password) {

		if (password != null){
			this.sudoPassword = password;
		}
		
		isCapturingVideo = captureVideo;
		
		this. isCommandLine = commandLine;
		if (isCommandLine){
			isLiveViewVideo = false;
		}
		
		this.isLiveViewVideo = liveViewVideo;

		StatusResult status = new StatusResult();
		status.setSuccess(true);
		//avoid running it twice
		if (this.running) {
			return status;
		} // else keep it false until all the following checks are passed
		if (filemanager.directoryExistAndNotEmpty(folderToSaveTrace)) {
			status.setError(ErrorCodeRegistry.getTraceDirExist());
			return status;
		}
		
		//there might be permission issue to creating dir to save trace
		filemanager.mkDir(folderToSaveTrace);
		if (!filemanager.directoryExist(folderToSaveTrace)) {
			status.setError(ErrorCodeRegistry.getFailedToCreateLocalTraceDirectory());
			return status;
		}

		// initialize monitor, xcode and rvi
		status = init(status);
		if (!status.isSuccess()) {// an error has occurred in initialization
			return status;
		}
		
		// 
		String udid = getDeviceSerialNumber(status);
		if (udid == null || udid.length() < 2) {
			//Failed to get Serial Number of Device, connect an IOS device to start.
			log.error(defaultBundle.getString("Error.serialnumberconnection"));
			status.setSuccess(false);
			status.setError(ErrorCodeRegistry.getIncorrectSerialNumber());
		}

		if (!status.isSuccess()) {
			return status; // failed to get device s/n
		}
		
		datadir = folderToSaveTrace;
		localTraceFolder = new File(folderToSaveTrace);
		if (!localTraceFolder.exists()) {
			if (!localTraceFolder.mkdirs()) {
				datadir = "";
				//There was an error creating directory: 
				log.error(defaultBundle.getString("Error.foldernamerequired"));
				status.setSuccess(false);
				status.setError(ErrorCodeRegistry.getMissingFolderName());
				return status;
			}
		}

		final String trafficFilePath = datadir + Util.FILE_SEPARATOR + defaultBundle.getString("datadump.trafficFile"); //"traffic.pcap";

		//device info
		String deviceDetails = datadir + Util.FILE_SEPARATOR + "device_details";
		status = checkDeviceInfo(status, udid, deviceDetails);
		if (!status.isSuccess()) {
			return status; // device info error
		}

		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getIosCollector(),
				GoogleAnalyticsUtil.getAnalyticsEvents().getStartTrace()); //GA Request
		final String serialNumber = udid;

		if (this.sudoPassword == "" || !isValidSudoPassword(sudoPassword)) {
			if (isCommandLine) {
				System.out.println(defaultBundle.getString("Error.sudopasswordissue"));
				System.exit(0);
			} 
			else {
				status.setData("requestPassword");
				return status; // bad or missing sudo password
			}
		}

		launchCollection(trafficFilePath, serialNumber, status);
		return status;
	}
	
	/**
	 * @param isCapturingVideo
	 * @param isLiveViewVideo
	 * @param status
	 * @param trafficFilePath
	 * @param serialNumber
	 * @return
	 */
	private StatusResult launchCollection(final String trafficFilePath, final String serialNumber, StatusResult status) {
		// check RVI status, and reinitialize it if not initialized already
		status = initRVI(status, trafficFilePath, serialNumber);
		if (!status.isSuccess()) {
			return status;
		}

		// packet capture start
		startPacketCollection();

		if (isCapturingVideo) {
			startVideoCapture(status);
			if (!status.isSuccess()) {
				return status;
			}
		}

		if (isCapturingVideo && isLiveViewVideo) {

			if (isDeviceConnected) {
				log.info("device is connected");
			} else {
				log.info("Device not connected");
			}
		}
		return status;
	}

	private StatusResult startVideoCapture(StatusResult status) {

		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getIosCollector(),
				GoogleAnalyticsUtil.getAnalyticsEvents().getVideoCheck()); //GA Request

		if (videoCapture == null) {
			final String videofilepath = datadir + Util.FILE_SEPARATOR + TraceDataConst.FileName.VIDEO_MOV_FILE;
			videofile = new File(videofilepath);
			try {
				videoCapture = new VideoCaptureMacOS(videofile);
			} catch (IOException e) {
				log.error(rvi.getErrorMessage());
				status.setSuccess(false);
				status.setError(ErrorCodeRegistry.getrviError());
				return status;
			}
			videoCapture.setWorkingFolder(datadir);
			videoCapture.addSubscriber(this);
		}

		videoworker = new SwingWorker<String, Object>() {
			@Override
			protected String doInBackground() throws Exception {
				if (videoCapture != null) {
					videoCapture.start();
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					String res = get();
				} catch (Exception ex) {
					log.info("Error thrown by videoworker: " + ex.getMessage());
				}
			}
		};

		videoworker.execute();
		return status;
	}

	private void startPacketCollection() {

		packetworker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				rvi.startCapture();
				running = true;
				return null;
			}

			@Override
			protected void done() {
				try {
					String res = get();
				} catch (Exception ex) {
					log.info("Error thrown by packetworker: " + ex.getMessage());
				}
			}
		};
		packetworker.execute();
	}

	private StatusResult initRVI(StatusResult status, final String trafficFilePath, final String serialNumber) {
		
		log.debug("initRVI");
		if (rvi == null) {
			rvi = new RemoteVirtualInterface(this.sudoPassword);
			rvi.disconnectFromRvi(serialNumber);
		}

		try {
			if (!rvi.setup(serialNumber, trafficFilePath)) {
				log.error(rvi.getErrorMessage());
				if (isCommandLine) {
					System.out.println(rvi.getErrorMessage());
				}
				status.setSuccess(false);
				status.setError(ErrorCodeRegistry.getrviError());
			}
		} catch (Exception e1) {
			log.error(e1.getMessage());
			status.setSuccess(false);
			status.setError(ErrorCodeRegistry.getrviError());
		}
		return status;
	}

	private StatusResult checkDeviceInfo(StatusResult status, String udid, String deviceDetails) {
		if (!deviceinfo.getDeviceInfo(udid, deviceDetails)) {
			log.error(defaultBundle.getString("Error.deviceinfoissue"));
			status.setSuccess(false);
			status.setError(ErrorCodeRegistry.getDeviceInfoIssue());
		} else {
			String version = deviceinfo.getDeviceVersion(); //get device version number
			log.info("Device Version :" + version);
			if (version != null && version.length() > 0) {
				int versionNumber = 0;
				int dotIndex = 0;
				try {
					dotIndex = version.indexOf(".");
					versionNumber = Integer.parseInt(version.substring(0, dotIndex));
					log.info("Parsed Version Number : " + versionNumber);
				} catch (NumberFormatException nfe) {
					log.error(defaultBundle.getString("Error.deviceversionissue"));
					status.setSuccess(false);
					status.setError(ErrorCodeRegistry.getDeviceVersionIssue());
				}
				if (versionNumber < 5) {
					log.error(defaultBundle.getString("Error.iosunsupportedversion"));
					status.setSuccess(false);
					status.setError(ErrorCodeRegistry.getiOSUnsupportedVersion());
				}
			}
		}
		return status;
	}

	public String getDeviceSerialNumber(StatusResult status) {
		String udid = null;

		UDIDReader reader = new UDIDReader();

		try {
			udid = reader.getSerialNumber();
			udid = udid.replaceAll("\\s", ""); // remove whitespace

		} catch (IOException e) {
			log.error(defaultBundle.getString("Error.incorrectserialnumber") + e.getMessage());
			status.setSuccess(false);
			status.setError(ErrorCodeRegistry.getIncorrectSerialNumber());
		}
		return udid;
	}

	private StatusResult init(StatusResult status) {
		if (monitor == null) {
			monitor = new ExternalDeviceMonitorIOS();
			monitor.subscribe(this);
			monitor.start();
			log.info(defaultBundle.getString("Status.start"));//"Started device monitoring");
		}
		if (xcode == null) {
			xcode = new XCodeInfo();
		}
		if (!hasRVI) {
			hasRVI = xcode.isRVIAvailable();
		}
		if (!hasRVI) {
			//please install the latest version of XCode to continue.
			log.error(defaultBundle.getString("Error.xcoderequired"));
			status.setSuccess(false);
			status.setError(ErrorCodeRegistry.getFailedToLoadXCode());
			return status;
		} else {
			if (!hasxCodeV) {
				if (xcode.isXcodeAvailable()) {
					hasxCodeV = xcode.isXcodeSupportedVersionInstalled();
					if (!hasxCodeV) {
						status.setSuccess(false);
						status.setError(ErrorCodeRegistry.getFailedToLoadXCode());
						return status;
					}
				} else {
					//please install the latest version of XCode to continue.
					log.error(defaultBundle.getString("Error.xcoderequired"));
					status.setSuccess(false);
					status.setError(ErrorCodeRegistry.getFailedToLoadXCode());
					return status;
				}
				log.info("Found rvictl command");
			}
		}
		return status;
	}

	/**
	 * get the name of the directory. The last part of full directory after
	 * slash. e.g: full path /User/Documents will return Documents as the name.
	 * 
	 * @return
	 */
	public String getDirectoryName(String path) {
		String name = "";
		if (path.length() > 1) {
			path = path.replace('\\', '/');
			int index = path.lastIndexOf('/');
			if (index != -1) {
				name = path.substring(index + 1);
			} else {
				name = path;
			}
		}
		return name;
	}

	/**
	 * Check if the password provided is correct
	 * 
	 * @param password
	 *            sudoer password
	 * @return true if password is correct otherwise false
	 */
	private boolean isValidSudoPassword(String password) {
		
		ExternalProcessRunner runner = new ExternalProcessRunner();
		String cmd = "echo " + password + " | sudo -k -S cat /etc/sudoers 2>&1";
		String data = null;
		try {
			data = runner.runCmd(new String[] { "bash", "-c", cmd });
		} catch (IOException e) {
			log.debug("IOException:", e);
			//There was an error validating password.
			log.error(defaultBundle.getString("Error.validatepassword"));
			return false;
		}
		if (data != null) {
			data = data.trim();
		}
		if (data != null && data.length() > 1 && !data.contains("incorrect password attempt")) {
			return true;
		}

		return false;
	}

	/**
	 * Create and populate the "time" file time file format line 1: header line
	 * 2: pcap start time line 3: eventtime or uptime (doesn't appear to be
	 * used) line 4: pcap stop time line 5: time zone offset
	 */
	private void recordPcapStartStop() {
		try {

			String sFileName = "time";
			timeFile = new File(datadir + Util.FILE_SEPARATOR + sFileName);
			timeStream = new FileOutputStream(timeFile);
		} catch (IOException e) {
			log.error("file creation error: " + e.getMessage());
		}

		String str = String.format("%s\n%.3f\n%d\n%.3f\n", "Synchronized timestamps" // line 1 (header),,
				, rvi.getTcpdumpInitDate().getTime() / 1000.0 // line 2 (pcap start time)
				, 0 // line 3 (userTime) should refer to device. [ not used ]
				, rvi.getTcpdumpStopDate().getTime() / 1000.0 // line 4 (pcap stop time)
				);

		try {
			timeStream.write(str.getBytes());
			timeStream.flush();
			timeStream.close();
		} catch (IOException e) {
			log.error("closeTimeFile() IOException:" + e.getMessage());
		}

	}

	@Override
	public StatusResult stopCollector() {
		StatusResult status = new StatusResult();
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getIosCollector(),
				GoogleAnalyticsUtil.getAnalyticsEvents().getEndTrace()); //GA Request
		//		showWaitBox();
		this.stopWorkers();
		if (datadir.length() > 1) {
			File folder = new File(datadir);
			if (folder.exists()) {
				//if video data is zero, java media player will throw exception
				if (videofile != null && videofile.exists() && videofile.length() < 2) {
					videofile.delete();
					log.info("deleted empty video file");
				}
				//now check for pcap existence otherwise there will be popup error.
				File pcapfile = new File(datadir + Util.FILE_SEPARATOR + defaultBundle.getString("datadump.trafficFile"));
				status.setSuccess(pcapfile.exists());
			}
		}
		return status;
	}

	/**
	 * check if the internal collector method is running, in this case tcpdump
	 */
	@Override
	public boolean isTrafficCaptureRunning(int seconds) {
		log.info("isiOSCollectorRunning()");
		boolean tcpdumpActive = false;
		int count = 30;
		int timer = seconds / count;
		if (packetworker == null || !isRunning()) {
			return false;
		}
		do {// TODO
			log.debug("isTrafficCaptureRunning :" + packetworker.isCancelled() +" - "+ packetworker.isDone());
			tcpdumpActive = (packetworker.isCancelled() || packetworker.isDone());//checkTcpDumpRunning(device);
			if (!tcpdumpActive) {
				try {
					//log.info("waiting " + timer + ", for tcpdump to launch:" + count);
					Thread.sleep(timer);
				} catch (InterruptedException e) {
				}
			}
		} while (tcpdumpActive == false && count-- > 0);
		return tcpdumpActive;
	}

	@Override
	public void haltCollectorInDevice() {
		stopCollector();
	}

	@Override
	public String[] getLog() {
		// TODO check if ios devices has log that can be imported
		return null;
	}

	@Override
	public void timeOutShutdown() {
		stopCollector();
	}

	/**
	 * Stores the password
	 */
	@Override
	public boolean setPassword(String password) {
		if (isValidSudoPassword(password)){
			sudoPassword = password;
			return true;
		}
		return false;
	}

	/**
	 * Retrieve the sudo password
	 * @return password
	 */
	@Override
	public String getPassword() {
		return sudoPassword;
	}

}
