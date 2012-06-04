/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.BluetoothInfo.BluetoothState;
import com.att.aro.model.CameraInfo.CameraState;
import com.att.aro.model.GpsInfo.GpsState;
import com.att.aro.model.PacketInfo.Direction;
import com.att.aro.model.ScreenStateInfo.ScreenState;
import com.att.aro.model.UserEvent.UserEventType;
import com.att.aro.model.WifiInfo.WifiState;
import com.att.aro.pcap.IPPacket;
import com.att.aro.pcap.PCapAdapter;
import com.att.aro.pcap.Packet;
import com.att.aro.pcap.PacketListener;

/**
 * Encapsulates the trace data. Contains methods that parse the files in the
 * trace folder, and convert it to Lists of Data Analyzer bean objects.
 */
public class TraceData implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private static Pattern wifiPattern = Pattern
			.compile("\\S*\\s*\\S*\\s*(\\S*)\\s*(\\S*)\\s*(.*)");

	/**
	 * This class handles analysis of imported trace data
	 */
	public class Analysis implements Serializable {
		private static final long serialVersionUID = 1L;

		private class PacketCounter {
			private int packetCount;
			private long totalBytes;
			
			public synchronized void add(PacketInfo p) {
				totalBytes += p.getLen();
				++packetCount;
			}
		}
		
		// Configuration/profile/filter
		private Profile profile;
		private AnalysisFilter filter;
		
		// List of packets included in analysis (application filtered)
		private List<PacketInfo> packets;
		private Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();

		// Analysis results
		private Set<String> appNames = new HashSet<String>();
		private Collection<ApplicationPacketSummary> applicationPacketSummary = new ArrayList<ApplicationPacketSummary>();
		private Collection<IPPacketSummary> ipPacketSummary = new ArrayList<IPPacketSummary>();
		private long totalBytes = 0;
		private double packetsDuration = 0.0;
		private double avgKbps = 0.0;
		private List<TCPSession> tcpSessions;
		private RRCStateMachine rrcStateMachine;
		private CacheAnalysis cacheAnalysis;
		private BestPractices bestPractice;
		private EnergyModel energyModel;

		// List of Burst Collection Info
		private BurstCollectionAnalysis bcAnalysis;

		// CPU activity info
		private List<CpuActivity> cpuActivityList = new ArrayList<CpuActivity>();

		// Gps Info
		private List<GpsInfo> gpsInfos = new ArrayList<GpsInfo>();

		// Bluetooth Info
		private List<BluetoothInfo> bluetoothInfos = new ArrayList<BluetoothInfo>();

		// Wifi Info
		private List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();

		// Battery Info
		private List<BatteryInfo> batteryInfos = new ArrayList<BatteryInfo>();

		// Radio Info
		private List<RadioInfo> radioInfos = new ArrayList<RadioInfo>();

		// Camera Info
		private List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();

		// Screen State Info
		private List<ScreenStateInfo> screenStateInfos = new ArrayList<ScreenStateInfo>();

		// List of User Event Info
		private List<UserEvent> userEvents = new ArrayList<UserEvent>();
		private double gpsActiveDuration;
		private double wifiActiveDuration;
		private double bluetoothActiveDuration;
		private double cameraActiveDuration;

		/**
		 * Constructor
		 * 
		 * @param profile
		 *            The profile to be applied.
		 * @param selections
		 *            The applications/IP address selections.
		 * @param selectionType
		 *            The selection type i.e. Application or IP address.
		 */
		private Analysis(Profile profile, AnalysisFilter filter) throws IOException {

			this.profile = profile != null ? profile : new Profile3G();

			TimeRange timeRange = filter != null ? filter.getTimeRange() : null;
			if (timeRange != null) {
				double beginTime = timeRange.getBeginTime();
				double endTime = timeRange.getEndTime();
				this.cpuActivityList = getCpuInfosForTheTimeRange(
						TraceData.this.cpuActivityList, beginTime, endTime);
				this.gpsInfos = getGpsInfosForTheTimeRange(TraceData.this.gpsInfos,
						beginTime, endTime);
				this.bluetoothInfos = getBluetoothInfosForTheTimeRange(
						TraceData.this.bluetoothInfos, beginTime, endTime);
				this.wifiInfos = getWifiInfosForTheTimeRange(
						TraceData.this.wifiInfos, beginTime, endTime);
				this.batteryInfos = getBatteryInfosForTheTimeRange(
						TraceData.this.batteryInfos, beginTime, endTime);
				this.radioInfos = getRadioInfosForTheTimeRange(
						TraceData.this.radioInfos, beginTime, endTime);
				this.cameraInfos = getCameraInfosForTheTimeRange(
						TraceData.this.cameraInfos, beginTime, endTime);
				this.screenStateInfos = getScreenInfosForTheTimeRange(
						TraceData.this.screenStateInfos, beginTime, endTime);
				this.userEvents = getUserEventsForTheTimeRange(
						TraceData.this.userEvents, beginTime, endTime);
				
			} else {
				this.cpuActivityList = TraceData.this.cpuActivityList;
				this.gpsInfos = TraceData.this.gpsInfos;
				this.bluetoothInfos = TraceData.this.bluetoothInfos;
				this.wifiInfos = TraceData.this.wifiInfos;
				this.batteryInfos = TraceData.this.batteryInfos;
				this.radioInfos = TraceData.this.radioInfos;
				this.cameraInfos = TraceData.this.cameraInfos;
				this.screenStateInfos = TraceData.this.screenStateInfos;
				this.userEvents = TraceData.this.userEvents;
				this.bluetoothActiveDuration = TraceData.this.bluetoothActiveDuration;
				this.gpsActiveDuration = TraceData.this.gpsActiveDuration;
				this.cameraActiveDuration = TraceData.this.cameraActiveDuration;
				this.wifiActiveDuration = TraceData.this.wifiActiveDuration;
			}

			if (filter != null) {

				// Filter packets based upon selected app names
				packets = new ArrayList<PacketInfo>();
				for (PacketInfo packet : TraceData.this.allPackets) {
					
					// Check time range
					double timestamp = packet.getTimeStamp();
					if (timeRange != null && (timeRange.getBeginTime() > timestamp || timeRange.getEndTime() < timestamp)) {

						// Not in time range
						continue;
					}
					
					// Check to see if application is selected
					if (filter.getPacketColor(packet) == null) {
						
						// App unknown by filter
						continue;
					}

					packets.add(packet);
				}
			} else {

				// No filter. Use all packets
				packets = TraceData.this.allPackets;
			}

			this.filter = filter != null ? new AnalysisFilter(filter) : new AnalysisFilter(TraceData.this);
			runAnalysis();
		}

		/**
		 * Performs a TimeRangeAnalysis on the trace data.
		 * 
		 * @return TimeRangeAnalysis The object containing TimeRangeAnalysis
		 *         data.
		 */
		public TimeRangeAnalysis performTimeRangeAnalysis(
				double analyzeBeginTime, double analyzeEndTime) {
			List<RrcStateRange> rrcCollection = this.rrcStateMachine
					.getRRcStateRanges();
			long payloadLength = 0;
			long totalBytes = 0;
			int n = packets.size();

			for (int i = 0; i < n; i++) {
				PacketInfo p = packets.get(i);
				if (p.getTimeStamp() >= analyzeBeginTime
						&& p.getTimeStamp() <= analyzeEndTime) {
					payloadLength += p.getPayloadLen();
					totalBytes += p.getLen();
				}
			}

			double energy = 0.0f;
			double activeTime = 0.0f;
			int m = rrcCollection.size();

			for (int i = 0; i < m; i++) {
				double beginTime;
				double endTime;

				RrcStateRange rrc = rrcCollection.get(i);
				if (rrc.getEndTime() < analyzeBeginTime) {
					continue;
				}
				if (rrc.getBeginTime() > analyzeEndTime) {
					continue;
				}

				if (rrc.getBeginTime() >= analyzeBeginTime) {
					beginTime = rrc.getBeginTime();
				} else {
					beginTime = analyzeBeginTime;
				}

				if (rrc.getEndTime() <= analyzeEndTime) {
					endTime = rrc.getEndTime();
				} else {
					endTime = analyzeEndTime;
				}

				RRCState s = rrc.getState();

				energy += profile.energy(beginTime, endTime, s, packets);
				if (profile.getProfileType() == ProfileType.LTE) {
					if (s == RRCState.LTE_CONTINUOUS
							|| s == RRCState.LTE_CR_TAIL) {
						activeTime += endTime - beginTime;
					}
				} else {
					if (s == RRCState.STATE_DCH || s == RRCState.TAIL_DCH) {
						activeTime += endTime - beginTime;
					}
				}
			}

			return new TimeRangeAnalysis(analyzeBeginTime, analyzeEndTime,
					totalBytes, payloadLength, activeTime, energy);
		}

		/**
		 * Returns the GPS information.
		 * 
		 * @return A List of GPSInfo objects containing the information.
		 */
		public List<GpsInfo> getGpsInfos() {
			return Collections.unmodifiableList(gpsInfos);
		}

		/**
		 * Returns the Bluetooth information.
		 * 
		 * @return A List of BluetoothInfo objects containing the information.
		 */
		public List<BluetoothInfo> getBluetoothInfos() {
			return Collections.unmodifiableList(bluetoothInfos);
		}

		/**
		 * Returns the WiFi information.
		 * 
		 * @return A List of WiFiInfo objects containing the information.
		 */
		public List<WifiInfo> getWifiInfos() {
			return Collections.unmodifiableList(wifiInfos);
		}

		/**
		 * Returns the camera information.
		 * 
		 * @return A List of CameraInfo objects containing the information.
		 */
		public List<CameraInfo> getCameraInfos() {
			return Collections.unmodifiableList(cameraInfos);
		}

		/**
		 * Returns the screen state information.
		 * 
		 * @return A List of ScreenStateInfo objects containing the information.
		 */
		public List<ScreenStateInfo> getScreenStateInfos() {
			return Collections.unmodifiableList(screenStateInfos);
		}

		/**
		 * Returns the battery information.
		 * 
		 * @return A List of BatteryInfo objects containing the information.
		 */
		public List<BatteryInfo> getBatteryInfos() {
			return Collections.unmodifiableList(batteryInfos);
		}

		/**
		 * Returns the user events that occurred during the trace.
		 * 
		 * @return A List of UserEvent objects containing the user generated
		 *         events.
		 */
		public List<UserEvent> getUserEvents() {
			return Collections.unmodifiableList(userEvents);
		}

		/**
		 * Returns the radio information.
		 * 
		 * @return A List of RadioInfo objects containing the information.
		 */
		public List<RadioInfo> getRadioInfos() {
			return Collections.unmodifiableList(radioInfos);
		}

		/**
		 * Returns the cpu activity information.
		 * 
		 * @return A List of CpuActivity objects containing the information.
		 */
		public List<CpuActivity> getCpuActivityList() {
			return Collections.unmodifiableList(cpuActivityList);
		}

		/**
		 * Returns the trace for which this analysis was run The trace data
		 * object.
		 */
		public TraceData getTraceData() {
			return TraceData.this;
		}

		public void clear() {
			for (PacketInfo p : allPackets) {
				p.setBurst(null);
				p.setRequestResponseInfo(null);
				p.setSession(null);
				p.setStateMachine(null);
				p.setTcpInfo(null);
			}
		}

		/**
		 * Returns the profile associated with the trace data.
		 * 
		 * @return The profile
		 */
		public Profile getProfile() {
			return profile;
		}

		/**
		 * Returns the filter settings used for this analysis
		 * @return
		 */
		public AnalysisFilter getFilter() {
			
			// Returns a copy to prevent changes
			return new AnalysisFilter(filter);
		}
		
		/**
		 * Returns the list of packets associated with the trace data.
		 * 
		 * @return The list of packets.
		 */
		public List<PacketInfo> getPackets() {
			return Collections.unmodifiableList(packets);
		}

		/**
		 * @return The packetSizeToCountMap
		 */
		public Map<Integer, Integer> getPacketSizeToCountMap() {
			return Collections.unmodifiableMap(packetSizeToCountMap);
		}

		/**
		 * @return the applicationPacketSummary
		 */
		public Collection<ApplicationPacketSummary> getApplicationPacketSummary() {
			return Collections.unmodifiableCollection(applicationPacketSummary);
		}

		/**
		 * @return the ipPacketSummary
		 */
		public Collection<IPPacketSummary> getIpPacketSummary() {
			return Collections.unmodifiableCollection(ipPacketSummary);
		}

		/**
		 * @return The tcpSessions
		 */
		public List<TCPSession> getTcpSessions() {
			return Collections.unmodifiableList(tcpSessions);
		}

		/**
		 * @return The rrcStateMachine
		 */
		public RRCStateMachine getRrcStateMachine() {
			return rrcStateMachine;
		}

		/**
		 * @return The cacheAnalysis
		 */
		public CacheAnalysis getCacheAnalysis() {
			return cacheAnalysis;
		}

		/**
		 * @return The bestPractice
		 */
		public BestPractices getBestPractice() {
			return bestPractice;
		}

		/**
		 * @return The energyModel
		 */
		public EnergyModel getEnergyModel() {
			return energyModel;
		}

		/**
		 * Returns the list of burst infos found in the trace data analysis.
		 * 
		 * @return The list of burst infos.
		 */
		public List<Burst> getBurstInfos() {
			return bcAnalysis.getBurstCollection();
		}

		/**
		 * @return The bcAnalysis
		 */
		public BurstCollectionAnalysis getBcAnalysis() {
			return bcAnalysis;
		}

		/**
		 * Returns the names of the apps that contributed to network traffic
		 * in the analysis
		 * @return the app names
		 */
		public Set<String> getAppNames() {
			return Collections.unmodifiableSet(appNames);
		}

		/**
		 * Returns the total number of bytes analyzed
		 * 
		 * @return The bytes
		 */
		public long getTotalBytes() {
			return totalBytes;
		}

		/**
		 * Returns the duration of time from the first packet to the last
		 * 
		 * @return The packetsDuration
		 */
		public double getPacketsDuration() {
			return packetsDuration;
		}

		/**
		 * Returns the average rate of data transfer in kilobits per second
		 * 
		 * @return The avgKbps
		 */
		public double getAvgKbps() {
			return avgKbps;
		}

		/**
		 * Runs the basic analysis on the trace data using the current
		 * configuration after the applications/ip addresses selections are
		 * made.
		 * 
		 * @throws IOException
		 */
		private synchronized void runAnalysis() throws IOException {

			// Collect basic statistics
			if (packets.size() > 0) {
				PacketInfo lastPacket = packets.get(packets.size() - 1);
				Map<String, PacketCounter> appPackets = new HashMap<String, PacketCounter>();
				Map<InetAddress, PacketCounter> ipPackets = new HashMap<InetAddress, PacketCounter>();
				for (PacketInfo packet : packets) {
					totalBytes += packet.getLen();
					
					String appName = packet.getAppName();
					appNames.add(appName);
					PacketCounter pc = appPackets.get(appName);
					if (pc == null) {
						pc = new PacketCounter();
						appPackets.put(appName, pc);
					}
					pc.add(packet);

					if (packet.getPacket() instanceof IPPacket) {

						// Count packets by packet size
						Integer packetSize = packet.getPayloadLen();

						Integer iValue = packetSizeToCountMap.get(packetSize);
						if (iValue == null) {
							iValue = 1;
						} else {
							iValue++;
						}
						packetSizeToCountMap.put(packetSize, iValue);

						// Get IP address summary
						InetAddress ip = packet.getRemoteIPAddress();
						pc = ipPackets.get(ip);
						if (pc == null) {
							pc = new PacketCounter();
							ipPackets.put(ip, pc);
						}
						pc.add(packet);
					}
				}
				for (Map.Entry<InetAddress, PacketCounter> m : ipPackets.entrySet()) {
					ipPacketSummary.add(new IPPacketSummary(m.getKey(), m.getValue().packetCount, m.getValue().totalBytes));
				}
				for (Map.Entry<String, PacketCounter> m : appPackets.entrySet()) {
					applicationPacketSummary.add(new ApplicationPacketSummary(m.getKey(), m.getValue().packetCount, m.getValue().totalBytes));
				}

				packetsDuration = lastPacket.getTimeStamp()
						- packets.get(0).getTimeStamp();
				avgKbps = packetsDuration != 0 ? totalBytes * 8.0 / 1000.0 / packetsDuration : 0.0;
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("===== Basic Statistics =====");
					logger.fine("Size = : " + getTotalBytes());
					logger.fine("Duration = : " + getPacketsDuration());
					logger.fine("Packets = : " + getPackets().size());
					logger.fine("Avg Rate = : " + getAvgKbps() + " kbps");
					logger.fine("");
				}
			}
			// Analyze packets for TCP sessions
			this.tcpSessions = TCPSession.extractTCPSessions(packets);

			// Do cache analysis
			this.cacheAnalysis = new CacheAnalysis(this.tcpSessions);

			// Simulate RRC state machine
			this.rrcStateMachine = new RRCStateMachine(this);

			// Create energy model
			this.energyModel = new EnergyModel(this);

			// Burst Analysis
			this.bcAnalysis = new BurstCollectionAnalysis(this);

			// Creates BestPractices object
			this.bestPractice = new BestPractices(this);
		}

		/**
		 * Returns the list of user events filtered based on the time range.
		 */

		private List<UserEvent> getUserEventsForTheTimeRange(
				List<UserEvent> userEvents, double beginTime, double endTime) {
			List<UserEvent> filteredUserEvents = new ArrayList<UserEvent>();
			for (UserEvent userEvent : userEvents) {
				if (userEvent.getPressTime() >= beginTime
						&& userEvent.getReleaseTime() <= endTime) {

					filteredUserEvents.add(userEvent);
				}
			}
			return filteredUserEvents;
		}

		/**
		 * Returns the list of screen events filtered based on the time range.
		 */

		private List<ScreenStateInfo> getScreenInfosForTheTimeRange(
				List<ScreenStateInfo> screenStateInfos, double beginTime,
				double endTime) {

			List<ScreenStateInfo> filteredScreenStateInfos = new ArrayList<ScreenStateInfo>();
			for (ScreenStateInfo screenStateInfo : screenStateInfos) {

				if (screenStateInfo.getBeginTimeStamp() >= beginTime
						&& screenStateInfo.getEndTimeStamp() <= endTime) {
					filteredScreenStateInfos.add(screenStateInfo);
				} else if (screenStateInfo.getBeginTimeStamp() <= beginTime
						&& screenStateInfo.getEndTimeStamp() <= endTime && screenStateInfo.getEndTimeStamp() > beginTime) {
					filteredScreenStateInfos.add(new ScreenStateInfo(beginTime,
							screenStateInfo.getEndTimeStamp(), screenStateInfo
									.getScreenState(), screenStateInfo
									.getScreenBrightness(), screenStateInfo
									.getScreenTimeout()));
				} else if (screenStateInfo.getBeginTimeStamp() <= beginTime
						&& screenStateInfo.getEndTimeStamp() >= endTime) {
					filteredScreenStateInfos.add(new ScreenStateInfo(beginTime,
							endTime, screenStateInfo.getScreenState(),
							screenStateInfo.getScreenBrightness(),
							screenStateInfo.getScreenTimeout()));
				} else if (screenStateInfo.getBeginTimeStamp() >= beginTime && screenStateInfo.getBeginTimeStamp() < endTime
						&& screenStateInfo.getEndTimeStamp() >= endTime) {
					filteredScreenStateInfos.add(new ScreenStateInfo(
							screenStateInfo.getBeginTimeStamp(), endTime,
							screenStateInfo.getScreenState(), screenStateInfo
									.getScreenBrightness(), screenStateInfo
									.getScreenTimeout()));
				}
			}
			return filteredScreenStateInfos;
		}

		/**
		 * Returns the list of camera events filtered based on the time range.
		 */
		private List<CameraInfo> getCameraInfosForTheTimeRange(
				List<CameraInfo> cameraInfos, double beginTime, double endTime) {

			List<CameraInfo> filteredCameraInfos = new ArrayList<CameraInfo>();
			CameraInfo filteredCameraInfo = null;
			for (CameraInfo cameraInfo : cameraInfos) {

				if (cameraInfo.getBeginTimeStamp() >= beginTime
						&& cameraInfo.getEndTimeStamp() <= endTime) {
					filteredCameraInfo = cameraInfo;
					filteredCameraInfos.add(filteredCameraInfo);
					if(filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON){
						this.cameraActiveDuration += filteredCameraInfo.getEndTimeStamp() - filteredCameraInfo.getBeginTimeStamp();
					}
				} else if (cameraInfo.getBeginTimeStamp() <= beginTime
						&& cameraInfo.getEndTimeStamp() <= endTime && cameraInfo.getEndTimeStamp() > beginTime) {
					filteredCameraInfo = new CameraInfo(beginTime,
							cameraInfo.getEndTimeStamp(), cameraInfo
							.getCameraState());
					filteredCameraInfos.add(filteredCameraInfo);
					if(filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON){
						this.cameraActiveDuration += filteredCameraInfo.getEndTimeStamp() - filteredCameraInfo.getBeginTimeStamp();
					}
				} else if (cameraInfo.getBeginTimeStamp() <= beginTime
						&& cameraInfo.getEndTimeStamp() >= endTime) {
					filteredCameraInfo = new CameraInfo(beginTime, endTime,
							cameraInfo.getCameraState());
					filteredCameraInfos.add(filteredCameraInfo);
					if(filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON){
						this.cameraActiveDuration += filteredCameraInfo.getEndTimeStamp() - filteredCameraInfo.getBeginTimeStamp();
					}
				} else if (cameraInfo.getBeginTimeStamp() >= beginTime && cameraInfo.getBeginTimeStamp() < endTime
						&& cameraInfo.getEndTimeStamp() >= endTime) {
					filteredCameraInfo = new CameraInfo(cameraInfo
							.getBeginTimeStamp(), endTime, cameraInfo
							.getCameraState());
					filteredCameraInfos.add(filteredCameraInfo);
					if(filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON){
						this.cameraActiveDuration += filteredCameraInfo.getEndTimeStamp() - filteredCameraInfo.getBeginTimeStamp();
					}
				}
			}
			return filteredCameraInfos;
		}

		/**
		 * Returns the list of radio events filtered based on the time range.
		 */

		private List<RadioInfo> getRadioInfosForTheTimeRange(
				List<RadioInfo> radioInfos, double beginTime, double endTime) {

			List<RadioInfo> filteredRadioInfos = new ArrayList<RadioInfo>();
			for (RadioInfo radioInfo : radioInfos) {

				if (radioInfo.getTimeStamp() >= beginTime
						&& radioInfo.getTimeStamp() <= endTime) {
					filteredRadioInfos.add(radioInfo);
				}
			}
			return filteredRadioInfos;
		}

		/**
		 * Returns the list of battery events filtered based on the time range.
		 */
		private List<BatteryInfo> getBatteryInfosForTheTimeRange(
				List<BatteryInfo> batteryInfos, double beginTime, double endTime) {

			List<BatteryInfo> filteredBatteryInfos = new ArrayList<BatteryInfo>();
			for (BatteryInfo batteryInfo : batteryInfos) {

				if (batteryInfo.getBatteryTimeStamp() >= beginTime
						&& batteryInfo.getBatteryTimeStamp() <= endTime) {
					filteredBatteryInfos.add(batteryInfo);
				}
			}
			return filteredBatteryInfos;
		}

		/**
		 * Returns the list of wifi events filtered based on the time range.
		 */
		private List<WifiInfo> getWifiInfosForTheTimeRange(
				List<WifiInfo> wifiInfos, double beginTime, double endTime) {

			List<WifiInfo> filteredWifiInfos = new ArrayList<WifiInfo>();
			for (WifiInfo wifiInfo : wifiInfos) {

				if (wifiInfo.getBeginTimeStamp() >= beginTime
						&& wifiInfo.getEndTimeStamp() <= endTime) {
					filteredWifiInfos.add(wifiInfo);
				} else if (wifiInfo.getBeginTimeStamp() <= beginTime
						&& wifiInfo.getEndTimeStamp() <= endTime && wifiInfo.getEndTimeStamp() > beginTime) {
					filteredWifiInfos.add(new WifiInfo(beginTime, wifiInfo
							.getEndTimeStamp(), wifiInfo.getWifiState(),
							wifiInfo.getWifiMacAddress(), wifiInfo
									.getWifiRSSI(), wifiInfo.getWifiSSID()));
				} else if (wifiInfo.getBeginTimeStamp() <= beginTime
						&& wifiInfo.getEndTimeStamp() >= endTime) {
					filteredWifiInfos.add(new WifiInfo(beginTime, endTime,
							wifiInfo.getWifiState(), wifiInfo
									.getWifiMacAddress(), wifiInfo
									.getWifiRSSI(), wifiInfo.getWifiSSID()));
				} else if (wifiInfo.getBeginTimeStamp() >= beginTime && wifiInfo.getBeginTimeStamp() < endTime
						&& wifiInfo.getEndTimeStamp() >= endTime) {
					filteredWifiInfos.add(new WifiInfo(wifiInfo
							.getBeginTimeStamp(), endTime, wifiInfo
							.getWifiState(), wifiInfo.getWifiMacAddress(),
							wifiInfo.getWifiRSSI(), wifiInfo.getWifiSSID()));
				}
			}
			return filteredWifiInfos;
		}

		/**
		 * Returns the list of bluetooth events filtered based on the time
		 * range.
		 */
		private List<BluetoothInfo> getBluetoothInfosForTheTimeRange(
				List<BluetoothInfo> bluetoothInfos, double beginTime,
				double endTime) {

			List<BluetoothInfo> filteredBluetoothInfos = new ArrayList<BluetoothInfo>();
			
			BluetoothInfo filteredBluetoothInfo = null;
			
			for (BluetoothInfo bluetoothInfo : bluetoothInfos) {

				if (bluetoothInfo.getBeginTimeStamp() >= beginTime
						&& bluetoothInfo.getEndTimeStamp() <= endTime) {
					filteredBluetoothInfo = bluetoothInfo;
					filteredBluetoothInfos.add(filteredBluetoothInfo);
					if(filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED){
						this.bluetoothActiveDuration += filteredBluetoothInfo.getEndTimeStamp() - filteredBluetoothInfo.getBeginTimeStamp();
					}
				} else if (bluetoothInfo.getBeginTimeStamp() <= beginTime
						&& bluetoothInfo.getEndTimeStamp() <= endTime && bluetoothInfo.getEndTimeStamp() > beginTime) {
					filteredBluetoothInfo = new BluetoothInfo(beginTime,
							bluetoothInfo.getEndTimeStamp(), bluetoothInfo
							.getBluetoothState()); 
					filteredBluetoothInfos.add(filteredBluetoothInfo);
					if(filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED){
						this.bluetoothActiveDuration += filteredBluetoothInfo.getEndTimeStamp() - filteredBluetoothInfo.getBeginTimeStamp();
					}
				} else if (bluetoothInfo.getBeginTimeStamp() <= beginTime
						&& bluetoothInfo.getEndTimeStamp() >= endTime) {
					filteredBluetoothInfo = new BluetoothInfo(beginTime,
							endTime, bluetoothInfo.getBluetoothState());
					filteredBluetoothInfos.add(filteredBluetoothInfo);
					if(filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED){
						this.bluetoothActiveDuration += filteredBluetoothInfo.getEndTimeStamp() - filteredBluetoothInfo.getBeginTimeStamp();
					}
				} else if (bluetoothInfo.getBeginTimeStamp() >= beginTime && bluetoothInfo.getBeginTimeStamp() < endTime
						&& bluetoothInfo.getEndTimeStamp() >= endTime) {
					filteredBluetoothInfo = new BluetoothInfo(bluetoothInfo
							.getBeginTimeStamp(), endTime, bluetoothInfo
							.getBluetoothState());
					filteredBluetoothInfos.add(filteredBluetoothInfo);
					if(filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED){
						this.bluetoothActiveDuration += filteredBluetoothInfo.getEndTimeStamp() - filteredBluetoothInfo.getBeginTimeStamp();
					}
				}
			}

			return filteredBluetoothInfos;
		}

		/**
		 * Returns the list of gps events filtered based on the time range.
		 */
		private List<GpsInfo> getGpsInfosForTheTimeRange(
				List<GpsInfo> gpsInfos, double beginTime, double endTime) {

			List<GpsInfo> filteredGpsInfos = new ArrayList<GpsInfo>();
			
			GpsInfo filteredGpsInfo = null;

			for (GpsInfo gpsInfo : gpsInfos) {

				if (gpsInfo.getBeginTimeStamp() >= beginTime
						&& gpsInfo.getEndTimeStamp() <= endTime) {
					filteredGpsInfo = gpsInfo;
					filteredGpsInfos.add(filteredGpsInfo);
				if(filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE){
					this.gpsActiveDuration += filteredGpsInfo.getEndTimeStamp() - filteredGpsInfo.getBeginTimeStamp();
				}
					
				} else if (gpsInfo.getBeginTimeStamp() <= beginTime
						&& gpsInfo.getEndTimeStamp() <= endTime && gpsInfo.getEndTimeStamp() > beginTime) {
					filteredGpsInfo = new GpsInfo(beginTime, gpsInfo
							.getEndTimeStamp(), gpsInfo.getGpsState());
					filteredGpsInfos.add(filteredGpsInfo);
				if(filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE){
					this.gpsActiveDuration += filteredGpsInfo.getEndTimeStamp() - filteredGpsInfo.getBeginTimeStamp();
				}
				} else if (gpsInfo.getBeginTimeStamp() <= beginTime
						&& gpsInfo.getEndTimeStamp() >= endTime) {
					filteredGpsInfo = new GpsInfo(beginTime, endTime,
							gpsInfo.getGpsState());
					filteredGpsInfos.add(filteredGpsInfo);
				if(filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE){
					this.gpsActiveDuration += filteredGpsInfo.getEndTimeStamp() - filteredGpsInfo.getBeginTimeStamp();
				}
				} else if (gpsInfo.getBeginTimeStamp() >= beginTime && gpsInfo.getBeginTimeStamp() < endTime
						&& gpsInfo.getEndTimeStamp() >= endTime ) {
					 filteredGpsInfo = new GpsInfo(gpsInfo
							.getBeginTimeStamp(), endTime, gpsInfo
							.getGpsState());
					filteredGpsInfos.add(filteredGpsInfo);
				if(filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE){
					this.gpsActiveDuration += filteredGpsInfo.getEndTimeStamp() - filteredGpsInfo.getBeginTimeStamp();
				}
					
			}
			}
			
			return filteredGpsInfos;
		}

		/**
		 * Returns the list of cpu events filtered based on the time range.
		 */
		private List<CpuActivity> getCpuInfosForTheTimeRange(
				List<CpuActivity> cpuActivityList, double beginTime,
				double endTime) {

			List<CpuActivity> filteredCpuActivities = new ArrayList<CpuActivity>();
			for (CpuActivity cpuActivity : cpuActivityList) {

				if (cpuActivity.getBeginTimeStamp() >= beginTime
						&& cpuActivity.getEndTimeStamp() <= endTime) {
					filteredCpuActivities.add(cpuActivity);
				} else if (cpuActivity.getBeginTimeStamp() <= beginTime
						&& cpuActivity.getEndTimeStamp() <= endTime && cpuActivity.getEndTimeStamp() > beginTime) {
					filteredCpuActivities.add(new CpuActivity(beginTime,
							cpuActivity.getEndTimeStamp(), cpuActivity
									.getUsage()));
				} else if (cpuActivity.getBeginTimeStamp() <= beginTime
						&& cpuActivity.getEndTimeStamp() >= endTime) {
					filteredCpuActivities.add(new CpuActivity(beginTime,
							endTime, cpuActivity.getUsage()));
				} else if (cpuActivity.getBeginTimeStamp() >= beginTime && cpuActivity.getBeginTimeStamp() < endTime
						&& cpuActivity.getEndTimeStamp() >= endTime) {
					filteredCpuActivities.add(new CpuActivity(cpuActivity
							.getBeginTimeStamp(), endTime, cpuActivity
							.getUsage()));
				}
			}
			return filteredCpuActivities;
		}
		
		/**
		 * Returns the total amount of time that the GPS peripheral was in an active
		 * state.
		 * 
		 * @return The GPS active duration.
		 */
		public double getGPSActiveDuration() {
			return gpsActiveDuration;
		}

		/**
		 * Returns the total amount of time that the WiFi peripheral was in an
		 * active state.
		 * 
		 * @return The WiFi active duration.
		 */
		public double getWiFiActiveDuration() {
			return wifiActiveDuration;
		}

		/**
		 * Returns the total amount of time that the Bluetooth peripheral was in an
		 * active state.
		 * 
		 * @return The Bluetooth active duration.
		 */
		public double getBluetoothActiveDuration() {
			return bluetoothActiveDuration;
		}

		/**
		 * Returns the total amount of time that the camera peripheral was in an
		 * active state.
		 * 
		 * @return The camera active duration.
		 */
		public double getCameraActiveDuration() {
			return cameraActiveDuration;
		}


	}

	/**
	 * Private utility class
	 */
	public static class Times {
		private Double startTime;
		private Double eventTime;
		private Double duration;

		/**
		 * @return the startTime
		 */
		public Double getStartTime() {
			return startTime;
		}

		/**
		 * @return the eventTime
		 */
		public Double getEventTime() {
			return eventTime;
		}

		/**
		 * @return the duration
		 */
		public Double getDuration() {
			return duration;
		}

	}
	
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(TraceData.class.getName());

	/**
	 * The name of the AppName file
	 */
	public static final String APPNAME_FILE = "appname";

	/**
	 * The name of the AppId file
	 */
	public static final String APPID_FILE = "appid";

	/**
	 * The name of the Cpu file
	 */
	public static final String CPU_FILE = "cpu";

	/**
	 * The name of the Time file
	 */
	public static final String TIME_FILE = "time";

	/**
	 * The name of the pcap file
	 */
	public static final String PCAP_FILE = "traffic.cap";

	/**
	 * The name of the device_info file
	 */
	public static final String DEVICEINFO_FILE = "device_info";

	/**
	 * The name of the device_info file
	 */
	public static final String DEVICEDETAILS_FILE = "device_details";

	/**
	 * The name of the GPS file
	 */
	public static final String GPS_FILE = "gps_events";

	/**
	 * The name of the Bluetooth file
	 */
	public static final String BLUETOOTH_FILE = "bluetooth_events";

	/**
	 * The name of the Camera file
	 */
	public static final String CAMERA_FILE = "camera_events";

	/**
	 * The name of the Screen file
	 */
	public static final String SCREEN_STATE_FILE = "screen_events";

	/**
	 * The name of the Battery file
	 */
	public static final String BATTERY_FILE = "battery_events";

	/**
	 * The name of the Wifi file
	 */
	public static final String WIFI_FILE = "wifi_events";

	/**
	 * The name of the user events trace file
	 */
	public static final String USER_EVENTS_FILE = "processed_events";
	/**
	 * The name of the screen rotations trace file
	 */
	public static final String SCREEN_ROTATIONS_FILE = "screen_rotations";

	/**
	 * The name of the radio events trace file
	 */
	public static final String RADIO_EVENTS_FILE = "radio_events";

	/**
	 * The name of the video time file
	 */
	public static final String VIDEO_TIME_FILE = "video_time";

	/**
	 * The name of the video MOV file
	 */
	public static final String VIDEO_MOV_FILE = "video.mov";

	/**
	 * The name of the video MP4 file
	 */
	public static final String VIDEO_MP4_FILE = "video.mp4";

	// User event keywords
	private static final String PRESS = "press";
	private static final String RELEASE = "release";
	private static final String SCREEN = "screen";
	private static final String KEY = "key";
	private static final String KEY_POWER = "power";
	private static final String KEY_VOLUP = "volup";
	private static final String KEY_VOLDOWN = "voldown";
	private static final String KEY_BALL = "ball";
	private static final String KEY_HOME = "home";
	private static final String KEY_MENU = "menu";
	private static final String KEY_BACK = "back";
	private static final String KEY_SEARCH = "search";
	private static final String KEY_GREEN = "green";
	private static final String KEY_RED = "red";
	private static final String KEY_KEY = "key";
	private static final String KEY_LANDSCAPE = "landscape";
	private static final String KEY_PORTRAIT = "portrait";

	// GPS State keywords
	private static final String GPS_DISABLED = "OFF";
	private static final String GPS_ACTIVE = "ACTIVE";
	private static final String GPS_STANDBY = "STANDBY";

	// Camera State keywords
	private static final String CAMERA_OFF = "OFF";
	private static final String CAMERA_ON = "ON";

	// WiFi State keywords
	private static final String WIFI_OFF = "OFF";
	private static final String WIFI_CONNECTED = "CONNECTED";
	private static final String WIFI_DISCONNECTED = "DISCONNECTED";
	private static final String WIFI_CONNECTING = "CONNECTING";
	private static final String WIFI_DISCONNECTING = "DISCONNECTING";
	private static final String WIFI_SUSPENDED = "SUSPENDED";

	// Bluetooth State keywords
	private static final String BLUETOOTH_OFF = "OFF";
	private static final String BLUETOOTH_CONNECTED = "CONNECTED";
	private static final String BLUETOOTH_DISCONNECTED = "DISCONNECTED";

	// Screen State keywords
	private static final String SCREEN_OFF = "OFF";
	private static final String SCREEN_ON = "ON";

	private static final int PACKET_UNKNOWN_APP = -1;
	private static final int PACKET_EOF = -127;

	// Trace network types
	private static final int WIFI = -1;
	private static final int GPRS = 1;
	private static final int UMTS = 3;
	private static final int HSDPA = 8;
	private static final int HSUPA = 9;
	private static final int HSPA = 10;
	private static final int HSPAP = 15;
	private static final int LTE = 13;

	
	/**
	 * Utility method that will just read the trace time file info for the specified trace
	 * directory
	 * @param traceDirectory The trace directory
	 * @return Times read from the TIME file
	 * @throws IOException
	 */
	public static Times readTimes(File traceDirectory) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File(traceDirectory, TIME_FILE)));
		Times result = new Times();
		try {
			String s;

			// Ignore first line
			br.readLine();

			// Second line is pcap time
			s = br.readLine();
			if (s != null) {
				result.startTime = Double.valueOf(s);

				s = br.readLine();
				if (s != null) {
					result.eventTime = Double.parseDouble(s) / 1000.0;
				}

				s = br.readLine();
				if (s != null) {
					result.duration = Double.valueOf(Double.parseDouble(s)
							- result.startTime.doubleValue());
				}
			}
		} finally {
			br.close();
		}
		
		return result;
	}
	
	private double videoStartTime;

	private File traceDir;

	private Set<InetAddress> localIPAddresses = new HashSet<InetAddress>(1);

	// App Info
	private List<String> appInfos = new ArrayList<String>();

	// App Version Info
	private Map<String, String> appVersionMap = new HashMap<String, String>();

	// CPU activity info
	private List<CpuActivity> cpuActivityList = new ArrayList<CpuActivity>();

	// Gps Info
	private List<GpsInfo> gpsInfos = new ArrayList<GpsInfo>();

	// Bluetooth Info
	private List<BluetoothInfo> bluetoothInfos = new ArrayList<BluetoothInfo>();

	// Wifi Info
	private List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();

	// Battery Info
	private List<BatteryInfo> batteryInfos = new ArrayList<BatteryInfo>();

	// Radio Info
	private List<RadioInfo> radioInfos = new ArrayList<RadioInfo>();

	// Camera Info
	private List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();

	// Screen State Info
	private List<ScreenStateInfo> screenStateInfos = new ArrayList<ScreenStateInfo>();

	// List of User Event Info
	private List<UserEvent> userEvents = new ArrayList<UserEvent>();

	// time
	private String collectorName;
	private String deviceModel;
	private String deviceMake;
	private String osType;
	private String osVersion;
	private String collectorVersion;
	private NetworkType networkType;
	private double pcapTime0;
	private Date traceDateTime;
	private double eventTime0;
	private double traceDuration;
	private double gpsActiveDuration;
	private double wifiActiveDuration;
	private double bluetoothActiveDuration;
	private double cameraActiveDuration;

	private Set<String> missingFiles = new HashSet<String>();

	// All packets included in the trace (not filtered)
	private File pcapFile;
	private List<PacketInfo> allPackets = new ArrayList<PacketInfo>(1000);
	private Map<InetAddress, Integer> ipCountMap = new HashMap<InetAddress, Integer>();

	// private WhatIf whatIf = new WhatIf(WhatIf.WhatIfType.WHATIF_NO) ;
	private Set<String> allAppNames = new HashSet<String>();
	private Map<String, Set<InetAddress>> appIps = new HashMap<String, Set<InetAddress>>();

	/**
	 * Pcap packet listener
	 */
	private PacketListener packetListener = new PacketListener() {

		@Override
		public void packetArrived(Packet packet) {
			if (packet instanceof IPPacket) { // Replaces GetPacketInfo(...)
				IPPacket ip = (IPPacket) packet;
				if (ip.getIPVersion() != 4) {
					logger.warning("225 - Non IPv4 packet received.  Version: "
							+ ip.getIPVersion());
				}

				// no IP fragmentation
				if (ip.getFragmentOffset() != 0) {
					logger.warning("226 - no IP fragmentation");
				}

				addIpCount(ip.getSourceIPAddress());
				addIpCount(ip.getDestinationIPAddress());
			}
			allPackets.add(new PacketInfo(packet));

		}
	};

	/**
	 * Initializes an instance of the TraceData class, using the specified trace
	 * directory.
	 * 
	 * @param traceDir
	 *            - Directory where the trace files are located.
	 * 
	 * @throws IllegalArgumentException
	 *             if traceDir does not represent and existing directory in the
	 *             file system
	 * @throws IOException
	 *             when error occurs reading trace information
	 */
	public TraceData(File traceDir) throws IOException {

		// Check input directory
		if (traceDir == null || !traceDir.exists()) {
			throw new IllegalArgumentException(
					"Argument must represent an existing directory or pcap file");
		}
		this.traceDir = traceDir;

		if (traceDir.isDirectory()) {

			// Full data collector trace
			readData();
		} else {

			// Read PCAP file only
			readPcapTrace(traceDir, null, null, null);
		}

	}

	/**
	 * Returns the trace directory.
	 * 
	 * @return A File object containing the trace directory.
	 */
	public File getTraceDir() {
		return traceDir;
	}

	/**
	 * Returns the date and time of the trace data.
	 * 
	 * @return The trace date and time.
	 */
	public Date getTraceDateTime() {
		return traceDateTime;
	}

	/**
	 * Returns the total duration of the loaded trace file.
	 * 
	 * @return The trace duration.
	 */
	public double getTraceDuration() {
		return traceDuration;
	}

	/**
	 * Returns the name of the collector that was used to collect the trace
	 * data.
	 * 
	 * @return The collector name.
	 */
	public String getCollectorName() {
		return collectorName;
	}

	/**
	 * Returns the model of the device.
	 * 
	 * @return The device model.
	 */
	public String getDeviceModel() {
		return deviceModel;
	}

	/**
	 * Returns the make of the device.
	 * 
	 * @return The device make.
	 */
	public String getDeviceMake() {
		return deviceMake;
	}

	/**
	 * Returns the type of operating system that the trace data was collected
	 * on.
	 * 
	 * @return A string that describes the OS type.
	 */
	public String getOsType() {
		return osType;
	}

	/**
	 * Returns the version of the operating system that the trace data was
	 * collected on.
	 * 
	 * @return A string that describes the OS version.
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * Returns the version of the collector that was used to collect the trace
	 * data.
	 * 
	 * @return The collector version.
	 */
	public String getCollectorVersion() {
		return collectorVersion;
	}

	/**
	 * @return The networkType
	 */
	public NetworkType getNetworkType() {
		return networkType;
	}

	/**
	 * Returns the names of all the apps in the trace, including apps that were
	 * filtered from the analysis.
	 * 
	 * @return A Set of strings containing the application names.
	 */
	public Set<String> getAllAppNames() {
		return Collections.unmodifiableSet(allAppNames);
	}

	/**
	 * Returns the Application version map.
	 * 
	 * @return The application versions.
	 */
	public Map<String, String> getAppVersionMap() {
		return appVersionMap;
	}

	/**
	 * @return the appIps
	 */
	public Map<String, Set<InetAddress>> getAppIps() {
		return Collections.unmodifiableMap(appIps);
	}

	/**
	 * Returns the start time of the video.
	 * 
	 * @return The video start time.
	 */
	public double getVideoStartTime() {
		return videoStartTime;
	}

	/**
	 * Returns the pcapFile.
	 * 
	 * @return The pcap file.
	 */
	public File getPcapFile() {
		return pcapFile;
	}

	/**
	 * Returns the names of any files missing from the trace directory.
	 * 
	 * @return A Set of strings containing the names of the missing files.
	 */
	public Set<String> getMissingFiles() {
		return Collections.unmodifiableSet(missingFiles);
	}

	/**
	 * Runs analysis on the trace data for the specified collection filter,
	 * using the specified device profile.
	 * 
	 * @param profile
	 *            The device profile settings.
	 * 
	 * @param filter
	 *            An optional analysis filter that would filter out info from
	 *            the trace during analysis.
	 * 
	 * @return An Analysis object containing the trace analysis.
	 */
	public synchronized Analysis runAnalysis(Profile profile,
			AnalysisFilter filter) throws IOException {
		return new Analysis(profile, filter);
	}

	/**
	 * Reads the application names from the appinfo trace file.
	 * 
	 * @throws IOException
	 */
	private void readAppInfo() throws IOException {
		File file = new File(traceDir, APPNAME_FILE);
		if (!file.exists()) {
			this.missingFiles.add(APPNAME_FILE);
		}

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			for (String s = br.readLine(); s != null; s = br.readLine()) {
				String strFields[] = s.split(" ");
				this.appInfos.add(strFields[0]);
				if (strFields.length > 1) {
					this.appVersionMap.put(strFields[0], strFields[1]);
				}
			}
		} finally {
			br.close();
		}

	}

	/**
	 * Reads the application ID's from the appid trace file.
	 * 
	 * @return The list of app ids found in the trace data.
	 * @throws IOException
	 */
	private List<Integer> readAppIDs() throws IOException {
		File file = new File(traceDir, APPID_FILE);
		if (!file.exists()) {
			this.missingFiles.add(APPID_FILE);
			return Collections.emptyList();
		}

		List<Integer> appIds = new ArrayList<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			for (String s = br.readLine(); s != null; s = br.readLine()) {
				int appId = Integer.valueOf(s);

				// Check for EOF indicator
				if (appId == PACKET_EOF) {
					break;
				}
				appIds.add(appId);
			}
		} finally {
			br.close();
		}
		return appIds;
	}

	/**
	 * Reads the PCAP file and the trace times from the time trace file.
	 * 
	 * @throws IOException
	 */
	private void readTimeAndPcap() throws IOException {

		File file = new File(traceDir, TIME_FILE);
		Double startTime = null;
		Double duration = null;
		if (file.exists()) {
			
			Times times = readTimes(traceDir);
			startTime = times.startTime;
			if (times.eventTime != null) {
				this.eventTime0 = times.eventTime.doubleValue();
			}
			duration = times.duration;

		} else {
			this.missingFiles.add(TIME_FILE);
		}

		// Read the pcap file to get default times
		File pcap = new File(traceDir, PCAP_FILE);
		readPcapTrace(pcap, readAppIDs(), startTime, duration);
	}

	/**
	 * Reads a device Info from the device file in trace folder.
	 * 
	 * @throws IOException
	 */
	private void readDeviceInfo() throws IOException {

		File file = new File(traceDir, DEVICEINFO_FILE);
		if (!file.exists()) {
			this.missingFiles.add(DEVICEINFO_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {

			String s;
			while ((s = br.readLine()) != null) {

				// In case of IPv6 scoped address, remove scope ID
				int i = s.indexOf('%');
				localIPAddresses.add(InetAddress.getByName(i >= 0 ? s
						.substring(0, i) : s));
			}

		} finally {
			br.close();
		}
	}

	/**
	 * Reads a device data from the device file in trace folder.
	 * 
	 * @throws IOException
	 */
	private void readDeviceDetails() throws IOException {

		File file = new File(traceDir, DEVICEDETAILS_FILE);
		if (!file.exists()) {
			this.missingFiles.add(DEVICEDETAILS_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			this.collectorName = br.readLine();
			this.deviceModel = br.readLine();
			this.deviceMake = br.readLine();
			this.osType = br.readLine();
			this.osVersion = br.readLine();
			this.collectorVersion = br.readLine();

			try {
				String networkTypeStr = br.readLine();
				int networkType = networkTypeStr != null ? Integer
						.parseInt(networkTypeStr.trim()) : 0;
				switch (networkType) {
				case WIFI:
					this.networkType = NetworkType.WIFI;
					break;
				case GPRS:
					this.networkType = NetworkType.GPRS;
					break;
				case UMTS:
					this.networkType = NetworkType.UMTS;
					break;
				case HSDPA:
					this.networkType = NetworkType.HSDPA;
					break;
				case HSUPA:
					this.networkType = NetworkType.HSUPA;
					break;
				case HSPA:
					this.networkType = NetworkType.HSPA;
					break;
				case HSPAP:
					this.networkType = NetworkType.HSPAP;
					break;
				case LTE:
					this.networkType = NetworkType.LTE;
					break;
				default:
					this.networkType = NetworkType.UNKNOWN;
					break;
				}
			} catch (NumberFormatException e) {
				this.networkType = NetworkType.UNKNOWN;
			}
		} finally {
			br.close();
		}

	}

	/**
	 * Reads the pcap trace file from the trace folder. Using Jpcap library it
	 * iterate through all packet in the pcap file.
	 * 
	 * @throws IOException when an unexpected I/O error occurs
	 * @throws FileNotFoundException when file does not exist or is empty
	 */
	private void readPcapTrace(File pcap, List<Integer> appIds, Double startTime, Double duration) throws IOException, FileNotFoundException {

		if (!pcap.exists()) {
			logger.severe("No TCP data found in trace");
			
			// Force FileNotFoundException
			new FileInputStream(pcap);
		}
		this.pcapFile = pcap;
		new PCapAdapter(pcap, packetListener);

		// Ignore the last packet (nexus one data collector's problem)
		// Don't know why this is done in prototype
		// Commented out so results match wireshark
		// allPackets.remove(allPackets.size() - 1);

		// Determine application name associated with each packet
		if (allPackets.size() > 0) {
			int i = 0;
			final String pcapAppName = "";
			this.pcapTime0 = startTime != null ? startTime.doubleValue()
					: allPackets.get(0).getTimeStamp();
			this.traceDuration = duration != null ? duration
					.doubleValue() : allPackets.get(allPackets.size() - 1)
					.getTimeStamp() - this.pcapTime0;
			if (appIds == null) {
				appIds = Collections.emptyList();
			}
			for (Iterator<PacketInfo> iter = allPackets.iterator(); iter.hasNext(); ++i) {
				PacketInfo packet = iter.next();

				// Filter out non-IP packets
				if (!(packet.getPacket() instanceof IPPacket)) {
					iter.remove();
					continue;
				}

				IPPacket ip = (IPPacket) packet.getPacket();
				packet.setDir(determinePacketDirection(ip.getSourceIPAddress(),
						ip.getDestinationIPAddress()));
				packet.setTimestamp(ip.getTimeStamp() - this.pcapTime0);

				String appName;
				if (i < appIds.size()) {
					int appId = appIds.get(i);

					// Check for valid application
					if (appId >= 0) {
						assert (appId < appInfos.size());

						appName = appId < appInfos.size() ? appInfos
								.get(appId) : null;
					} else {

						// Should indicate unknown app ID
						assert (appId == PACKET_UNKNOWN_APP);
						appName = null;
					}
				} else {
					appName = pcapAppName;
				}
				packet.setAppName(appName);
				this.allAppNames.add(appName);

				// Group IPs by app
				Set<InetAddress> ips = appIps.get(appName);
				if (ips == null) {
					ips = new HashSet<InetAddress>();
					appIps.put(appName, ips);
				}
				ips.add(packet.getRemoteIPAddress());

				// Set packet ID to match Wireshark ID
				packet.setId(i + 1);

			}

			Collections.sort(allPackets);
		} else {
			this.pcapTime0 = startTime != null ? startTime.doubleValue() : pcap
					.lastModified() / 1000.0;
			this.traceDuration = duration != null ? duration
					.doubleValue() : 0.0;
		}
		this.traceDateTime = new Date((long) (this.pcapTime0 * 1000));
	}

	/**
	 * Parses the user event trace
	 * 
	 * @throws IOException
	 */
	private void readUserEvents() throws IOException {
		Map<UserEventType, Double> lastEvent = new EnumMap<UserEventType, Double>(
				UserEventType.class);

		File file = new File(traceDir, USER_EVENTS_FILE);
		if (!file.exists()) {
			this.missingFiles.add(USER_EVENTS_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			for (String lineBuf = br.readLine(); lineBuf != null; lineBuf = br
					.readLine()) {

				// Ignore empty line
				if (lineBuf.trim().length() == 0) {
					continue;
				}

				// Parse entry
				String strFields[] = lineBuf.split(" ");
				if (strFields.length < 0) {
					logger.warning("Found invalid user event entry: " + lineBuf);
					continue;
				}

				// Get timestamp
				double dTimeStamp = Double.parseDouble(strFields[0]);
				if (dTimeStamp > 1.0e9) {
					dTimeStamp = normalizeTime(dTimeStamp);
				} else {

					// Old data collector method (backward compatible)
					dTimeStamp -= eventTime0;
				}

				// Get event type
				UserEvent.UserEventType actionType = UserEvent.UserEventType.EVENT_UNKNOWN;
				String processedEvent;
				if (strFields.length == 3 && SCREEN.equals(strFields[1])) {
					processedEvent = strFields[2];
					actionType = UserEventType.SCREEN_TOUCH;
				} else if (strFields.length == 4 && KEY.equals(strFields[1])) {
					processedEvent = strFields[3];
					if (KEY_KEY.equals(strFields[2])) {
						actionType = UserEventType.KEY_KEY;
					} else if (KEY_POWER.equals(strFields[2])) {
						actionType = UserEventType.KEY_POWER;
					} else if (KEY_VOLUP.equals(strFields[2])) {
						actionType = UserEventType.KEY_VOLUP;
					} else if (KEY_VOLDOWN.equals(strFields[2])) {
						actionType = UserEventType.KEY_VOLDOWN;
					} else if (KEY_BALL.equals(strFields[2])) {
						actionType = UserEventType.KEY_BALL;
					} else if (KEY_HOME.equals(strFields[2])) {
						actionType = UserEventType.KEY_HOME;
					} else if (KEY_MENU.equals(strFields[2])) {
						actionType = UserEventType.KEY_MENU;
					} else if (KEY_BACK.equals(strFields[2])) {
						actionType = UserEventType.KEY_BACK;
					} else if (KEY_SEARCH.equals(strFields[2])) {
						actionType = UserEventType.KEY_SEARCH;
					} else if (KEY_GREEN.equals(strFields[2])) {
						actionType = UserEventType.KEY_GREEN;
					} else if (KEY_RED.equals(strFields[2])) {
						actionType = UserEventType.KEY_RED;
					}
				} else {
					logger.warning("Invalid user event type in trace: "
							+ lineBuf);
					continue;
				}

				// Get press or release
				boolean bPress = false;
				if (PRESS.equalsIgnoreCase(processedEvent)) {
					bPress = true;
				} else if (RELEASE.equalsIgnoreCase(processedEvent)) {
					bPress = false;
				} else {
					logger.warning("211 - Key event does not have press/release indication: "
							+ lineBuf);
					continue;
				}

				if (bPress) {
					lastEvent.put(actionType, dTimeStamp);
				} else {
					Double lastTime = lastEvent.remove(actionType);
					if (lastTime != null) {
						userEvents.add(new UserEvent(actionType, lastTime,
								dTimeStamp));
					} else {
						logger.warning("Found key release event with no associated press event: "
								+ lineBuf);
						continue;
					}
				}
			}

			for (Map.Entry<UserEventType, Double> entry : lastEvent.entrySet()) {
				logger.warning("Unmatched user press/release input event: "
						+ entry.getKey());
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Reads the screen rotations information contained in the
	 * "screen_rotations" file found inside the trace directory and adds them to
	 * the user events list.
	 * 
	 * @throws IOException
	 */
	private void readScreenRotations() throws IOException {

		File file = new File(traceDir, SCREEN_ROTATIONS_FILE);

		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {

			String line = null;
			while ((line = reader.readLine()) != null) {

				String[] strFields = line.split(" ");
				// Get timestamp
				double dTimeStamp = normalizeTime(Double.parseDouble(strFields[0]));

				UserEventType eventType = null;

				if (strFields[1].contains(KEY_LANDSCAPE)) {
					eventType = UserEventType.SCREEN_LANDSCAPE;
				} else if (strFields[1].contains(KEY_PORTRAIT)) {
					eventType = UserEventType.SCREEN_PORTRAIT;
				}

				userEvents.add(new UserEvent(eventType, dTimeStamp,
						dTimeStamp + 0.5));
			}

			Collections.sort(userEvents, new UserEventSorting());

		} finally {
			reader.close();
		}

	}

	private class UserEventSorting implements Comparator<UserEvent> {
		@Override
		public int compare(UserEvent o1, UserEvent o2) {
			return Double.valueOf(o1.getPressTime()).compareTo(
					o2.getPressTime());
		}
	}

	/**
	 * Method to Read the data from the trace folder for all the files.
	 */
	private synchronized void readData() throws IOException {

		try {
			readAppInfo();
		} catch (IOException e) {
			logger.warning("*** Warning: no app information found ***");
		}

		// Read the time file and PCAP trace
		readTimeAndPcap();

		try {
			readDeviceInfo();
		} catch (IOException e) {
			logger.warning("*** Warning: no device information found ***");
		}

		try {
			readDeviceDetails();
		} catch (IOException e) {
			logger.warning("*** Warning: no device detail information found ***");
		}

		try {
			readCpu();
		} catch (IOException e) {
			logger.warning("*** Warning: no CPU information found ***");
		}

		try {
			readGps();
		} catch (IOException e) {
			logger.warning("*** Warning: no GPS information found ***");
		}

		try {
			readBluetooth();
		} catch (IOException e) {
			logger.warning("*** Warning: no Bluetooth information found ***");
		}

		try {
			readWifi();
		} catch (IOException e) {
			logger.warning("*** Warning: no Wifi information found ***");
		}

		try {
			readCamera();
		} catch (IOException e) {
			logger.warning("*** Warning: no Camera information found ***");
		}

		try {
			readScreenState();
		} catch (IOException e) {
			logger.warning("*** Warning: no Screen State information found ***");
		}

		try {
			readUserEvents();
		} catch (IOException e) {
			logger.warning("*** Warning: no user event information found ***");
		}
		try {
			readScreenRotations();
		} catch (IOException e) {
			logger.warning("*** Warning: no screen rotations information found ***");
		}

		try {
			// Reads the battery information
			readBattery();
		} catch (IOException e) {
			logger.warning("*** Warning: no battery information found ***");
		}

		try {
			readRadioEvents();
		} catch (IOException e) {
			logger.warning("*** Warning: no Radio Events information found ***");
		}

		try {
			readVideoTime();
		} catch (IOException e) {
			logger.warning("*** Warning: no Video time information found ***");
		}

	}

	/**
	 * Reads the CPU trace information from the CPU file.
	 * 
	 * @throws IOException
	 */
	private void readCpu() throws IOException {
		File file = new File(traceDir, CPU_FILE);
		if (!file.exists()) {
			this.missingFiles.add(CPU_FILE);
		}

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double prevCpuUsage = 0.0;
			double beginTime = 0.0;
			double endTime = 0.0;
			String firstLine = br.readLine();
			if (firstLine != null) {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length == 2) {
					beginTime = normalizeTime(Double.parseDouble(strFieldsFirstLine[0]));
					prevCpuUsage = Double.parseDouble(strFieldsFirstLine[1]);

				}

				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {
					String strFields[] = strLineBuf.split(" ");
					if (strFields.length == 2) {
						endTime = normalizeTime(Double.parseDouble(strFields[0]));
						double cpuUsage = Double.parseDouble(strFields[1]);
						CpuActivity cpuActivity = new CpuActivity(beginTime,
								endTime, prevCpuUsage);
						cpuActivityList.add(cpuActivity);

						prevCpuUsage = cpuUsage;
						beginTime = endTime;
					}
				}
				cpuActivityList.add(new CpuActivity(beginTime,
						getTraceDuration() , prevCpuUsage));
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the GPS data from the trace file and store it in the
	 * gpsInfos list. It also updates the active duration for GPS.
	 */
	private void readGps() throws IOException {
		File file = new File(traceDir, GPS_FILE);
		if (!file.exists()) {
			this.missingFiles.add(GPS_FILE);
		}

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double dLastActiveTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			GpsState prevGpsState = null;
			GpsState gpsState = null;
			double beginTime = 0.0;
			double endTime = 0.0;
			String firstLine = br.readLine();
			if (firstLine != null) {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length == 2) {
					try {
						beginTime = normalizeTime(Double
								.parseDouble(strFieldsFirstLine[0]));
						if (GPS_STANDBY.equals(strFieldsFirstLine[1])) {
							prevGpsState = GpsState.GPS_STANDBY;
						} else if (GPS_DISABLED.equals(strFieldsFirstLine[1])) {
							prevGpsState = GpsState.GPS_DISABLED;
						} else if (GPS_ACTIVE.equals(strFieldsFirstLine[1])) {
							prevGpsState = GpsState.GPS_ACTIVE;
							if (0.0 == dLastActiveTimeStamp) {
								dLastActiveTimeStamp = beginTime;
							}
						} else {
							logger.warning("Invalid GPS state: " + firstLine);
							prevGpsState = GpsState.GPS_UNKNOWN;
						}

						if ((!GPS_ACTIVE.equals(strFieldsFirstLine[1]))
								&& dLastActiveTimeStamp > 0.0) {
							dActiveDuration += (beginTime - dLastActiveTimeStamp);
							dLastActiveTimeStamp = 0.0;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing GPS event: "
										+ firstLine, e);
					}
				}
				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {

					String strFields[] = strLineBuf.split(" ");
					if (strFields.length == 2) {
						try {
							endTime = normalizeTime(Double
									.parseDouble(strFields[0]));
							if (GPS_STANDBY.equals(strFields[1])) {
								gpsState = GpsState.GPS_STANDBY;
							} else if (GPS_DISABLED.equals(strFields[1])) {
								gpsState = GpsState.GPS_DISABLED;
							} else if (GPS_ACTIVE.equals(strFields[1])) {
								gpsState = GpsState.GPS_ACTIVE;
								if (0.0 == dLastActiveTimeStamp) {
									dLastActiveTimeStamp = endTime;
								}
							} else {
								logger.warning("Invalid GPS state: "
										+ strLineBuf);
								gpsState = GpsState.GPS_UNKNOWN;
							}
							gpsInfos.add(new GpsInfo(beginTime, endTime,
									prevGpsState));

							if ((!GPS_ACTIVE.equals(strFields[1]))
									&& dLastActiveTimeStamp > 0.0) {
								dActiveDuration += (endTime - dLastActiveTimeStamp);
								dLastActiveTimeStamp = 0.0;
							}
							prevGpsState = gpsState;
							beginTime = endTime;

						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Unexpected error parsing GPS event: "
											+ strLineBuf, e);
						}
					} else {
						logger.warning("Invalid GPS trace entry: " + strLineBuf);
					}

				}

				gpsInfos.add(new GpsInfo(beginTime, getTraceDuration(), prevGpsState));

				// Duration calculation should probably be done in analysis
				if (prevGpsState == GpsState.GPS_ACTIVE) {
					dActiveDuration += Math.max(0, getTraceDuration()
							- dLastActiveTimeStamp);
				}

				this.gpsActiveDuration = dActiveDuration;
				Collections.sort(gpsInfos);
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the Bluetooth data from the trace file and store it in the
	 * bluetoothInfos list. It also updates the active duration for Bluetooth.
	 * 
	 * @throws IOException
	 */
	private void readBluetooth() throws IOException {

		File file = new File(traceDir, BLUETOOTH_FILE);
		if (!file.exists()) {
			this.missingFiles.add(BLUETOOTH_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double beginTime = 0.0;
			double endTime;
			double dLastTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			BluetoothState prevBtState = null;
			BluetoothState btState = null;
			BluetoothState lastState = null;
			String firstLine = br.readLine();
			if (firstLine != null) {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length == 2) {
					try {
						beginTime = normalizeTime(Double
								.parseDouble(strFieldsFirstLine[0]));
						if (BLUETOOTH_CONNECTED.equals(strFieldsFirstLine[1])) {
							prevBtState = BluetoothState.BLUETOOTH_CONNECTED;
						} else if (BLUETOOTH_DISCONNECTED
								.equals(strFieldsFirstLine[1])) {
							prevBtState = BluetoothState.BLUETOOTH_DISCONNECTED;
						} else if (BLUETOOTH_OFF.equals(strFieldsFirstLine[1])) {
							prevBtState = BluetoothState.BLUETOOTH_TURNED_OFF;
						} else {
							logger.warning("Unknown bluetooth state: "
									+ firstLine);
							prevBtState = BluetoothState.BLUETOOTH_UNKNOWN;
						}

						if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
							dActiveDuration += (beginTime - dLastTimeStamp);
						}
						lastState = prevBtState;
						dLastTimeStamp = beginTime;
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing bluetooth event: "
										+ firstLine, e);
					}
				} else {
					logger.warning("Invalid Bluetooth trace entry: "
							+ firstLine);
				}
				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {
					String strFields[] = strLineBuf.split(" ");
					if (strFields.length == 2) {
						try {
							endTime = normalizeTime(Double
									.parseDouble(strFields[0]));
							if (BLUETOOTH_CONNECTED.equals(strFields[1])) {
								btState = BluetoothState.BLUETOOTH_CONNECTED;
							} else if (BLUETOOTH_DISCONNECTED
									.equals(strFields[1])) {
								btState = BluetoothState.BLUETOOTH_DISCONNECTED;
							} else if (BLUETOOTH_OFF.equals(strFields[1])) {
								btState = BluetoothState.BLUETOOTH_TURNED_OFF;
							} else {
								logger.warning("Unknown bluetooth state: "
										+ strLineBuf);
								btState = BluetoothState.BLUETOOTH_UNKNOWN;
							}
							bluetoothInfos.add(new BluetoothInfo(beginTime,
									endTime, prevBtState));

							if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
								dActiveDuration += (endTime - dLastTimeStamp);
							}
							lastState = btState;
							dLastTimeStamp = endTime;
							prevBtState = btState;
							beginTime = endTime;
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Unexpected error parsing bluetooth event: "
											+ strLineBuf, e);
						}
					} else {
						logger.warning("Invalid Bluetooth trace entry: "
								+ strLineBuf);
					}
				}
				bluetoothInfos.add(new BluetoothInfo(beginTime,
						getTraceDuration(), prevBtState));
				// Duration calculation should probably be done in analysis
				if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
					dActiveDuration += Math.max(0, getTraceDuration()
							- dLastTimeStamp);
				}

				this.bluetoothActiveDuration = dActiveDuration;
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the WIFI data from the trace file and store it in the
	 * wifiInfos list. It also updates the active duration for Wifi.
	 */
	private void readWifi() throws IOException {
		File file = new File(traceDir, WIFI_FILE);
		if (!file.exists()) {
			this.missingFiles.add(WIFI_FILE);
		}
		double dLastTimeStamp = 0.0;
		double dActiveDuration = 0.0;
		double beginTime = 0.0;
		double endTime = 0.0;
		String prevMacAddress = null;
		String prevRssi = null;
		String prevSsid = null;
		WifiState prevWifiState = null;
		WifiState lastWifiState = null;
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String firstLine = br.readLine();
			if (firstLine != null) {
				try {
					String strFieldsFirstLine[] = firstLine.split(" ");
					if (strFieldsFirstLine.length >= 2) {
						beginTime = normalizeTime(Double
								.parseDouble(strFieldsFirstLine[0]));
						if (WIFI_OFF.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_DISABLED;
						} else if (WIFI_CONNECTED.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_CONNECTED;
							Matcher matcher = wifiPattern.matcher(firstLine);
							if (matcher.lookingAt()) {
								prevMacAddress = matcher.group(1);
								prevRssi = matcher.group(2);
								prevSsid = matcher.group(3);
							} else {
								logger.warning("Unable to parse wifi connection params: "
										+ firstLine);
							}
						} else if (WIFI_DISCONNECTED
								.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_DISCONNECTED;
						} else if (WIFI_CONNECTING
								.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_CONNECTING;
						} else if (WIFI_DISCONNECTING
								.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_DISCONNECTING;
						} else if (WIFI_SUSPENDED.equals(strFieldsFirstLine[1])) {
							prevWifiState = WifiState.WIFI_SUSPENDED;
						} else {
							logger.warning("Unknown wifi state: " + firstLine);
							prevWifiState = WifiState.WIFI_UNKNOWN;
						}

						if (prevWifiState != lastWifiState) {

							if (lastWifiState == WifiState.WIFI_CONNECTED
									|| lastWifiState == WifiState.WIFI_CONNECTING
									|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
								dActiveDuration += (beginTime - dLastTimeStamp);
							}
							lastWifiState = prevWifiState;
							dLastTimeStamp = beginTime;
						}
					} else {
						logger.warning("Invalid WiFi trace entry: " + firstLine);
					}

				} catch (Exception e) {
					logger.log(Level.WARNING,
							"Unexpected error parsing GPS event: " + firstLine,
							e);
				}

				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {
					String strFields[] = strLineBuf.split(" ");
					try {
						if (strFields.length >= 2) {
							String macAddress = null;
							String rssi = null;
							String ssid = null;
							WifiState wifiState = null;
							endTime = normalizeTime(Double
									.parseDouble(strFields[0]));
							if (WIFI_OFF.equals(strFields[1])) {
								wifiState = WifiState.WIFI_DISABLED;
							} else if (WIFI_CONNECTED.equals(strFields[1])) {
								wifiState = WifiState.WIFI_CONNECTED;
								Matcher matcher = wifiPattern
										.matcher(strLineBuf);
								if (matcher.lookingAt()) {
									macAddress = matcher.group(1);
									rssi = matcher.group(2);
									ssid = matcher.group(3);
								} else {
									logger.warning("Unable to parse wifi connection params: "
											+ strLineBuf);
								}
							} else if (WIFI_DISCONNECTED.equals(strFields[1])) {
								wifiState = WifiState.WIFI_DISCONNECTED;
							} else if (WIFI_CONNECTING.equals(strFields[1])) {
								wifiState = WifiState.WIFI_CONNECTING;
							} else if (WIFI_DISCONNECTING.equals(strFields[1])) {
								wifiState = WifiState.WIFI_DISCONNECTING;
							} else if (WIFI_SUSPENDED.equals(strFields[1])) {
								wifiState = WifiState.WIFI_SUSPENDED;
							} else {
								logger.warning("Unknown wifi state: "
										+ strLineBuf);
								wifiState = WifiState.WIFI_UNKNOWN;
							}

							if (wifiState != lastWifiState) {
								wifiInfos.add(new WifiInfo(beginTime, endTime,
										prevWifiState, prevMacAddress,
										prevRssi, prevSsid));
								if (lastWifiState == WifiState.WIFI_CONNECTED
										|| lastWifiState == WifiState.WIFI_CONNECTING
										|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
									dActiveDuration += (endTime - dLastTimeStamp);
								}
								lastWifiState = wifiState;
								dLastTimeStamp = endTime;
								beginTime = endTime;
								prevWifiState = wifiState;
								prevMacAddress = macAddress;
								prevRssi = rssi;
								prevSsid = ssid;
							}
						} else {
							logger.warning("Invalid WiFi trace entry: "
									+ strLineBuf);
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing GPS event: "
										+ strLineBuf, e);
					}

				}
				wifiInfos.add(new WifiInfo(beginTime, getTraceDuration() , prevWifiState, prevMacAddress, prevRssi,
						prevSsid));

				// Duration calculation should probably be done in analysis
				if (lastWifiState == WifiState.WIFI_CONNECTED
						|| lastWifiState == WifiState.WIFI_CONNECTING
						|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
					dActiveDuration += Math.max(0, getTraceDuration()
							- dLastTimeStamp);
				}

				this.wifiActiveDuration = dActiveDuration;
				Collections.sort(wifiInfos);
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the Camera data from the trace file and store it in the
	 * cameraInfos list. It also updates the active duration for Camera.
	 */
	private void readCamera() throws IOException {
		File file = new File(traceDir, CAMERA_FILE);
		if (!file.exists()) {
			this.missingFiles.add(CAMERA_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double beginTime = 0.0;
			double endTime;
			double dLastActiveTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			CameraState prevCameraState = null;
			CameraState cameraState = null;
			String firstLine = br.readLine();
			if (firstLine != null) {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length == 2) {
					try {
						beginTime = normalizeTime(Double
								.parseDouble(strFieldsFirstLine[0]));
						if (CAMERA_ON.equals(strFieldsFirstLine[1])) {
							prevCameraState = CameraState.CAMERA_ON;
							if (0.0 == dLastActiveTimeStamp) {
								dLastActiveTimeStamp = beginTime;
							}
						} else if (CAMERA_OFF.equals(strFieldsFirstLine[1])) {
							prevCameraState = CameraState.CAMERA_OFF;
						} else {
							logger.warning("Unknown camera state: " + firstLine);
							prevCameraState = CameraState.CAMERA_UNKNOWN;
						}

						if ((!CAMERA_ON.equals(strFieldsFirstLine[1]))
								&& dLastActiveTimeStamp > 0.0) {
							dActiveDuration += (beginTime - dLastActiveTimeStamp);
							dLastActiveTimeStamp = 0.0;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error in camera events: "
										+ firstLine, e);
					}
				} else {
					logger.warning("Unrecognized camera event: " + firstLine);
				}

				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {
					String strFields[] = strLineBuf.split(" ");
					if (strFields.length == 2) {
						try {
							endTime = normalizeTime(Double
									.parseDouble(strFields[0]));
							if (CAMERA_ON.equals(strFields[1])) {
								cameraState = CameraState.CAMERA_ON;
								if (0.0 == dLastActiveTimeStamp) {
									dLastActiveTimeStamp = endTime;
								}
							} else if (CAMERA_OFF.equals(strFields[1])) {
								cameraState = CameraState.CAMERA_OFF;
							} else {
								logger.warning("Unknown camera state: "
										+ strLineBuf);
								cameraState = CameraState.CAMERA_UNKNOWN;
							}
							cameraInfos.add(new CameraInfo(beginTime, endTime,
									prevCameraState));

							if ((!CAMERA_ON.equals(strFields[1]))
									&& dLastActiveTimeStamp > 0.0) {
								dActiveDuration += (endTime - dLastActiveTimeStamp);
								dLastActiveTimeStamp = 0.0;
							}
							prevCameraState = cameraState;
							beginTime = endTime;

						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Unexpected error in camera events: "
											+ strLineBuf, e);
						}
					} else {
						logger.warning("Unrecognized camera event: "
								+ strLineBuf);
					}
				}
				cameraInfos.add(new CameraInfo(beginTime, getTraceDuration() , prevCameraState));

				// Duration calculation should probably be done in analysis
				if (cameraState == CameraState.CAMERA_ON) {
					dActiveDuration += Math.max(0, getTraceDuration()
							- dLastActiveTimeStamp);
				}

				this.cameraActiveDuration = dActiveDuration;
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the Screen State data from the trace file and store it in
	 * the ScreenStateInfos list.
	 */
	private void readScreenState() throws IOException {

		File file = new File(traceDir, SCREEN_STATE_FILE);
		if (!file.exists()) {
			this.missingFiles.add(SCREEN_STATE_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double beginTime = 0.0;
			double endTime = 0.0;
			ScreenState prevScreenState = null;
			ScreenState screenState;
			String prevBrigtness = null;
			String brightness = null;
			int prevTimeOut = 0;
			int timeout = 0;
			String firstLine = br.readLine();
			if (firstLine != null) {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length >= 2) {
					try {
						beginTime = normalizeTime(Double
								.parseDouble(strFieldsFirstLine[0]));

						if (SCREEN_ON.equals(strFieldsFirstLine[1])) {
							prevScreenState = ScreenState.SCREEN_ON;
							if (strFieldsFirstLine.length >= 4) {
								prevTimeOut = Integer
										.parseInt(strFieldsFirstLine[2]);
								prevBrigtness = strFieldsFirstLine[3];
							}
						} else if (SCREEN_OFF.equals(strFieldsFirstLine[1])) {
							prevScreenState = ScreenState.SCREEN_OFF;
						} else {
							logger.warning("Unknown screen state: " + firstLine);
							prevScreenState = ScreenState.SCREEN_UNKNOWN;
						}

					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error in screen events: "
										+ firstLine, e);
					}
				} else {
					logger.warning("Unrecognized screen state event: "
							+ firstLine);
				}
				for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
						.readLine()) {
					String strFields[] = strLineBuf.split(" ");
					if (strFields.length >= 2) {
						try {
							endTime = normalizeTime(Double
									.parseDouble(strFields[0]));
							brightness = null;
							timeout = 0;
							if (SCREEN_ON.equals(strFields[1])) {
								screenState = ScreenState.SCREEN_ON;
								if (strFields.length >= 4) {
									timeout = Integer.parseInt(strFields[2]);
									brightness = strFields[3];
								}
							} else if (SCREEN_OFF.equals(strFields[1])) {
								screenState = ScreenState.SCREEN_OFF;
							} else {
								logger.warning("Unknown screen state: "
										+ strLineBuf);
								screenState = ScreenState.SCREEN_UNKNOWN;
							}

							ScreenStateInfo screenInfo = new ScreenStateInfo(
									beginTime, endTime, prevScreenState,
									prevBrigtness, prevTimeOut);
							screenStateInfos.add(screenInfo);
							prevScreenState = screenState;
							prevBrigtness = brightness;
							prevTimeOut = timeout;
							beginTime = endTime;
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Unexpected error in screen events: "
											+ strLineBuf, e);
						}
					} else {
						logger.warning("Unrecognized screen state event: "
								+ strLineBuf);
					}
				}
				screenStateInfos.add(new ScreenStateInfo(beginTime,
						getTraceDuration() , prevScreenState,
						prevBrigtness, prevTimeOut));

			}
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the Battery data from the trace file and store it in the
	 * batteryInfos list.
	 */
	private void readBattery() throws IOException {
		//Adding defaults for the first line of the battery file.
		int previousLevel = 0;
		int previousTemp = 0;
		boolean previousState = false;
		File file = new File(traceDir, BATTERY_FILE);
		if (!file.exists()) {
			this.missingFiles.add(BATTERY_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 4) {
					try {
						double bTimeStamp = normalizeTime(Double
								.parseDouble(strFields[0]));
						int bLevel = Integer.parseInt(strFields[1]);
						int bTemp = Integer.parseInt(strFields[2]);
						boolean bState = Boolean.valueOf(strFields[3]);
						//Checks to make sure that the new line is not the same as the previous line so duplicate points arn't plotted
						if(bLevel != previousLevel || bTemp != previousTemp || bState != previousState) 
							batteryInfos.add(new BatteryInfo(bTimeStamp, bState,
									bLevel, bTemp));
						previousLevel = Integer.parseInt(strFields[1]);
						previousTemp = Integer.parseInt(strFields[2]);
						previousState = Boolean.valueOf(strFields[3]);
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing battery event: "
										+ strLineBuf, e);
					}
				} else {
					logger.warning("Invalid battery_events entry: "
							+ strLineBuf);
				}
			}
		} finally {
			br.close();
		}
	}

	/**
	 * Reads the Radio data from the file and stores it in the RadioInfo.
	 */
	private void readRadioEvents() throws IOException {
		File file = new File(traceDir, RADIO_EVENTS_FILE);
		if (!file.exists()) {
			this.missingFiles.add(RADIO_EVENTS_FILE);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			Double lastDbmValue = null;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {

				String[] strFields = strLineBuf.split(" ");
				try {
					if (strFields.length == 2) {
						double timestampVal = normalizeTime(Double
								.parseDouble(strFields[0]));
						double dbmValue = Double.parseDouble(strFields[1]);

						// Special handling for lost or regained signal
						if (lastDbmValue != null
								&& timestampVal > 0.0
								&& (dbmValue >= 0.0 || lastDbmValue
										.doubleValue() >= 0.0)
								&& dbmValue != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal,
									lastDbmValue.doubleValue()));
						}

						// Add radio event
						radioInfos.add(new RadioInfo(timestampVal, dbmValue));
						lastDbmValue = dbmValue;
					} else if (strFields.length == 6) {
						
						// LTE
						double timestampVal = normalizeTime(Double
								.parseDouble(strFields[0]));
						RadioInfo ri = new RadioInfo(timestampVal,
								Integer.parseInt(strFields[1]),
								Integer.parseInt(strFields[2]),
								Integer.parseInt(strFields[3]),
								Integer.parseInt(strFields[4]),
								Integer.parseInt(strFields[5]));
						
						// Special handling for lost or regained signal
						if (lastDbmValue != null
								&& timestampVal > 0.0
								&& (ri.getSignalStrength() >= 0.0 || lastDbmValue
										.doubleValue() >= 0.0)
								&& ri.getSignalStrength() != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal,
									lastDbmValue.doubleValue()));
						}

						// Add radio event
						radioInfos.add(ri);
						lastDbmValue = ri.getSignalStrength();
						
					} else {
						logger.warning("Invalid radio_events entry: " + strLineBuf);
					}
				} catch (Exception e) {
					logger.log(Level.WARNING,
							"Unexpected error parsing radio event: "
									+ strLineBuf, e);
				}
			}
		} finally {
			br.close();
		}

	}

	/**
	 * Method to read times from the video time trace file and store video time
	 * variables.
	 */
	private void readVideoTime() throws IOException {
		String videoDisplayFileName = rb.getString("video.videoDisplayFile");
		String videoFileNameFromDevice = rb
				.getString("video.videoFileOnDevice");
		File videoDisplayFile = new File(traceDir, videoDisplayFileName);
		File videoFileFromDevice = new File(traceDir, videoFileNameFromDevice);
		if (videoDisplayFile.exists() || videoFileFromDevice.exists()) {
			File file = new File(traceDir, VIDEO_TIME_FILE);
			if (!file.exists()) {
				if (new File(traceDir, VIDEO_MP4_FILE).exists()
						|| new File(traceDir, VIDEO_MOV_FILE).exists()) {
					this.missingFiles.add(VIDEO_TIME_FILE);
				}
			} else {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					String s = br.readLine();
					if (s != null) {
						String[] strValues = s.split(" ");
						if (strValues.length > 0) {
							try {
								videoStartTime = Double
										.parseDouble(strValues[0]);
							} catch (NumberFormatException e) {
								logger.log(
										Level.SEVERE,
										"Cannot determine actual video start time",
										e);
							}
							if (strValues.length > 1) {
								// For emulator only, tcpdumpLocalStartTime is
								// start
								// time started according to local pc/laptop.
								// getTraceDateTime is time according to
								// emulated device
								// -- the tcpdumpDeviceVsLocalTimeDetal is
								// difference
								// between the two and is added as an offset
								// to videoStartTime so that traceEmulatorTime
								// and
								// videoStartTime are in sync.
								double tcpdumpLocalStartTime = Double
										.parseDouble(strValues[1]);
								double tcpdumpDeviceVsLocalTimeDelta = (getTraceDateTime()
										.getTime() / 1000.0)
										- tcpdumpLocalStartTime;
								videoStartTime += tcpdumpDeviceVsLocalTimeDelta;
							}
						}
					}
				} finally {
					br.close();
				}
			}
		}
		if (videoStartTime == 0.0) {
			videoStartTime = pcapTime0;
		}
	}

	/**
	 * Attempts to determine packet direction based upon source and destination
	 * IP addresses
	 */
	private PacketInfo.Direction determinePacketDirection(InetAddress source,
			InetAddress dest) {

		// Check identified local IP addresses
		if (this.localIPAddresses.contains(source)) {
			return Direction.UPLINK;
		} else if (this.localIPAddresses.contains(dest)) {
			return Direction.DOWNLINK;
		}

		// Do same check done by ARO prototype
		boolean srcLocal = isLocal(source);
		boolean destLocal = isLocal(dest);
		if (srcLocal && !destLocal) {
			this.localIPAddresses.add(source);
			return Direction.UPLINK;
		} else if (destLocal && !srcLocal) {
			this.localIPAddresses.add(dest);
			return Direction.DOWNLINK;
		}

		// Otherwise make a guess based upon the count of time the IP has been
		// in a packet
		int srcCount = ipCountMap.get(source).intValue();
		int destCount = ipCountMap.get(dest).intValue();
		if (srcCount >= destCount) {
			this.localIPAddresses.add(source);
			return Direction.UPLINK;
		} else {
			this.localIPAddresses.add(dest);
			return Direction.DOWNLINK;
		}
	}

	/**
	 * ARO prototype logic for finding local IP address
	 */
	private boolean isLocal(InetAddress ip) {

		if (ip instanceof Inet4Address) {
			byte[] addr = ((Inet4Address) ip).getAddress();
			return addr[0] == 10;
		}
		return false;
	}

	/**
	 * Normalizes the collected time with respect to the trace start time. The
	 * check done is for backward compatibility with traces created with early
	 * test versions of the data collector which attempted to normalize on the
	 * device. This normalization was moved here to make it consistent with all
	 * analysis data.
	 * 
	 * @param time
	 *            The time value to be normalized.
	 * @return The normalized time in double.
	 */
	private double normalizeTime(double time) {

		// The comparison check here is for backward compatibility
		time = time > 1.0E9 ? time - pcapTime0 : time;
		if (time < 0) {
			return 0.0;
		}
		return time;
	}

	/**
	 * Adds the IP count in ipCountMap list.
	 * 
	 * @param ip
	 *            unique ip address.
	 */
	private void addIpCount(InetAddress ip) {
		Integer ipCount = this.ipCountMap.get(ip);
		if (ipCount == null) {
			ipCount = Integer.valueOf(0);
		}
		int i = ipCount.intValue();
		ipCountMap.put(ip, ++i);
	}

}
