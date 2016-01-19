package com.att.aro.core.packetanalysis.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.ITraceDataReader;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.packetanalysis.pojo.TraceTime;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.peripheral.IAlarmAnalysisInfoParser;
import com.att.aro.core.peripheral.IAlarmDumpsysTimestampReader;
import com.att.aro.core.peripheral.IAlarmInfoReader;
import com.att.aro.core.peripheral.IAppInfoReader;
import com.att.aro.core.peripheral.IBatteryInfoReader;
import com.att.aro.core.peripheral.IBluetoothInfoReader;
import com.att.aro.core.peripheral.ICameraInfoReader;
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
import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisResult;
import com.att.aro.core.peripheral.pojo.AlarmDumpsysTimestamp;
import com.att.aro.core.peripheral.pojo.AlarmInfo;
import com.att.aro.core.peripheral.pojo.AppInfo;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CpuActivityList;
import com.att.aro.core.peripheral.pojo.DeviceDetail;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.NetworkTypeObject;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.VideoTime;
import com.att.aro.core.peripheral.pojo.WakelockInfo;
import com.att.aro.core.peripheral.pojo.WifiInfo;
import com.att.aro.core.util.Util;

public class TraceDataReaderImpl implements IPacketListener, ITraceDataReader {
	@InjectLogger
	private static ILogger logger;
	
	private IFileManager filereader;
	
	@Autowired
	public void setFileReader(IFileManager filereader){
		this.filereader = filereader;
	}
	
	@Autowired
	@Qualifier("packetReader")
	private IPacketReader packetreader;
	
	@Autowired
	private ICpuActivityReader cpureader;
	
	@Autowired
	private IGpsInfoReader gpsreader;
	
	@Autowired
	private IBluetoothInfoReader bluetoothreader;
	
	@Autowired
	private IWifiInfoReader wifireader;
	
	@Autowired
	private ICameraInfoReader camerareader;
	
	@Autowired
	private IScreenStateInfoReader screenstatereader;
	
	@Autowired
	private IAppInfoReader appinforeader;
	
	@Autowired
	private IRadioInfoReader radioinforeader;
	
	@Autowired
	private IAlarmAnalysisInfoParser alarmanalysisinfoparser;
	
	@Autowired
	private IWakelockInfoReader wakelockinforeader;
	
	@Autowired
	private IAlarmDumpsysTimestampReader alarmdumpsysreader;
	
	@Autowired
	private IUserEventReader usereventreader;
	
	@Autowired
	private IScreenRotationReader screenrotationreader;
	
	@Autowired
	private IAlarmInfoReader alarminforeader;
	
	@Autowired
	private IBatteryInfoReader batteryinforeader;
	
	@Autowired
	private IVideoTimeReader videotimereader;
	
	@Autowired
	private INetworkTypeReader networktypereader;
	
	@Autowired
	private IDeviceDetailReader devicedetailreader;
	
	@Autowired
	private IDeviceInfoReader deviceinforeader;
	
	private Set<InetAddress> localIPAddresses = null;
	private List<PacketInfo> allPackets = null;
	private Map<InetAddress, Integer> ipCountMap = null;
	
	private void init(){
		localIPAddresses = new HashSet<InetAddress>(1);
		allPackets = new ArrayList<PacketInfo>();
	}
	/**
	 * read all kind of trace file in a directry
	 * @param directoryPath full path to physical directory
	 * @throws FileNotFoundException 
	 */
	public TraceDirectoryResult readTraceDirectory(String directoryPath) throws FileNotFoundException{
		if(!filereader.directoryExist(directoryPath)){
			throw new FileNotFoundException("Not found directory: "+directoryPath);
		}
		TraceDirectoryResult result = new TraceDirectoryResult();
		result.setTraceDirectory(directoryPath);
		
		//readAppInfo();
		//Reads the application names from the appinfo trace file.
		AppInfo app = appinforeader.readData(directoryPath);
		result.setAppVersionMap(app.getAppVersionMap());
		result.setAppInfos(app.getAppInfos());

		// Read the time file and PCAP trace
		try {
			result = readTimeAndPcap(result);
		} catch (IOException e1) {
			logger.error("Failed to read file",e1);
			return null;//no need to continue, everything else is useless without packet data
		}
		if(result == null){
			return null;
		}
 
		//extract ip address from device_info file
		result.setLocalIPAddresses(deviceinforeader.readData(directoryPath));
		this.localIPAddresses.addAll(result.getLocalIPAddresses());
		
		readDeviceDetails(result);
		
		readNetworkDetails(result);
		
		readCpuTraceFile(result);

		readGps(result);

		readBluetooth(result);
		
		readWifi(result);

		readCamera(result);

		readScreenState(result);
		
		readUserEvents(result);
		
		readScreenRotations(result);

		readAlarmDumpsysTimestamp(result);
		
		try {
 			readAlarmAnalysisInfo(result);
		} catch (IOException e) {
			logger.info("*** Warning: no alarm dumpsys information found ***");
		}
		
		//alarm info from kernel log file
		readKernelLog(result);
			
		readBattery(result);
		
		readWakelockInfo(result);
		
		readRadioEvents(result);
		
		readVideoTime(result);

		return result;
	}
	
	/**
	 * Read a trace file. (ie traffic.cap)
	 * @param traceFilePath full path to a trace file
	 * @return TraceFileResult
	 * @throws IOException
	 */
	public TraceFileResult readTraceFile(String traceFilePath) throws IOException{
		if(!filereader.fileExist(traceFilePath)){
			throw new FileNotFoundException("Trace file not found: "+traceFilePath);
		}
		//read pcap file only
		TraceFileResult result = new TraceFileResult();
		String traceDirectory = filereader.getDirectory(traceFilePath);
		if(traceDirectory == null){
			traceDirectory = "";
		}
		result.setTraceDirectory(traceFilePath);
		this.init();
		this.ipCountMap = result.getIpCountMap();
		result = (TraceFileResult)this.readPcapTraceFile(traceFilePath, null, null, result);
		if(result == null){
			return null;
		}
		readVideoTime(result);
		result.setAllpackets(allPackets);
		return result;
	}
	
	/**
	 * 
	 * @param TraceDirectoryResult res
	 * @return
	 * @throws IOException
	 */
	private TraceTime readTimes(TraceDirectoryResult res) throws IOException {
		String filepath = res.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.TIME_FILE;
		String[] lines = filereader.readAllLine(filepath);
		TraceTime result = new TraceTime();
		
		String line;
		//ignore line 1 and start from line 2 in file
		//array index 0 is line 1
		
		// Second line is pcap time
		
		if (lines.length > 1) {
			line = lines[1];
			result.setStartTime(Double.valueOf(line));

		}
		if(lines.length > 2){
			line = lines[2];
			result.setEventTime(Double.parseDouble(line) / 1000.0);
		}
		if(lines.length > 3){
			line = lines[3];
			Double duration = Double.parseDouble(line) - result.getStartTime();
			result.setDuration(duration);
		}
		if (lines.length > 4) {
			line = lines[4];
			try {
			result.setTimezoneOffset(Integer.valueOf(line));
			}catch (NumberFormatException e){
				logger.warn("Unable to parse Collector Timezone Offset - value: " + line);
			}
		}
		
		return result;
	}
	/**
	 * Reads the application ID's from the appid trace file.
	 * 
	 * @param appIdFileName Name of the file containing list of application IDs.
	 * @return The list of app ids found in the trace data.
	 * @throws IOException
	 */
	List<Integer> readAppIDs(TraceDirectoryResult res) throws IOException {
		String filepath = res.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.APPID_FILE;
		
		if (!filereader.fileExist(filepath)) {
			return Collections.emptyList();
		}
		String[] lines = filereader.readAllLine(filepath);
		List<Integer> appIds = new ArrayList<Integer>();
		
		int appId;
		for (String line: lines) {
			line = line.trim();
			if (!(line.isEmpty())) {
				appId = Integer.valueOf(line);
				// Check for EOF indicator
				if (appId == TraceDataConst.PACKET_EOF) {
					break;
				}
				appIds.add(appId);
			} else {
				logger.warn("appid file contains a line not well formated");
			}
			
		}
		
		return appIds;
	}
	/**
	 * Reads the PCAP file and the trace times from the time trace file.
	 * 
	 * @throws IOException
	 */
	private TraceDirectoryResult readTimeAndPcap(TraceDirectoryResult dresult) throws IOException {
		TraceDirectoryResult result = dresult;
		String filepath = result.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.TIME_FILE;
		
		Double startTime = null;
		Double duration = null;
		if (filereader.fileExist(filepath)) {
			TraceTime times = readTimes(result);
			startTime = times.getStartTime();
			if (times.getEventTime() != null) {
				result.setEventTime0(times.getEventTime());
			}
			if (times.getTimezoneOffset() != null){
				result.setCaptureOffset(times.getTimezoneOffset());
			}
			duration = times.getDuration();

		}

		// Read the pcap files to get default times
		List<Integer> appIds = readAppIDs(result);
		result.setAppIds(appIds);
		result.setTotalNoPackets(appIds.size());
		filepath = result.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.TRAFFIC + TraceDataConst.FileName.CAP_EXT;
		
		this.init();
		this.ipCountMap = result.getIpCountMap();
		result = (TraceDirectoryResult)this.readPcapTraceFile(filepath, startTime, duration, result);
		if(result == null){
			return new TraceDirectoryResult();
		}
		
		for (int i = 1;; i++) {
			filepath = result.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.TRAFFIC + i + TraceDataConst.FileName.CAP_EXT;
			if (filereader.fileExist(filepath)) {
				result = (TraceDirectoryResult)this.readPcapTraceFile(filepath, startTime, duration, result);
			} else {
				break;
			}
		}
		if (result==null){
			result = new TraceDirectoryResult(); 
		}
		result.setAllpackets(this.allPackets);
		this.checkExternalVideoAndTime(result, startTime, duration);
		return result;
	}
	
	/**
	 * Reads a device data from the device file in trace folder.
	 * 
	 * @throws IOException
	 */
	private void readDeviceDetails(TraceDirectoryResult result) {
		DeviceDetail device = devicedetailreader.readData(result.getTraceDirectory());
		if(device != null){
			result.setDeviceDetail(device);
			if(device.getTotalLines() > 7){
				String line = device.getScreenSize();
				if (null != line) {
					String[] resolution = line.split("\\*");
					result.setDeviceScreenSizeX(Integer.parseInt(resolution[0]));
					result.setDeviceScreenSizeY(Integer.parseInt(resolution[1]));
				}
			}
		}
		
	}

	private void readNetworkDetails(TraceDirectoryResult result){
		NetworkTypeObject obj = networktypereader.readData(result.getTraceDirectory(), result.getPcapTime0(), result.getTraceDuration());
		if(obj != null){
			result.setNetworkTypeInfos(obj.getNetworkTypeInfos());
			result.setNetworkTypesList(obj.getNetworkTypesList());
		}
	}
	/**
	 * Associate app IDs with packets 
	 */
	private String getAppNameForPacket(int packetIdx, List<Integer> appIds, List<String> appInfos){
		
		String appName = "Unknown";
		int numberOfAppIds = appIds.size();
				
		if (!appIds.isEmpty() && !appInfos.isEmpty()) {
			if (packetIdx < numberOfAppIds && packetIdx >= TraceDataConst.VALID_UNKNOWN_APP_ID ) {
	
				int appIdIdx = appIds.get(packetIdx);
				if (appIdIdx >= 0) {
					if (appIdIdx < appInfos.size())
					{
						appName = appInfos.get(appIdIdx);
					} else {
						logger.debug("Invalid app ID "+appIdIdx+" for packet "+packetIdx);
					}
				} else if (appIdIdx != TraceDataConst.VALID_UNKNOWN_APP_ID) {
					logger.debug("Invalid app ID "+appIdIdx+" for packet "+packetIdx);
				}
				
			} else {
				logger.debug("No app ID for packet "+ packetIdx);
			}
		}

		return appName;
		
	}
	
	/**
	 * Parses the user event trace
	 * 
	 * @throws IOException
	 */
	private void readUserEvents(TraceDirectoryResult result) {
		List<UserEvent> userEvents = usereventreader.readData(result.getTraceDirectory(), result.getEventTime0(), result.getPcapTime0());
		result.setUserEvents(userEvents);
	}
	/**
	 * Reads the screen rotations information contained in the
	 * "screen_rotations" file found inside the trace directory and adds them to
	 * the user events list.
	 * 
	 * @throws IOException
	 */
	private void readScreenRotations(TraceDirectoryResult result) {
		List<UserEvent> list = this.screenrotationreader.readData(result.getTraceDirectory(), result.getPcapTime0());
		result.setScreenRotationCounter(list.size());
		result.getUserEvents().addAll(list);
	}
	/**
	 * Reads the CPU trace information from the CPU file.
	 * 
	 * @throws IOException
	 */
	private void readCpuTraceFile(TraceDirectoryResult result) {
		CpuActivityList cpuActivityList = cpureader.readData(result.getTraceDirectory(), result.getPcapTime0());
		result.setCpuActivityList(cpuActivityList);
	}
	
	/**
	 * Method to read the GPS data from the trace file and store it in the
	 * gpsInfos list. It also updates the active duration for GPS.
	 */
	private void readGps(TraceDirectoryResult result){
		List<GpsInfo> gpsInfos = gpsreader.readData(result.getTraceDirectory(), result.getPcapTime0(), result.getTraceDuration());
		result.setGpsInfos(gpsInfos);
		result.setGpsActiveDuration(gpsreader.getGpsActiveDuration());
	}
	/**
	 * Method to read the Bluetooth data from the trace file and store it in the
	 * bluetoothInfos list. It also updates the active duration for Bluetooth.
	 * 
	 * @throws IOException
	 */
	private void readBluetooth(TraceDirectoryResult result) {
		List<BluetoothInfo> bluetoothInfos = bluetoothreader.readData(result.getTraceDirectory(), result.getPcapTime0(), result.getTraceDuration());
		result.setBluetoothInfos(bluetoothInfos);
		result.setBluetoothActiveDuration(bluetoothreader.getBluetoothActiveDuration());
	}
	
	/**
	 * Method to read the WIFI data from the trace file and store it in the
	 * wifiInfos list. It also updates the active duration for Wifi.
	 */
	private void readWifi(TraceDirectoryResult result) {
		List<WifiInfo> wifiInfos = wifireader.readData(result.getTraceDirectory(), result.getPcapTime0(), result.getTraceDuration());
		result.setWifiInfos(wifiInfos);
		result.setWifiActiveDuration(wifireader.getWifiActiveDuration());
	}
	
	/**
	 * Method to read the Camera data from the trace file and store it in the
	 * cameraInfos list. It also updates the active duration for Camera.
	 */
	private void readCamera(TraceDirectoryResult result){
		List<CameraInfo> cameraInfos = camerareader.readData(result.getTraceDirectory(),result.getPcapTime0(), result.getTraceDuration());
		result.setCameraInfos(cameraInfos);
		result.setCameraActiveDuration(camerareader.getActiveDuration());
	}

	/**
	 * Method to read the Screen State data from the trace file and store it in
	 * the ScreenStateInfos list.
	 */
	private void readScreenState(TraceDirectoryResult result){
		List<ScreenStateInfo> screenStateInfos = screenstatereader.readData(result.getTraceDirectory(), result.getPcapTime0(), result.getTraceDuration());
		result.setScreenStateInfos(screenStateInfos);
	}
	
	/**
	 * Method to read the Battery data from the trace file and store it in the
	 * batteryInfos list.
	 */
	private void readBattery(TraceDirectoryResult result) {
		List<BatteryInfo> batteryInfos = batteryinforeader.readData(result.getTraceDirectory(), result.getPcapTime0());
		result.setBatteryInfos(batteryInfos);
	}
	/**
	 * Method to read the alarm event from the trace file and store it in the
	 * alarmInfos list.
	 */
	private void readKernelLog(TraceDirectoryResult result){
		List<AlarmInfo> alarmInfos = alarminforeader.readData(result.getTraceDirectory(), result.getDumpsysEpochTimestamp(), 
				result.getDumpsysElapsedTimestamp(), result.getTraceDateTime());
		result.setAlarmInfos(alarmInfos);
	}
	
	/** 
	 * Method to set a reference time
	 * Use ALARM_END_FILE elapsed realtime as dumpsys batteryinfo time reference.
	 * 
	 * set: dumpsysEpochTimestamp
	 * 	dumpsysElapsedTimestamp
	 *
	 */
	private void readAlarmDumpsysTimestamp(TraceDirectoryResult result){
		AlarmDumpsysTimestamp time = this.alarmdumpsysreader.readData(result.getTraceDirectory(), 
				result.getTraceDateTime(), result.getTraceDuration(), result.getOsVersion(), result.getEventTime0());
		if(time != null){
			result.setDumpsysElapsedTimestamp(time.getDumpsysElapsedTimestamp());
			result.setDumpsysEpochTimestamp(time.getDumpsysEpochTimestamp());
		}
	}

	/**
	 * Create List<AlarmStatisticsInfos> of alarms triggered during the trace. 
	 */
	private void readAlarmAnalysisInfo(TraceDirectoryResult res) throws IOException {
		String filepath = res.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.ALARM_END_FILE;
		// Collect triggered alarms summary at end of capture
		if (!filereader.fileExist(filepath)) {
			return;
		}
		filepath = res.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.ALARM_START_FILE;
		if (!filereader.fileExist(filepath)) {
			return;
		}
		AlarmAnalysisResult result = alarmanalysisinfoparser.parse(res.getTraceDirectory(), TraceDataConst.FileName.ALARM_END_FILE, res.getOsVersion(),
				res.getDumpsysEpochTimestamp(), res.getDumpsysElapsedTimestamp(), res.getTraceDateTime());
		// alarmanalysisinfoparser.parse is flawed, an index can run off the end of a String[]
		if (result != null) {
			List<AlarmAnalysisInfo> alarmStatisticsInfosEnd = result.getStatistics();
			res.getScheduledAlarms().putAll(result.getScheduledAlarms());
			// Collect triggered alarms summary at start of capture
			AlarmAnalysisResult result2 = alarmanalysisinfoparser.parse(res.getTraceDirectory(), TraceDataConst.FileName.ALARM_START_FILE, res.getOsVersion(),
					res.getDumpsysEpochTimestamp(), res.getDumpsysElapsedTimestamp(), res.getTraceDateTime());
			List<AlarmAnalysisInfo> alarmStatisticsInfosStart = result2.getStatistics();
			res.getScheduledAlarms().putAll(result2.getScheduledAlarms());

			// Differentiate the triggered alarms between start/end of catpure.
			if (alarmStatisticsInfosEnd != null && alarmStatisticsInfosStart != null) {
				List<AlarmAnalysisInfo> alarmStatisticsInfos = alarmanalysisinfoparser.compareAlarmAnalysis(alarmStatisticsInfosEnd, alarmStatisticsInfosStart);
				res.setAlarmStatisticsInfos(alarmStatisticsInfos);
			}
		}
	}

	/* 
	 * Method to read the Wakelock data from the batteryinfo file and store it in the
	 * wakelockInfos list.
	 *
	 * pre: call readAlarmDumpsysTimestamp(), it requires a timestamp for alignment.
	 *
	 * */
	private void readWakelockInfo(TraceDirectoryResult result) {
		List<WakelockInfo> wakelockInfos = wakelockinforeader.readData(result.getTraceDirectory(), result.getOsVersion(), 
				result.getDumpsysEpochTimestamp(), result.getTraceDateTime());
		result.setWakelockInfos(wakelockInfos);
	}
	/**
	 * Reads the Radio data from the file and stores it in the RadioInfo.
	 */
	private void readRadioEvents(TraceDirectoryResult result) {
		List<RadioInfo> radioInfos = radioinforeader.readData(result.getTraceDirectory(), result.getPcapTime0());
		result.setRadioInfos(radioInfos);
	}
	/**
	 * Method to read times from the video time trace file and store video time
	 * variables.
	 */
	private void readVideoTime(AbstractTraceResult result) {
		// Read the external video file,If available.
		String dirParent = null;
		if(result.getTraceResultType().equals(TraceResultType.TRACE_FILE)){
			dirParent = filereader.getDirectory(result.getTraceDirectory());
		}else{
			dirParent = result.getTraceDirectory();
		}
		logger.info("dirParent: "+dirParent);
		
		VideoTime vtime = videotimereader.readData(dirParent, result.getTraceDateTime());
		result.setVideoStartTime(vtime.getVideoStartTime());
		result.setExVideoFound(vtime.isExVideoFound());
		result.setExVideoTimeFileNotFound(vtime.isExVideoTimeFileNotFound());
		result.setNativeVideo(vtime.isNativeVideo());
		if (result.getVideoStartTime() == 0.0) {
			result.setVideoStartTime(result.getPcapTime0());
		}
	}
	
	private AbstractTraceResult readPcapTraceFile(String filepath, Double startTime, Double duration, AbstractTraceResult dresult) throws IOException {
		if (!filereader.fileExist(filepath)) {
			if (logger != null){
				logger.error("No packet file found at: " + filepath);
			}
			return null;
		}
		AbstractTraceResult result = dresult;
		if (this.packetreader == null) {
			//this.packetreader = new PacketReaderImpl();
			throw new NullPointerException("this.packetreader is null");
		}
		this.packetreader.readPacket(filepath, this);

		double pcapTime0 = 0;
		double traceDuration = 0;
		// Determine application name associated with each packet
		if (!allPackets.isEmpty()) {

			pcapTime0 = startTime != null ? startTime.doubleValue() : allPackets.get(0).getPacket().getTimeStamp();
			traceDuration = duration != null ? duration.doubleValue() : allPackets.get(allPackets.size() - 1).getPacket().getTimeStamp() - pcapTime0;
			List<Integer> appIds = result.getAppIds();
			if (appIds == null) {
				appIds = Collections.emptyList();
				result.setAppIds(appIds);
			}

			//Determine if timezone difference needs to be accounted for
			int tzDiff = 0;
			int captureOffset = result.getCaptureOffset();
			if (captureOffset != -1) {
				int localOffset = Calendar.getInstance().getTimeZone().getRawOffset() / 1000;
				int collectorOffset = captureOffset * 60 * -1;
				tzDiff = collectorOffset - localOffset;

			}

			int packetIdx = 0;
			List<String> appInfos = result.getAppInfos();
			Set<String> allAppNames = result.getAllAppNames();
			Map<String, Set<InetAddress>> appIps = result.getAppIps();
			for (Iterator<PacketInfo> iter = allPackets.iterator(); iter.hasNext();) {
				PacketInfo packet = iter.next();

				// Filter out non-IP packets
				if (!(packet.getPacket() instanceof IPPacket)) {
					iter.remove();
					continue;
				}

				IPPacket ipPacket = (IPPacket) packet.getPacket();

				packet.setDir(determinePacketDirection(ipPacket.getSourceIPAddress(), ipPacket.getDestinationIPAddress()));
				packet.setTimestamp(ipPacket.getTimeStamp() - pcapTime0 - tzDiff);

				//Associate application ID with the packet 
				String appName = getAppNameForPacket(packetIdx, appIds, appInfos);
				packet.setAppName(appName);
				allAppNames.add(appName);

				// Group IPs by app
				Set<InetAddress> ips = appIps.get(appName);
				if (ips == null) {
					ips = new HashSet<InetAddress>();
					appIps.put(appName, ips);
				}
				ips.add(packet.getRemoteIPAddress());

				// Set packet ID to match Wireshark ID
				packet.setPacketId(++packetIdx);
			}

			Collections.sort(allPackets);
		} else {
			pcapTime0 = startTime != null ? startTime.doubleValue() : filereader.getLastModified(filepath) / 1000.0;
			traceDuration = duration != null ? duration.doubleValue() : 0.0;
		}
		Date traceDateTime = new Date((long) (pcapTime0 * 1000));
		result.setPcapTime0(pcapTime0);
		result.setTraceDuration(traceDuration);
		result.setTraceDateTime(traceDateTime);
		return result;
	}
	
	void checkExternalVideoAndTime(AbstractTraceResult result, Double startTime, Double duration) throws IOException{
		List<Integer> appIds = result.getAppIds();
		// Only if Pcap file is loaded, execute the video sync process below.
		if((appIds.isEmpty()) && (startTime == null) && (duration == null)){
			
			 boolean exVideoFound = true;
			 boolean exVideoTimeFileNotFound = false;
			 double videoStartTime = 0;
			 // get the video_time file.
			 String videotimefile = result.getTraceDirectory() + Util.FILE_SEPARATOR + TraceDataConst.FileName.EXVIDEO_TIME_FILE;
			 
			 if (!filereader.fileExist(videotimefile)) {
					exVideoTimeFileNotFound =true;
					exVideoFound = false;
			}else {
				String[] lines = filereader.readAllLine(videotimefile);
					
				if (lines.length > 0) {
					String line = lines[0];
					String[] strValues = line.split(" ");
					if (strValues.length > 0) {
						try {
							videoStartTime = Double.parseDouble(strValues[0]);
						} catch (NumberFormatException e) {
							logger.error("Cannot determine actual video start time", e);
						}
						if (strValues.length > 1) {
							/* For emulator only, tcpdumpLocalStartTime is start
							 * time started according to local pc/laptop.
							 * getTraceDateTime is time according to emulated
							 * device -- the tcpdumpDeviceVsLocalTimeDetal is
							 * difference between the two and is added as an
							 * offset to videoStartTime so that
							 * traceEmulatorTime and videoStartTime are in sync.
							 */
							double tcpdumpLocalStartTime = Double.parseDouble(strValues[1]);
							double tcpdumpDeviceVsLocalTimeDelta = (result.getTraceDateTime()
									.getTime() / 1000.0) - tcpdumpLocalStartTime;
							videoStartTime += tcpdumpDeviceVsLocalTimeDelta;
						}
					}
				}
					
			}
			result.setVideoStartTime(videoStartTime);
			result.setExVideoFound(exVideoFound);
			result.setExVideoTimeFileNotFound(exVideoTimeFileNotFound);
		}
	}

	/**
	 * Attempts to determine packet direction based upon source and destination
	 * IP addresses
	 */
	private PacketDirection determinePacketDirection(InetAddress source, InetAddress dest) {

		// Check identified local IP addresses
		if (this.localIPAddresses.contains(source)) {
			return PacketDirection.UPLINK;
		} else if (this.localIPAddresses.contains(dest)) {
			return PacketDirection.DOWNLINK;
		}

		// Do same check done by ARO prototype
		boolean srcLocal = isLocal(source);
		boolean destLocal = isLocal(dest);
		if (srcLocal && !destLocal) {
			this.localIPAddresses.add(source);
			return PacketDirection.UPLINK;
		} else if (destLocal && !srcLocal) {
			this.localIPAddresses.add(dest);
			return PacketDirection.DOWNLINK;
		}

		// Otherwise make a guess based upon the count of time the IP has been
		// in a packet
		int srcCount = ipCountMap.get(source).intValue();
		int destCount = ipCountMap.get(dest).intValue();
		if (srcCount >= destCount) {
			this.localIPAddresses.add(source);
			return PacketDirection.UPLINK;
		} else {
			this.localIPAddresses.add(dest);
			return PacketDirection.DOWNLINK;
		}
	}

	/**
	 * ARO prototype logic for finding local IP address
	 */
	private boolean isLocal(InetAddress ipAddress) {

		if (ipAddress instanceof Inet4Address) {
			byte[] addr = ((Inet4Address) ipAddress).getAddress();
			return addr[0] == 10;
		}
		return false;
	}
	
	@Override
	public void packetArrived(String appName, Packet packet) {
		if (packet instanceof IPPacket) { // Replaces GetPacketInfo(...)
			IPPacket ipack = (IPPacket) packet;

			// no IP fragmentation
			if ((ipack.getIPVersion() == 4) && (ipack.getFragmentOffset() != 0)) {
				logger.warn("226 - no IP fragmentation");
			}

			addIpCount(ipack.getSourceIPAddress());
			addIpCount(ipack.getDestinationIPAddress());
		}
		allPackets.add(new PacketInfo(appName, packet));
	}
	
	/**
	 * Adds the IP count in ipCountMap list.
	 * 
	 * @param ipAddress
	 *            unique ip address.
	 */
	void addIpCount(InetAddress ipAddress) {
		Integer ipCount = this.ipCountMap.get(ipAddress);
		if (ipCount == null) {
			ipCount = Integer.valueOf(0);
		}
		int value = ipCount.intValue();
		ipCountMap.put(ipAddress, ++value);
	}
	
}//end class
