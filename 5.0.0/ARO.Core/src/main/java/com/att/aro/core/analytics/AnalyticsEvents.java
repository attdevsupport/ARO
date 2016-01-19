/**
 * 
 */
package com.att.aro.core.analytics;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class AnalyticsEvents {

	@Value("${ga.trackid}")
	private String trackerID;
	
	@Value("${ga.request.event.category.analyzer}")
	private String analyzerEvent;
	
	@Value("${ga.request.event.analyzer.action.startapp}")
	private String startApp;
	
	@Value("${ga.request.event.analyzer.action.endapp}")
	private String endApp;
	
	@Value("${ga.request.event.analyzer.action.load}")
	private String loadTrace;
	
	@Value("${ga.request.event.analyzer.action.load.pcap}")
	private String loadPcap;
	
	@Value("${ga.request.event.analyzer.action.almexport}")
	private String almExport;
	
	@Value("${ga.request.event.analyzer.action.exportlabel}")
	private String exportInitiated;
	
	@Value("${ga.request.event.category.collector}")
	private String collector;
	
	@Value("${ga.request.event.collector.action.starttrace}")
	private String startTrace;
	
	@Value("${ga.request.event.collector.action.endtrace}")
	private String endTrace;
	
	@Value("${ga.request.event.collector.action.startapp}")
	private String startCollectorApp;
	
	@Value("${ga.request.event.collector.action.endapp}")
	private String endCollectorApp;
	
	@Value("${ga.request.event.installation.event}")
	private String installer;
	
	@Value("${ga.request.event.installation.java.event}")
	private String language;
	
	@Value("${ga.request.event.collector.ios}")
	private String iosCollector;
	
	@Value("${ga.request.event.collector.rooted}")
	private String rootedCollector;
	
	@Value("${ga.request.event.collector.nonrooted}")
	private String nonRootedCollector;
	
	@Value("${ga.request.event.collector.action.video}")
	private String videoCheck;

	@Value("${ga.request.event.collector.emulator}")
	private String emulator;

	/**
	 * @return the trackerID
	 */
	public String getTrackerID() {
		return trackerID;
	}

	/**
	 * @return the analyzerEvent
	 */
	public String getAnalyzerEvent() {
		return analyzerEvent;
	}

	/**
	 * @return the startApp
	 */
	public String getStartApp() {
		return startApp;
	}

	/**
	 * @return the endApp
	 */
	public String getEndApp() {
		return endApp;
	}

	/**
	 * @return the loadTrace
	 */
	public String getLoadTrace() {
		return loadTrace;
	}

	/**
	 * @return the loadPcap
	 */
	public String getLoadPcap() {
		return loadPcap;
	}

	/**
	 * @return the almExport
	 */
	public String getAlmExport() {
		return almExport;
	}

	/**
	 * @return the exportInitiated
	 */
	public String getExportInitiated() {
		return exportInitiated;
	}

	/**
	 * @return the collector
	 */
	public String getCollector() {
		return collector;
	}

	/**
	 * @return the startTrace
	 */
	public String getStartTrace() {
		return startTrace;
	}

	/**
	 * @return the endTrace
	 */
	public String getEndTrace() {
		return endTrace;
	}

	/**
	 * @return the startCollectorApp
	 */
	public String getStartCollectorApp() {
		return startCollectorApp;
	}

	/**
	 * @return the endCollectorApp
	 */
	public String getEndCollectorApp() {
		return endCollectorApp;
	}

	/**
	 * @return the installer
	 */
	public String getInstaller() {
		return installer;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @return the iosCollector
	 */
	public String getIosCollector() {
		return iosCollector;
	}

	/**
	 * @return the rootedCollector
	 */
	public String getRootedCollector() {
		return rootedCollector;
	}

	/**
	 * @return the nonRootedCollector
	 */
	public String getNonRootedCollector() {
		return nonRootedCollector;
	}

	/**
	 * @return the videoCheck
	 */
	public String getVideoCheck() {
		return videoCheck;
	}

	/**
	 * @return the emulator
	 */
	public String getEmulator() {
		return emulator;
	}
	
}
