/*
 *  Copyright 2015 AT&T
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
package com.att.aro.mvc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.android.ddmlib.IDevice;
import com.att.aro.core.AROConfig;
import com.att.aro.core.IAROService;
import com.att.aro.core.ILogger;
import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDataCollectorManager;
import com.att.aro.core.datacollector.pojo.CollectorStatus;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.ApplicationSelection;
import com.att.aro.core.packetanalysis.pojo.IPAddressSelection;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.pojo.ErrorCodeRegistry;
import com.att.aro.core.util.Util;

public class AROController implements PropertyChangeListener, ActionListener {

	private IAROView theView;
	private AROTraceData theModel;
	private ApplicationContext context = new AnnotationConfigApplicationContext(AROConfig.class);

	@Autowired
	private IAROService serv;

	@Autowired
	private ILogger log;
	private IDataCollector collector;
	private String traceFolderPath;
	private boolean videoCapture;
	private Date traceStartTime;
	private long traceDuration;

	/**
	 * Constructor to instantiate an ARO API instance.
	 * 
	 * @param theView The view used by this controller
	 */
	public AROController(IAROView theView) {
		this.theView = theView;
		this.theModel = new AROTraceData();
		this.theView.addAROPropertyChangeListener(this);
		this.theView.addAROActionListener(this);
		if (this.log == null) {
			this.log = context.getBean(ILogger.class);
		}
	}

	/**
	 * Returns the Model defined by this MVC pattern.
	 * 
	 * @return The model
	 */
	public AROTraceData getTheModel() {
		return theModel;
	}

	/**
	 * <p>Note:  Do not use this method - use <em>updateModel(...)</em> instead.</p><p>
	 * 
	 * Analyze a trace and produce a report either in json or html<br>
	 * 
	 * @param trace The FQPN of the directory or pcap file to analyze
	 * @param profile The Profile to use for this analysis - LTE if null
	 * @param filter The filters to use - can be empty for no filtering specified
	 * @see #updateModel(String, Profile, AnalysisFilter)
	 */
	public AROTraceData runAnalyzer(String trace, Profile profile, AnalysisFilter filter) {

		serv = context.getBean(IAROService.class);
		AROTraceData results = new AROTraceData();

		try {
			// analyze trace file or directory?
			if (serv.isFile(trace)) {
				results = serv.analyzeFile(getBestPractice(), trace, profile, filter);
			} else {
				results = serv.analyzeDirectory(getBestPractice(), trace, profile, filter);
			}

		} catch (IOException exception) {
			log.error(exception.getMessage(), exception);
			results.setSuccess(false);
			results.setError(ErrorCodeRegistry.getUnknownFileFormat());
		}

		return results;
	}

	/**
	 * Not to be directly called.  Triggers a re-analysis if a property change is detected.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Profile profile = null;
		AnalysisFilter filter = null;
		if (theModel.getAnalyzerResult() != null) {
			profile = theModel.getAnalyzerResult().getProfile();
			filter = theModel.getAnalyzerResult().getFilter();
		}
		if (event.getPropertyName().equals("tracePath")) {
			updateModel((String) event.getNewValue(), profile, null);
		} else if (event.getPropertyName().equals("profile")) {
			if (theModel.isSuccess()) {
				updateModel(theModel.getAnalyzerResult().getTraceresult().getTraceDirectory(), (Profile) event.getNewValue(), filter);
			}
		} else if (event.getPropertyName().equals("filter")) {
			updateModel(theModel.getAnalyzerResult().getTraceresult().getTraceDirectory(), profile, (AnalysisFilter) event.getNewValue());
		}
	}

	/**
	 * Not to be directly called.  Handles triggering the functionality if requested by AWT UI.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

		String actionCommand = event.getActionCommand();

		// match on Android and iOS collectors
		//		if (actionCommand.equals("startCollector") || actionCommand.equals("startCollectorIos")) {
		if ("startCollector".equals(actionCommand) || "startCollectorIos".equals(actionCommand)) {
			startCollector(event, actionCommand);
		} else if ("stopCollector".equals(actionCommand)) {
			stopCollector();
			this.theView.updateCollectorStatus(CollectorStatus.STOPPED, null);

			log.info("stopCollector() performed");
		} else if ("haltCollectorInDevice".equals(actionCommand)) {
			haltCollectorInDevice();
			this.theView.updateCollectorStatus(CollectorStatus.STOPPED, null);

			log.info("stopCollector() performed");
		} else if ("printJSONReport".equals(actionCommand)) {
			printReport(true, theView.getReportPath());
		} else if ("printCSVReport".equals(actionCommand)) {
			printReport(false, theView.getReportPath());
		}
	}

	/**
	 * Generates a JSON or HTML report of the last analysis to the specified file
	 * 
	 * @param json true = generate JSON report, false = generate HTML report
	 * @param reportPath The FQPN of the file where this report is generated
	 * @return true = report generation successful
	 */
	public boolean printReport(boolean json, String reportPath) {
		boolean res = false;
		if (json) {
			res = serv.getJSonReport(reportPath, theModel);
			if (res) {
				log.info("Successfully produce JSON report: " + reportPath);
			} else {
				log.info("Failed to produce JSON report: " + reportPath);
			}
		} else {
			res = serv.getHtmlReport(reportPath, theModel);
			if (res) {
				log.info("Successfully produce HTML report: " + reportPath);
			} else {
				log.info("Failed to produce HTML report: " + reportPath);
			}
		}
		return res;
	}

	/**
	 * <p>This is the main entry point for requesting an analysis of a trace.</p><p>
	 * 
	 * <em>path</em> is the file or folder containing the trace raw data if we load
	 * tracefile , the path include the file name ex, ......\traffic.cap if we
	 * load tracefolder, the path include the folder name ex, .....\tracefolder
	 * 
	 * @param path Where the trace directory or .cap file is located
	 * @param profile The Profile to use for this analysis - LTE if null
	 * @param filter The filters to use - can be empty for no filtering specified
	 */
	public void updateModel(String path, Profile profile, AnalysisFilter filter) {
		if (path != null) {
			theModel = runAnalyzer(path, profile, filter);
			if (filter == null && theModel.isSuccess()) { //when the first loading traces, set the filter				
				initializeFilter();
			}
			theView.refresh();

		}
	}

	private void initializeFilter() {
		Collection<String> appNames = theModel.getAnalyzerResult().getTraceresult().getAllAppNames();
		Map<String, Set<InetAddress>> map = theModel.getAnalyzerResult().getTraceresult().getAppIps();
		Map<InetAddress, String> domainNames = new HashMap<InetAddress, String>();
		for (Session tcpSession : theModel.getAnalyzerResult().getSessionlist()) {
			if (!domainNames.containsKey(tcpSession.getRemoteIP())) {
				domainNames.put(tcpSession.getRemoteIP(), tcpSession.getDomainName());
			}
		}
		HashMap<String, ApplicationSelection> applications = new HashMap<String, ApplicationSelection>(appNames.size());
		ApplicationSelection appSelection;
		for (String app : appNames) {
			appSelection = new ApplicationSelection(app, map.get(app));
			appSelection.setDomainNames(domainNames);
			for (IPAddressSelection ipAddressSelection : appSelection.getIPAddressSelections()) {
				ipAddressSelection.setDomainName(domainNames.get(ipAddressSelection.getIpAddress()));
			}
			applications.put(app, appSelection);
		}
		TimeRange timeRange = new TimeRange(0.0, theModel.getAnalyzerResult().getTraceresult().getTraceDuration());
		AnalysisFilter initFilter = new AnalysisFilter(applications, timeRange, domainNames);

		theModel.getAnalyzerResult().setFilter(initFilter);
	}

	/**
	 * Returns the currently available collectors (Android VPN, Android Rooted, IOS).
	 * 
	 * @return The available collectors
	 */
	public List<IDataCollector> getAvailableCollectors() {
		IDataCollectorManager colmg = context.getBean(IDataCollectorManager.class);
		return colmg.getAvailableCollectors(context);
	}

	/**
	 * Returns the available devices to start collections on (Android, IOS on Mac)
	 * 
	 * @return The available devices
	 */
	public IDevice[] getConnectedDevices() {
		IDevice[] devices = null;
		try {
			devices = context.getBean(IAdbService.class).getConnectedDevices();
			//	adb = context.getBean(IAdbService.class).ensureADBServiceStarted();
		} catch (Exception exception) {
			log.error("failed to discover connected devices, Exception :" + exception.getMessage());
		}
		return devices;
	}

	/**
	 * Extract parameters from an AROCollectorActionEvent event. Initiate a
	 * collection on Android and iOS devices
	 * 
	 * @param event
	 * @param actionCommand
	 *            - "startCollector" or "startCollectorIos"
	 */
	private void startCollector(ActionEvent event, String actionCommand) {
		StatusResult result;
		this.theView.updateCollectorStatus(CollectorStatus.STARTING, null);

		if (event instanceof AROCollectorActionEvent) {
			int deviceId 			  = ((AROCollectorActionEvent) event).getDeviceId();
			String deviceSerialNumber = ((AROCollectorActionEvent) event).getDeviceSerialNumber();
			String traceName 		  = ((AROCollectorActionEvent) event).getTrace();
			videoCapture 			  = ((AROCollectorActionEvent) event).isVideoCapture();

			if ("startCollectorIos".equals(actionCommand)) {

				// iOS collector
				collector = ((AROCollectorActionEvent) event).getCollector();
				result = startCollector(deviceSerialNumber, traceName, videoCapture);
				log.info("---------- IOS " + result.toString());
				// trace is complete so report & return
				this.theView.updateCollectorStatus(result.isSuccess() ? CollectorStatus.STARTED : CollectorStatus.STOPPED, result);

				if (result.isSuccess() && videoCapture) {
					this.theView.liveVideoDisplay(collector);
				}
				
			} else {

				// Android collector
				result = startCollector(deviceId, traceName, videoCapture);
				
				log.info("---------- Android " + result.toString());
				if (!result.isSuccess()) { // report failure
					this.theView.updateCollectorStatus(null, result);
					if (result.getError().getCode() == 206) {
						collector = null; // prevent closing the running trace
						try {
							(new File(traceFolderPath)).delete();
						} catch (Exception e) {
							log.warn("failed to delete trace folder :" + traceFolderPath);
						}
					}
				} else { // apk has launched and been activated
					if (videoCapture && "startCollector".equals(actionCommand)) {
						this.theView.liveVideoDisplay(collector);
					}
					this.theView.updateCollectorStatus(CollectorStatus.STARTED, result);
				}
			}
		}
	}

	/**
	 * Start the collector on device (iOS Only)
	 * 
	 * @param deviceSerialNumber
	 * @param traceFolderName
	 * @param videoCapture
	 */
	private StatusResult startCollector(String deviceSerialNumber, String traceFolderName, boolean videoCapture) {
		
		StatusResult result = null;
		
		log.info("starting collector:" + traceFolderName + (videoCapture ? " with video" : " no video"));
		
		traceStartTime = new Date();
		traceFolderPath = Util.getAROTraceDirIOS() + System.getProperty("file.separator") + traceFolderName;
		
		result = collector.startCollector(false, traceFolderPath, videoCapture, true, deviceSerialNumber, null, collector.getPassword());
		log.debug("<><><><><><><> completed <><><><><><><>" + result.toString());
		
		if (result.isSuccess()) {
			Date traceStopTime = new Date();
			traceDuration = traceStopTime.getTime() - traceStartTime.getTime();
		} else {
			traceDuration = 0;
		}
		return result;
	}

	/**
	 * Start the collector on device (Android Only)
	 * 
	 * @param deviceId
	 * @param traceFolderName
	 * @param videoCapture
	 */
	public StatusResult startCollector(int deviceId, String traceFolderName, boolean videoCapture) {

		StatusResult result = null;

		log.info("starting collector:" + traceFolderName + (videoCapture ? " with video" : " no video"));

		getAvailableCollectors();
		IDevice[] androidDevices = getConnectedDevices();

		String androidId = androidDevices[deviceId].getSerialNumber();

		IAndroidDevice androidDev = context.getBean(IAndroidDevice.class);
		boolean rootflag = false;
		try {
			rootflag = androidDev.isAndroidRooted(androidDevices[deviceId]);
		} catch (Exception e) {
			log.error("Sorry, problem testing device: " + e.getMessage());
			result = new StatusResult();
			result.setError(ErrorCodeRegistry.getProblemAccessingDevice(e.getMessage()));
			return result = new StatusResult();
		}

		if (rootflag) {
			log.debug("rooted device");
			collector = context.getBean(IDataCollectorManager.class).getRootedDataCollector();
		} else {
			log.debug("non-rooted device");
			collector = context.getBean(IDataCollectorManager.class).getNorootedDataCollector();
		}

		traceFolderPath = Util.getAROTraceDirAndroid() + System.getProperty("file.separator") + traceFolderName;

		IFileManager fileManager = context.getBean(IFileManager.class);

		if (!fileManager.directoryExistAndNotEmpty(traceFolderPath)) {
			result = collector.startCollector(false, traceFolderPath, videoCapture, true, androidId, null, null);
			traceStartTime = new Date();

			if (result.isSuccess()) {
				log.info("Result : traffic capture launched successfully");
				traceDuration = 0;
			} else {
				log.error("Result trace success:" + result.isSuccess() + ", Name :" + result.getError().getName() + ", Description :" + result.getError().getDescription());
				log.error("device logcat:");
//				for (String line : collector.getLog()) {
//					log.error("logcat :" + line);
//				}
			}
		} else {
			log.info("Illegal path:" + traceFolderPath);
			result = new StatusResult();
			result.setError(ErrorCodeRegistry.getTraceFolderNotFound());
			return result;
		}
		return result;
	}

	/**
	 * Stop the current collection process via a "force close" approach.  Used in case a
	 * collector had problems starting or stopping cleanly but still needs resources cleaned up.
	 */
	public void haltCollectorInDevice() {
		if (collector == null) {
			return;
		} else {
			collector.stopCollector();
			collector.haltCollectorInDevice();
		}
	}

	/**
	 * Stop the current collection process in a clean manner.
	 */
	public void stopCollector() {
		if (collector == null) {
			return;
		}
		log.debug("stopCollector() check if running");
		if (collector.isTrafficCaptureRunning(1)) {
			StatusResult result = collector.stopCollector();
			log.info("stopped collector, result:" + result);
			if (result.isSuccess()) {
				Date traceStopTime = new Date();
				traceDuration = traceStopTime.getTime() - traceStartTime.getTime();
				this.theView.updateCollectorStatus(CollectorStatus.STOPPED, result);
			} else {
				traceDuration = 0;
				this.theView.updateCollectorStatus(null, result);
			}
		} else {
			collector.haltCollectorInDevice();
		}
	}

	/**
	 * return a list of best practice we want to run. the sequence is according
	 * to the Analyzer
	 * 
	 * @return a list of best practice
	 */
	private static List<BestPracticeType> getBestPractice() {
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();
		req.add(BestPracticeType.FILE_COMPRESSION);
		req.add(BestPracticeType.DUPLICATE_CONTENT);
		req.add(BestPracticeType.USING_CACHE);
		req.add(BestPracticeType.CACHE_CONTROL);
		req.add(BestPracticeType.COMBINE_CS_JSS);
		req.add(BestPracticeType.IMAGE_SIZE);
		req.add(BestPracticeType.MINIFICATION);
		req.add(BestPracticeType.SPRITEIMAGE);
		req.add(BestPracticeType.CONNECTION_OPENING);
		req.add(BestPracticeType.UNNECESSARY_CONNECTIONS);
		req.add(BestPracticeType.PERIODIC_TRANSFER);
		req.add(BestPracticeType.SCREEN_ROTATION);
		req.add(BestPracticeType.CONNECTION_CLOSING);
		req.add(BestPracticeType.HTTP_4XX_5XX);
		req.add(BestPracticeType.HTTP_3XX_CODE);
		req.add(BestPracticeType.SCRIPTS_URL);
		req.add(BestPracticeType.ASYNC_CHECK);
		req.add(BestPracticeType.HTTP_1_0_USAGE);
		req.add(BestPracticeType.FILE_ORDER);
		req.add(BestPracticeType.EMPTY_URL);
		req.add(BestPracticeType.FLASH);
		req.add(BestPracticeType.DISPLAY_NONE_IN_CSS);
		req.add(BestPracticeType.ACCESSING_PERIPHERALS);
		return req;
	}

	/**
	 * Returns the base path as persisted by ARO from which to select traces for analysis
	 * 
	 * @return Base path
	 */
	public String getTraceFolderPath() {
		return traceFolderPath;
	}

	/**
	 * Returns whether video capture is requested to be included for data collection
	 * 
	 * @return true = video is part of the collected data
	 */
	public boolean isVideoCapture() {
		return videoCapture;
	}

	/**
	 * Returns the length of time captured in milliseconds.
	 * 
	 * @return length of the trace in milliseconds
	 */
	public long getTraceDuration() {
		return traceDuration;
	}

	/**
	 * Do not use - for internal use only.
	 * 
	 * @param traceDuration
	 */
	public void setTraceDuration(long traceDuration) {
		this.traceDuration = traceDuration;
	}

}
