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
package com.att.aro.core.packetanalysis.pojo;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmInfo;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.peripheral.pojo.DeviceDetail;
import com.att.aro.core.peripheral.pojo.NetworkType;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.peripheral.pojo.WakelockInfo;
import com.att.aro.core.peripheral.pojo.WifiInfo;

/**
 *  Trace data from reading trace directory, which contains a pcap file.<br>
 *  Depending on the type of trace various other trace information may be recorded.
 *  
 *  <br>Potential trace data
 *  <pre>
 *    active_process      
 *    alarm_info_end      
 *    alarm_info_start    
 *    appid               
 *    appname             
 *    battery_events      
 *    batteryinfo_dump    
 *    bluetooth_events    
 *    camera_events       
 *    cpu                 
 *    datadump.csv        
 *    device_details      
 *    device_info         
 *    dmesg               
 *    gps_events          
 *    network_details     
 *    processed_events    
 *    prop                
 *    radio_events        
 *    screen_events       
 *    screen_rotations    
 *    time                
 *    traffic.cap         
 *    video.mov           
 *    video_time          
 *    wifi_events         
 *  </pre>
 * 
 */
public class TraceDirectoryResult extends AbstractTraceResult {
	
	/**
	 * from trace directory - screen_rotations
	 */
	private int screenRotationCounter = 0;
	
	/**
	 * Set of InetAddress
	 * <br>from trace directory - device_info
	 */
	private Set<InetAddress> localIPAddresses = null;

	// Alarm Info
	/**
	 * Epoch time in milliseconds from (trace directory) alarm_info_end/alarm_info_start files
	 */
	private double dumpsysEpochTimestamp;
	
	/**
	 * Elapsed time in milliseconds from (trace directory) alarm_info_end/alarm_info_start files
	 */
	private double dumpsysElapsedTimestamp; 
	
	/**
	 * from trace directory - dmesg
	 */
	private List<AlarmInfo> alarmInfos = null;
	
	/**
	 * from trace directorys - alarm_info_end or alarm_info_start if first file doesn't exist
	 */
	private List<AlarmAnalysisInfo> alarmStatisticsInfos = null;
	
	/**
	 * a Map of scheduled alarms parsed from alarmStatisticsInfos
	 */
	private Map<String, List<ScheduledAlarmInfo>> scheduledAlarms = null;

	/**
	 * App Version Info
	 * <br>from trace directory - appname
	 */
	private Map<String, String> appVersionMap = null;

	/**
	 * Wifi Info
	 * <br>from trace directory - wifi_events
	 */
	private List<WifiInfo> wifiInfos = null;

	/**
	 * Wakelock Info
	 * <br>from trace directory - batteryinfo_dump
	 */
	private List<WakelockInfo> wakelockInfos = null;

	/**
	 * Battery Info
	 * <br>from trace directory - battery_events
	 */
	private List<BatteryInfo> batteryInfos = null;

	/**
	 * Radio Info
	 * <br>from trace directory - radio_events
	 */
	private List<RadioInfo> radioInfos = null;

	/**
	 * from trace directory - network_details
	 */
	private List<NetworkBearerTypeInfo> networkTypeInfos = null;
	
	/**
	 * from trace directory - device_details
	 */
	private DeviceDetail deviceDetail;
	
	/**
	 * default screen size width<br>
	 * initially set to a default - updated with real size from trace directory
	 * - device_details
	 */
	private int deviceScreenSizeX = 480; //DEFAULT_SCREENSIZE_X;

	/**
	 * screen size height<br>
	 * initially set to a default - updated with real size from trace directory
	 * - device_details
	 */
	private int deviceScreenSizeY = 800; //DEFAULT_SCREENSIZE_Y;

	/**
	 * Event time in nanoseconds
	 * <br>from trace directory - time file line 3
	 */
	private double eventTime0;
	
	/**
	 * from trace directory - gps_events
	 */
	private double gpsActiveDuration;
	
	/**
	 * from trace directory - wifi_events
	 */	
	private double wifiActiveDuration;
	
	/**
	 * from trace directory - bluetooth_events
	 */
	private double bluetoothActiveDuration;
	
	/**
	 * from trace directory - camera_events
	 */
	private double cameraActiveDuration;

	/**
	 * List of trace files NOT found in trace directory.<br>
	 * Note: different collectors create different sets of trace files.
	 */
	private Set<String> missingFiles = null;
	
	/**
	 * from trace directory - network_details
	 */
	private List<NetworkType> networkTypesList = null;
	
	/**
	 * Total packets extracted from pcap file.
	 * <br>from trace directory - traffic.cap
	 */
	private int totalNoPackets = 0;

	
	
	
	/**
	 * from trace directory - device_details (7th line)
	 * <br> Note: No visible usage found
	 */
	private NetworkType networkType;


	/**
	 * Constructor, Initializes all TraceDirectoryResult objects.
	 */
	public TraceDirectoryResult() {
		super();
		screenRotationCounter = 0;
		localIPAddresses = new HashSet<InetAddress>(1);
		alarmInfos = new ArrayList<AlarmInfo>();
		alarmStatisticsInfos = new ArrayList<AlarmAnalysisInfo>();
		scheduledAlarms = new HashMap<String, List<ScheduledAlarmInfo>>();

		appVersionMap = new HashMap<String, String>();

		wifiInfos = new ArrayList<WifiInfo>();
		wakelockInfos = new ArrayList<WakelockInfo>();
		batteryInfos = new ArrayList<BatteryInfo>();
		radioInfos = new ArrayList<RadioInfo>();

		networkTypeInfos = new ArrayList<NetworkBearerTypeInfo>();
		networkType = null;

		this.deviceDetail = new DeviceDetail();

		deviceScreenSizeX = 480;
		deviceScreenSizeY = 800;
		eventTime0 = 0;

		gpsActiveDuration = 0;
		wifiActiveDuration = 0;
		bluetoothActiveDuration = 0;
		cameraActiveDuration = 0;
		missingFiles = new HashSet<String>();
		networkTypesList = new ArrayList<NetworkType>();
		totalNoPackets = 0;
	}

	/**
	 * @return screen rotations count
	 */
	public int getScreenRotationCounter() {
		return screenRotationCounter;
	}

	/**
	 * Set screen rotations count
	 * @param screenRotationCounter - screen rotations count
	 */
	public void setScreenRotationCounter(int screenRotationCounter) {
		this.screenRotationCounter = screenRotationCounter;
	}

	/**
	 * @return Set of InetAddress
	 */
	public Set<InetAddress> getLocalIPAddresses() {
		return localIPAddresses;
	}

	/**
	 * Store Set of InetAddress
	 * @param localIPAddresses - Set of InetAddress
	 */
	public void setLocalIPAddresses(Set<InetAddress> localIPAddresses) {
		this.localIPAddresses = localIPAddresses;
	}

	/**
	 * @return Epoch time in milliseconds
	 */
	public double getDumpsysEpochTimestamp() {
		return dumpsysEpochTimestamp;
	}

	/**
	 * Set Epoch time in milliseconds
	 * @param dumpsysEpochTimestamp - Epoch time in milliseconds
	 */
	public void setDumpsysEpochTimestamp(double dumpsysEpochTimestamp) {
		this.dumpsysEpochTimestamp = dumpsysEpochTimestamp;
	}

	/**
	 * @return Elapsed time in milliseconds
	 */
	public double getDumpsysElapsedTimestamp() {
		return dumpsysElapsedTimestamp;
	}

	/**
	 * Set Elapsed time in milliseconds
	 * @param dumpsysElapsedTimestamp - Elapsed time in milliseconds
	 */
	public void setDumpsysElapsedTimestamp(double dumpsysElapsedTimestamp) {
		this.dumpsysElapsedTimestamp = dumpsysElapsedTimestamp;
	}

	/**
	 * @return alarm info from dmesg
	 */
	public List<AlarmInfo> getAlarmInfos() {
		return alarmInfos;
	}

	/**
	 * Set alarm info from dmesg
	 * @param alarmInfos - alarm info from dmesg
	 */
	public void setAlarmInfos(List<AlarmInfo> alarmInfos) {
		this.alarmInfos = alarmInfos;
	}

	/**
	 * @return alarm statistics from alarm_info_end/alarm_info_start
	 */
	public List<AlarmAnalysisInfo> getAlarmStatisticsInfos() {
		return alarmStatisticsInfos;
	}

	/**
	 * Set alarm statistics from alarm_info_end/alarm_info_start
	 * @param alarmStatisticsInfos - alarm statistics from alarm_info_end/alarm_info_start
	 */
	public void setAlarmStatisticsInfos(List<AlarmAnalysisInfo> alarmStatisticsInfos) {
		this.alarmStatisticsInfos = alarmStatisticsInfos;
	}

	/**
	 * @return scheduled alarms - from alarm stats in alarm_info_end/alarm_info_start
	 */
	public Map<String, List<ScheduledAlarmInfo>> getScheduledAlarms() {
		return scheduledAlarms;
	}

	/**
	 * Set scheduled alarms - from alarm stats in alarm_info_end/alarm_info_start
	 * @param scheduledAlarms - scheduled alarms - from alarm stats in alarm_info_end/alarm_info_start
	 */
	public void setScheduledAlarms(Map<String, List<ScheduledAlarmInfo>> scheduledAlarms) {
		this.scheduledAlarms = scheduledAlarms;
	}

	/**
	 * @return Map of app versions
	 */
	public Map<String, String> getAppVersionMap() {
		return appVersionMap;
	}

	/**
	 * Set a Map of app version
	 * @param appVersionMap - a Map of app version
	 */
	public void setAppVersionMap(Map<String, String> appVersionMap) {
		this.appVersionMap = appVersionMap;
	}

	/**
	 * @return a List of WifiInfo
	 */
	public List<WifiInfo> getWifiInfos() {
		return wifiInfos;
	}

	/**
	 * Set a List of WifiInfo
	 * @param wifiInfos - a List of WifiInfo
	 */
	public void setWifiInfos(List<WifiInfo> wifiInfos) {
		this.wifiInfos = wifiInfos;
	}

	/**
	 * @return a List of WakelockInfo
	 */
	public List<WakelockInfo> getWakelockInfos() {
		return wakelockInfos;
	}

	/**
	 * Set a List of WakelockInfo
	 * @param wakelockInfos - a List of WakelockInfo
	 */
	public void setWakelockInfos(List<WakelockInfo> wakelockInfos) {
		this.wakelockInfos = wakelockInfos;
	}

	/**
	 * @return a List of BatteryInfo
	 */
	public List<BatteryInfo> getBatteryInfos() {
		return batteryInfos;
	}

	/**
	 * Set a List of BatteryInfo
	 * @param batteryInfos - a List of BatteryInfo
	 */
	public void setBatteryInfos(List<BatteryInfo> batteryInfos) {
		this.batteryInfos = batteryInfos;
	}

	/**
	 * @return a List of RadioInfo
	 */
	public List<RadioInfo> getRadioInfos() {
		return radioInfos;
	}

	/**
	 * Set a List of RadioInfo
	 * @param radioInfos - a List of RadioInfo
	 */
	public void setRadioInfos(List<RadioInfo> radioInfos) {
		this.radioInfos = radioInfos;
	}

	/**
	 * @return a List of NetworkBearerTypeInfo
	 */
	public List<NetworkBearerTypeInfo> getNetworkTypeInfos() {
		return networkTypeInfos;
	}

	/**
	 * Set a List of NetworkBearerTypeInfo
	 * @param networkTypeInfos - a List of NetworkBearerTypeInfo
	 */
	public void setNetworkTypeInfos(List<NetworkBearerTypeInfo> networkTypeInfos) {
		this.networkTypeInfos = networkTypeInfos;
	}

	/**
	 * @return network type
	 */
	public NetworkType getNetworkType() {
		return networkType;
	}

	/**
	 * Set network type
	 * @param networkType - network type
	 */
	public void setNetworkType(NetworkType networkType) {
		this.networkType = networkType;
	}

	/**
	 * @return device details - DeviceDetail
	 */
	public DeviceDetail getDeviceDetail() {
		return this.deviceDetail;
	}

	/**
	 * Set device details - DeviceDetail
	 * @param deviceDetail - device details
	 */
	public void setDeviceDetail(DeviceDetail deviceDetail) {
		this.deviceDetail = deviceDetail;
	}

	/**
	 * @return collector name from deviceDetail
	 */
	public String getCollectorName() {
		return this.deviceDetail.getCollectorName();
	}

	/**
	 * @return device model from deviceDetail
	 */
	public String getDeviceModel() {
		return this.deviceDetail.getDeviceModel();
	}

	/**
	 * @return device make from deviceDetail
	 */
	public String getDeviceMake() {
		return this.deviceDetail.getDeviceMake();
	}

	/**
	 * @return os type from deviceDetail ie. LGE
	 */
	public String getOsType() {
		return this.deviceDetail.getOsType();
	}

	/**
	 * @return os type from deviceDetail ie. android
	 */
	public String getOsVersion() {
		return this.deviceDetail.getOsVersion();
	}

	/**
	 * @return Version of collector 
	 */
	public String getCollectorVersion() {
		return this.deviceDetail.getCollectorVersion();
	}

	/**
	 * @return device screen width
	 */
	public int getDeviceScreenSizeX() {
		return deviceScreenSizeX;
	}

	/**
	 * Set device screen width
	 * @param deviceScreenSizeX - device screen width
	 */
	public void setDeviceScreenSizeX(int deviceScreenSizeX) {
		this.deviceScreenSizeX = deviceScreenSizeX;
	}

	/**
	 * @return device screen height
	 */
	public int getDeviceScreenSizeY() {
		return deviceScreenSizeY;
	}

	/**
	 * Set device screen height
	 * @param deviceScreenSizeY - device screen height
	 */
	public void setDeviceScreenSizeY(int deviceScreenSizeY) {
		this.deviceScreenSizeY = deviceScreenSizeY;
	}

	/**
	 * @return event time in nanoseconds
	 */
	public double getEventTime0() {
		return eventTime0;
	}

	/**
	 * Set event time in nanoseconds
	 * @param eventTime0 - event time in nanoseconds
	 */
	public void setEventTime0(double eventTime0) {
		this.eventTime0 = eventTime0;
	}

	/**
	 * @return gps activity duration
	 */
	public double getGpsActiveDuration() {
		return gpsActiveDuration;
	}

	/**
	 * Set gps activity duration
	 * @param gpsActiveDuration - gps activity duration
	 */
	public void setGpsActiveDuration(double gpsActiveDuration) {
		this.gpsActiveDuration = gpsActiveDuration;
	}

	/**
	 * @return wifi activity duration
	 */
	public double getWifiActiveDuration() {
		return wifiActiveDuration;
	}

	/**
	 * Set wifi activity duration
	 * @param wifiActiveDuration - wifi activity duration
	 */
	public void setWifiActiveDuration(double wifiActiveDuration) {
		this.wifiActiveDuration = wifiActiveDuration;
	}

	/**
	 * @return bluetooth  activity duration
	 */
	public double getBluetoothActiveDuration() {
		return bluetoothActiveDuration;
	}

	/**
	 * Set bluetooth  activity duration
	 * @param bluetoothActiveDuration - bluetooth  activity duration
	 */
	public void setBluetoothActiveDuration(double bluetoothActiveDuration) {
		this.bluetoothActiveDuration = bluetoothActiveDuration;
	}

	/**
	 * @return camera activity duration
	 */
	public double getCameraActiveDuration() {
		return cameraActiveDuration;
	}

	/**
	 * Set camera activity duration
	 * @param cameraActiveDuration - camera activity duration
	 */
	public void setCameraActiveDuration(double cameraActiveDuration) {
		this.cameraActiveDuration = cameraActiveDuration;
	}

	/**
	 * @return the Set of missing trace files
	 */
	public Set<String> getMissingFiles() {
		return missingFiles;
	}

	/**
	 * Set the Set of missing trace files
	 * @param missingFiles - Set of missing trace files
	 */
	public void setMissingFiles(Set<String> missingFiles) {
		this.missingFiles = missingFiles;
	}

	/**
	 * @return String of comma separated network types
	 */
	public String getNetworkTypesList() {
		if (networkTypesList != null && !networkTypesList.isEmpty()) {
			StringBuffer networksList = new StringBuffer();
			for (NetworkType networkType : networkTypesList) {

				networksList.append(networkType.toString());
				networksList.append(" , ");
			}
			return networksList.toString().substring(0, networksList.toString().lastIndexOf(","));
		} else {
			return "";
		}
	}

	/**
	 * Set a List of NetworkType
	 * @param networkTypesList - a List of NetworkType
	 */
	public void setNetworkTypesList(List<NetworkType> networkTypesList) {
		this.networkTypesList = networkTypesList;
	}

	/**
	 * @return total packets extracted from pcap file
	 */
	public int getTotalNoPackets() {
		return totalNoPackets;
	}

	/**
	 * Set total packets extracted from pcap file
	 * @param totalNoPackets - total packets extracted from pcap file
	 */
	public void setTotalNoPackets(int totalNoPackets) {
		this.totalNoPackets = totalNoPackets;
	}

	/**
	 * Return TraceResultType.TRACE_DIRECTORY to identify that this trace is
	 * collected from a directory
	 * 
	 * @return TraceResultType.TRACE_DIRECTORY
	 */
	@Override
	public TraceResultType getTraceResultType() {
		return TraceResultType.TRACE_DIRECTORY;
	}

}
