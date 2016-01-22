/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.adb.impl.AdbServiceImpl;
import com.att.aro.core.analytics.AnalyticsEvents;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.android.impl.AndroidImpl;
import com.att.aro.core.commandline.IExternalProcessReader;
import com.att.aro.core.commandline.IExternalProcessRunner;
import com.att.aro.core.commandline.IProcessFactory;
import com.att.aro.core.commandline.impl.ExternalProcessReaderImpl;
import com.att.aro.core.commandline.impl.ExternalProcessRunnerImpl;
import com.att.aro.core.commandline.impl.ProcessFactoryImpl;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.concurrent.impl.ThreadExecutorImpl;
import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.impl.ProfileFactoryImpl;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.fileio.impl.FileManagerImpl;
import com.att.aro.core.impl.AROServiceImpl;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.mobiledevice.impl.AndroidDeviceImpl;
import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.IByteArrayLineReader;
import com.att.aro.core.packetanalysis.ICacheAnalysis;
import com.att.aro.core.packetanalysis.IEnergyModelFactory;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.IPacketAnalyzer;
import com.att.aro.core.packetanalysis.IParseHeaderLine;
import com.att.aro.core.packetanalysis.IPktAnazlyzerTimeRangeUtil;
import com.att.aro.core.packetanalysis.IRequestResponseBuilder;
import com.att.aro.core.packetanalysis.IRrcStateMachineFactory;
import com.att.aro.core.packetanalysis.IRrcStateRangeFactory;
import com.att.aro.core.packetanalysis.ISessionManager;
import com.att.aro.core.packetanalysis.IThroughputCalculator;
import com.att.aro.core.packetanalysis.ITraceDataReader;
import com.att.aro.core.packetanalysis.impl.BurstCollectionAnalysisImpl;
import com.att.aro.core.packetanalysis.impl.ByteArrayLineReaderImpl;
import com.att.aro.core.packetanalysis.impl.CacheAnalysisImpl;
import com.att.aro.core.packetanalysis.impl.EnergyModelFactoryImpl;
import com.att.aro.core.packetanalysis.impl.HttpRequestResponseHelperImpl;
import com.att.aro.core.packetanalysis.impl.PacketAnalyzerImpl;
import com.att.aro.core.packetanalysis.impl.ParseHeaderLineImpl;
import com.att.aro.core.packetanalysis.impl.PktAnazlyzerTimeRangeImpl;
import com.att.aro.core.packetanalysis.impl.RequestResponseBuilderImpl;
import com.att.aro.core.packetanalysis.impl.RrcStateMachineFactoryImpl;
import com.att.aro.core.packetanalysis.impl.RrcStateRangeFactoryImpl;
import com.att.aro.core.packetanalysis.impl.SessionManagerImpl;
import com.att.aro.core.packetanalysis.impl.ThroughputCalculatorImpl;
import com.att.aro.core.packetanalysis.impl.TraceDataReaderImpl;
import com.att.aro.core.packetreader.IDomainNameParser;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.IPacketService;
import com.att.aro.core.packetreader.IPcapngHelper;
import com.att.aro.core.packetreader.impl.DomainNameParserImpl;
import com.att.aro.core.packetreader.impl.NetmonPacketReaderImpl;
import com.att.aro.core.packetreader.impl.PacketReaderImpl;
import com.att.aro.core.packetreader.impl.PacketServiceImpl;
import com.att.aro.core.packetreader.impl.PcapngHelperImpl;
import com.att.aro.core.peripheral.IAlarmAnalysisInfoParser;
import com.att.aro.core.peripheral.IAlarmDumpsysTimestampReader;
import com.att.aro.core.peripheral.IAlarmInfoReader;
import com.att.aro.core.peripheral.IAppInfoReader;
import com.att.aro.core.peripheral.IBatteryInfoReader;
import com.att.aro.core.peripheral.IBluetoothInfoReader;
import com.att.aro.core.peripheral.ICameraInfoReader;
import com.att.aro.core.peripheral.ICpuActivityParser;
import com.att.aro.core.peripheral.ICpuActivityReader;
import com.att.aro.core.peripheral.IDeviceDetailReader;
import com.att.aro.core.peripheral.IDeviceInfoReader;
import com.att.aro.core.peripheral.IGpsInfoReader;
import com.att.aro.core.peripheral.INetworkTypeReader;
import com.att.aro.core.peripheral.IRadioInfoReader;
import com.att.aro.core.peripheral.IScreenRotationReader;
import com.att.aro.core.peripheral.IScreenStateInfoReader;
import com.att.aro.core.peripheral.IUserEventReader;
import com.att.aro.core.peripheral.IVideoTimeReader;
import com.att.aro.core.peripheral.IWakelockInfoReader;
import com.att.aro.core.peripheral.IWifiInfoReader;
import com.att.aro.core.peripheral.impl.AlarmAnalysisInfoParserImpl;
import com.att.aro.core.peripheral.impl.AlarmDumpsysTimestampReaderImpl;
import com.att.aro.core.peripheral.impl.AlarmInfoReaderImpl;
import com.att.aro.core.peripheral.impl.AppInfoReaderImpl;
import com.att.aro.core.peripheral.impl.BatteryInfoReaderImpl;
import com.att.aro.core.peripheral.impl.BluetoothInfoReaderImpl;
import com.att.aro.core.peripheral.impl.CameraInfoReaderImpl;
import com.att.aro.core.peripheral.impl.CpuActivityParserImpl;
import com.att.aro.core.peripheral.impl.CpuActivityReaderImpl;
import com.att.aro.core.peripheral.impl.DeviceDetailReaderImpl;
import com.att.aro.core.peripheral.impl.DeviceInfoReaderImpl;
import com.att.aro.core.peripheral.impl.GpsInfoReaderImpl;
import com.att.aro.core.peripheral.impl.NetworkTypeReaderImpl;
import com.att.aro.core.peripheral.impl.RadioInfoReaderImpl;
import com.att.aro.core.peripheral.impl.ScreenRotationReaderImpl;
import com.att.aro.core.peripheral.impl.ScreenStateInfoReaderImpl;
import com.att.aro.core.peripheral.impl.UserEventReaderImpl;
import com.att.aro.core.peripheral.impl.VideoTimeReaderImpl;
import com.att.aro.core.peripheral.impl.WakelockInfoReaderImpl;
import com.att.aro.core.peripheral.impl.WifiInfoReaderImpl;
import com.att.aro.core.pojo.VersionInfo;
import com.att.aro.core.report.IReport;
import com.att.aro.core.report.impl.HtmlReportImpl;
import com.att.aro.core.report.impl.JSonReportImpl;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;
import com.att.aro.core.resourceextractorimpl.ReadWriteFileExtractorImpl;
import com.att.aro.core.settings.IAROSettings;
import com.att.aro.core.settings.impl.AROSettingsImpl;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.impl.VideoCaptureImpl;
import com.att.aro.core.video.impl.VideoWriterImpl;

/**
 * Spring configuration for ARO.Core<br>
 * Included are all the components to collect, open, analyze and generate reports.
 *
 */
@Configuration
@Lazy
@ComponentScan("com.att.aro")
@Import(AROBestPracticeConfig.class)
@PropertySource({"classpath:bestpractices.properties", "classpath:analytics.properties", "classpath:build.properties"})
@ImportResource({ "classpath*:plugins-analytics.xml", "classpath*:plugins.xml", "classpath*:plugin-manager.xml" ,"classpath*:plugins-noroot.xml"})
public class AROConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Provides access to ARO.Core functionality for analyzing and generating
	 * reports.
	 * 
	 * @return new AROServiceImpl()
	 */
	@Bean
	public IAROService getAROService() {
		return new AROServiceImpl();
	}

	@Bean
	public AnalyticsEvents getAnalyticsEvets(){
		return new AnalyticsEvents();
	}
	
	/**
	 * A model of version info about ARO.Core.
	 * 
	 * @return new VersionInfo()
	 */
	@Bean
	public VersionInfo getInfo() {
		return new VersionInfo();
	}

	/**
	 * Use to Runnable tasks
	 * 
	 * @return new ThreadExecutorImpl()
	 */
	@Bean
	public IThreadExecutor threadExecutor() {
		return new ThreadExecutorImpl();
	}

	/**
	 * Logger functions
	 * 
	 * @return new LoggerImpl("")
	 */
	@Bean
	public ILogger getLog() {
		return new LoggerImpl("");
	}

	/**
	 * Reads/Imports trace data into ARO.Core for MacOS and Linux
	 * 
	 * @return new PacketReaderImpl()
	 */
	@Bean(name = "packetReader")
	public IPacketReader getPacketReader() {
		return new PacketReaderImpl();
	}

	/**
	 * Reads/Imports trace data into ARO.Core for Windows
	 * 
	 * @return new NetmonPacketReaderImpl()
	 */
	@Bean(name = "netmonPacketReader")
	public IPacketReader getNetmonPacketReader() {
		return new NetmonPacketReaderImpl();
	}

	/**
	 * Handles the creation of a Packet from Pcap/Netmon bytebuffers during the
	 * reading of a trace.
	 * 
	 * @return new PacketServiceImpl()
	 */
	@Bean
	public IPacketService getPacketService() {
		return new PacketServiceImpl();
	}

	/**
	 * Handles interpretation of pcapfiles that may have been captured under
	 * MacOS for iOS
	 * 
	 * @return new PcapngHelperImpl()
	 */
	@Bean
	public IPcapngHelper getPcapngHelper() {
		return new PcapngHelperImpl();
	}

	/**
	 * Parses Domain Name from a packet
	 * 
	 * @return new DomainNameParserImpl()
	 */
	@Bean
	public IDomainNameParser getDomainNameParser() {
		return new DomainNameParserImpl();
	}

	/**
	 * Utility for handling files and directories
	 * 
	 * @return new FileManagerImpl()
	 */
	@Bean
	public IFileManager getReadFile() {
		return new FileManagerImpl();
	}

	/**
	 * Parse Cpu activity from a String
	 * 
	 * @return new CpuActivityParserImpl()
	 */
	@Bean
	public ICpuActivityParser getCpuActivityParser() {
		return new CpuActivityParserImpl();
	}

	/**
	 * Loads Cpu Activity from trace file cpu
	 * 
	 * @return new CpuActivityReaderImpl()
	 */
	@Bean
	public ICpuActivityReader getCpuActivityReader() {
		return new CpuActivityReaderImpl();
	}

	/**
	 * Creates List of HttpRequestResponseInfo from a Session
	 * 
	 * @return new RequestResponseBuilderImpl()
	 */
	@Bean
	public IRequestResponseBuilder getRequestResponseBuilder() {
		return new RequestResponseBuilderImpl();
	}

	/**
	 * Assembles and ReAssembles Sessions
	 * 
	 * @return new SessionManagerImpl()
	 */
	@Bean
	public ISessionManager getSessionManager() {
		return new SessionManagerImpl();
	}

	/**
	 * Reads Trace (traffic.cap) file or Trace Directory
	 * 
	 * @return new TraceDataReaderImpl()
	 */
	@Bean
	public ITraceDataReader getTraceDataReader() {
		return new TraceDataReaderImpl();
	}

	/**
	 * Reads the trace file - gps_events
	 * 
	 * @return new GpsInfoReaderImpl()
	 */
	@Bean
	public IGpsInfoReader getGpsInfoReader() {
		return new GpsInfoReaderImpl();
	}

	/**
	 * Reads the trace file - bluetooth_events
	 * 
	 * @return new BluetoothInfoReaderImpl()
	 */
	@Bean
	public IBluetoothInfoReader getBluetoothInfoReader() {
		return new BluetoothInfoReaderImpl();
	}

	/**
	 * Reads the trace file - wifi_events
	 * 
	 * @return new WifiInfoReaderImpl()
	 */
	@Bean
	public IWifiInfoReader getWifiInfoReader() {
		return new WifiInfoReaderImpl();
	}

	/**
	 * Reads the trace file - camera_events
	 * 
	 * @return new CameraInfoReaderImpl()
	 */
	@Bean
	public ICameraInfoReader getCameraInfoReader() {
		return new CameraInfoReaderImpl();
	}

	/**
	 * Reads the trace files - alarm_info_end or alarm_info_start, depending on
	 * what the device collector supplies
	 * 
	 * @return new AlarmAnalysisInfoParserImpl()
	 */
	@Bean
	public IAlarmAnalysisInfoParser getAlarmAnalysisInfoParser() {
		return new AlarmAnalysisInfoParserImpl();
	}

	/**
	 * Reads the trace file - radio_events
	 * 
	 * @return new RadioInfoReaderImpl()
	 */
	@Bean
	public IRadioInfoReader getRadioInfoReader() {
		return new RadioInfoReaderImpl();
	}

	/**
	 * Reads the trace file - batteryinfo_dump
	 * 
	 * @return new WakelockInfoReaderImpl()
	 */
	@Bean
	public IWakelockInfoReader getWakelockInfoReader() {
		return new WakelockInfoReaderImpl();
	}

	/**
	 * Reads the trace file - screen_events
	 * 
	 * @return new ScreenStateInfoReaderImpl()
	 */
	@Bean
	public IScreenStateInfoReader getScreenStateInfoReader() {
		return new ScreenStateInfoReaderImpl();
	}

	/**
	 * Reads the trace file - appname
	 * 
	 * @return new AppInfoReaderImpl()
	 */
	@Bean
	public IAppInfoReader getAppInfoReader() {
		return new AppInfoReaderImpl();
	}

	/**
	 * Reads the trace files - alarm_info_end or alarm_info_start, depending on
	 * what the device collector supplies
	 * 
	 * @return new AlarmDumpsysTimestampReaderImpl(
	 */
	@Bean
	public IAlarmDumpsysTimestampReader getAlarmDumpsysTimestampReader() {
		return new AlarmDumpsysTimestampReaderImpl();
	}

	/**
	 * Reads the trace file - processed_events
	 * 
	 * @return new UserEventReaderImpl()
	 */
	@Bean
	public IUserEventReader getUserEventReader() {
		return new UserEventReaderImpl();
	}

	/**
	 * Reads the trace file - screen_rotations
	 * 
	 * @return new ScreenRotationReaderImpl()
	 */
	@Bean
	public IScreenRotationReader getScreenRotationReader() {
		return new ScreenRotationReaderImpl();
	}

	/**
	 * Reads the trace file - dmesg
	 * 
	 * @return new AlarmInfoReaderImpl()
	 */
	@Bean
	public IAlarmInfoReader getAlarmInfoReader() {
		return new AlarmInfoReaderImpl();
	}

	/**
	 * Reads the trace file - battery_events
	 * 
	 * @return new BatteryInfoReaderImpl()
	 */
	@Bean
	public IBatteryInfoReader getBatteryInfoReader() {
		return new BatteryInfoReaderImpl();
	}

	/**
	 * Reads the trace file - device_details
	 * 
	 * @return new DeviceDetailReaderImpl()
	 */
	@Bean
	public IDeviceDetailReader getDeviceDetailReader() {
		return new DeviceDetailReaderImpl();
	}

	/**
	 * Reads the trace file - network_details
	 * 
	 * @return new NetworkTypeReaderImpl()
	 */
	@Bean
	public INetworkTypeReader getNetworkTypeReader() {
		return new NetworkTypeReaderImpl();
	}

	/**
	 * Reads the trace file - video_time or exVideo_time depending on type of
	 * movie collection
	 * 
	 * @return new VideoTimeReaderImpl()
	 */
	@Bean
	public IVideoTimeReader getVideoTimeReader() {
		return new VideoTimeReaderImpl();
	}

	/**
	 * Reads the trace file - device_info
	 * 
	 * @return new DeviceInfoReaderImpl()
	 */
	@Bean
	public IDeviceInfoReader getDeviceInfoReader() {
		return new DeviceInfoReaderImpl();
	}

	/**
	 * Creates a list of throughput calculations for the specified time range,
	 * sampling window, and list of packets.
	 * 
	 * @return new ThroughputCalculatorImpl()
	 */
	@Bean
	public IThroughputCalculator getThroughputCalculator() {
		return new ThroughputCalculatorImpl();
	}

	/**
	 * creates RrcStateRange based on profile type
	 * 
	 * @return new RrcStateRangeFactoryImpl()
	 */
	@Bean
	public IRrcStateRangeFactory getRrcStateRangeFactory() {
		return new RrcStateRangeFactoryImpl();
	}

	/**
	 * creates an AbstractRrcStateMachine to model RrcStateMachine data
	 * 
	 * @return new RrcStateMachineFactoryImpl()
	 */
	@Bean
	public IRrcStateMachineFactory getRrcStateMachineFactory() {
		return new RrcStateMachineFactoryImpl();
	}

	/**
	 * creates assorted phone profiles dealing with 3G,LTE and WIFI
	 * 
	 * @return new ProfileFactoryImpl()
	 */
	@Bean
	public IProfileFactory getProfileFactory() {
		return new ProfileFactoryImpl();
	}

	/**
	 * Generates EnergyModel
	 * 
	 * @return new EnergyModelFactoryImpl()
	 */
	@Bean
	public IEnergyModelFactory getEnergyModelFactory() {
		return new EnergyModelFactoryImpl();
	}

	/**
	 * Analyzes trace to create BurstCollectionAnalysisData - A model of burst
	 * collection analysis results
	 * 
	 * @return new BurstCollectionAnalysisImpl()
	 */
	@Bean
	public IBurstCollectionAnalysis getBurstCollectionAnalysis() {
		return new BurstCollectionAnalysisImpl();
	}

	/**
	 * Analyzes Trace (traffic.cap) file or Trace Directory
	 * 
	 * @return new PacketAnalyzerImpl()
	 */
	@Bean
	public IPacketAnalyzer getPacketAnalyzer() {
		return new PacketAnalyzerImpl();
	}

	/**
	 * Helper class for interpreting HttpRequestResponseInfo objects
	 * 
	 * @return new HttpRequestResponseHelperImpl()
	 */
	@Bean
	public IHttpRequestResponseHelper getHttpRequestResponseHelper() {
		return new HttpRequestResponseHelperImpl();
	}

	/**
	 * Analyzes all sessions to create the model CacheAnalysis
	 * 
	 * @return new CacheAnalysisImpl()
	 */
	@Bean
	public ICacheAnalysis getCacheAnalysis() {
		return new CacheAnalysisImpl();
	}

	/**
	 * Parses header line into HttpRequestResponseInfo object
	 * 
	 * @return new ParseHeaderLineImpl()
	 */
	@Bean
	public IParseHeaderLine getParseHeaderLineImpl() {
		return new ParseHeaderLineImpl();
	}

	/**
	 * Provides access to a VideoOutputStream to create a movie file, with each
	 * image written as a video frame(s).
	 * 
	 * @return new VideoWriterImpl()
	 */
	@Bean
	@Scope(value = "prototype")
	// => always create a new instance
	public IVideoWriter getVideoWriter() {
		return new VideoWriterImpl();
	}

	/**
	 * Class to encapsulate a byte[] for the retrieval of Strings
	 * @return new ByteArrayLineReaderImpl()
	 */
	@Bean
	public IByteArrayLineReader getByteArrayLineReader() {
		return new ByteArrayLineReaderImpl();
	}

	/**
	 * Used to launch and control Runnable classes to read files
	 * @return new ExternalProcessReaderImpl()
	 */
	@Bean
	public IExternalProcessReader getExternalProcessReaderImpl() {
		return new ExternalProcessReaderImpl();
	}

	/**
	 * Used to launch and control Runnable classes to execute shell processes
	 * @return new ExternalProcessRunnerImpl()
	 */
	@Bean
	public IExternalProcessRunner getExternalProcessRunnerImpl() {
		return new ExternalProcessRunnerImpl();
	}

	/**
	 * Creates JSON reports
	 * @return new JSonReportImpl()
	 */
	@Bean(name = "jsongenerate")
	public IReport getJSonGanarate() {
		return new JSonReportImpl();
	}

	/**
	 * Creates HTML reports
	 * @return new HtmlReportImpl()
	 */
	@Bean(name = "htmlgenerate")
	public IReport getHtmlGenerate() {
		return new HtmlReportImpl();
	}

	/**
	 * Read/Write access to config.properties
	 * @return new SettingsImpl()
	 */
	@Bean
	public IAROSettings getAROConfigFile() {
		return new AROSettingsImpl();
	}

	/**
	 * Controls AndroidDebugBridge ddmlib
	 * @return new AdbServiceImpl()
	 */
	@Bean
	public IAdbService getAdbService() {
		return new AdbServiceImpl();
	}

	/**
	 * Checks if Android device is rooted or not
	 * @return new AndroidDeviceImpl()
	 */
	@Bean
	public IAndroidDevice getAndroidDevice() {
		return new AndroidDeviceImpl();
	}

	/**
	 * Extracts selected files embedded in the ARO.Core jar file. ie tcpdump
	 * @return new ReadWriteFileExtractorImpl()
	 */
	@Bean
	public IReadWriteFileExtractor getReadWriteFileExtractorImpl() {
		return new ReadWriteFileExtractorImpl();
	}

	/**
	 * Captures images to create a video.mov file
	 * @return new VideoCaptureImpl()
	 */
	@Bean
	@Scope(value = "prototype")
	// => always create a new instance
	public IVideoCapture getVideoCapture() {
		return new VideoCaptureImpl();
	}

	/**
	 * Helper class to control Android device or emulator
	 * @return new AndroidImpl()
	 */
	@Bean
	IAndroid getAndroid() {
		return new AndroidImpl();
	}

	/**
	 * Executes shell commands and returns the Runtime
	 * @return new ProcessFactoryImpl()
	 */
	@Bean
	public IProcessFactory getProcessFactory() {
		return new ProcessFactoryImpl();
	}
	
	/**
	 * helper class for packetAnalyzer for corp time range 
	 * @return
	 */
	@Bean
	public IPktAnazlyzerTimeRangeUtil getPktAnalyzerTimeRange() {
		return new PktAnazlyzerTimeRangeImpl();
	}
}
