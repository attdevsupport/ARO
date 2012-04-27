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
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 * To read the trace data from trace folder.Parse the date and convert it in to
 * the list of bean objects.
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

		// Configuration/profile/filter
		private Profile profile;
		private Map<String, ApplicationSelection> applicationSelections;

		// List of packets included in analysis (application filtered)
		private List<PacketInfo> packets;
		private Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();

		// Analysis results
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

		/**
		 * Constructor
		 * 
		 * @param profile
		 * @param applicationSelections
		 */
		private Analysis(Profile profile,
				Collection<ApplicationSelection> applicationSelections)
				throws IOException {
			this.profile = profile != null ? profile : new Profile3G();

			runAnalysis(applicationSelections);
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
		 * Returns the profifile associated with the trace data.
		 * 
		 * @return The profile
		 */
		public Profile getProfile() {
			return profile;
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
		 * Returns the application selections used in this analysis
		 * 
		 * @return A copy is returned to prevent inadvertant modification
		 */
		public Map<String, ApplicationSelection> getApplicationSelections() {

			// Return a copy
			Map<String, ApplicationSelection> result = new HashMap<String, ApplicationSelection>(
					applicationSelections.size());
			for (Map.Entry<String, ApplicationSelection> entry : applicationSelections
					.entrySet()) {
				result.put(entry.getKey(),
						new ApplicationSelection(entry.getValue()));
			}
			return result;
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
		 * Runs the analysis on the trace data using the current configuration
		 * 
		 * @param appSel
		 *            Selected applications to be included in analysis
		 * @throws IOException
		 */
		private synchronized void runAnalysis(
				Collection<ApplicationSelection> appSel) throws IOException {

			// Set up application filtering
			Map<String, ApplicationSelection> applicationSelections = new HashMap<String, ApplicationSelection>();
			Set<String> appNames = null;
			if (appSel != null) {

				// Build application selection map from input arg
				appNames = new HashSet<String>(appSel.size());
				for (ApplicationSelection as : appSel) {
					String appName = as.getAppName();
					if (TraceData.this.allAppNames.contains(appName)) {
						applicationSelections.put(appName, as);
						if (as.isSelected()) {
							appNames.add(appName);
						}
					}
				}

				// Filter packets based upon selected app names
				packets = new ArrayList<PacketInfo>();
				for (PacketInfo packet : TraceData.this.allPackets) {
					if (appNames.contains(packet.getAppName())) {
						packets.add(packet);
					}
				}
			} else {

				// No filter. Use all packets
				packets = TraceData.this.allPackets;
			}

			// Make sure all app names are represented
			for (String app : allAppNames) {
				if (!applicationSelections.containsKey(app)) {
					applicationSelections.put(app,
							new ApplicationSelection(app));
				}
			}
			this.applicationSelections = applicationSelections;

			// Collect basic statistics
			if (packets.size() > 0) {
				PacketInfo lastPacket = packets.get(packets.size() - 1);
				for (PacketInfo packet : packets) {
					totalBytes += packet.getLen();

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
					}

				}

				packetsDuration = lastPacket.getTimeStamp()
						- packets.get(0).getTimeStamp();
				avgKbps = totalBytes * 8.0 / 1000.0 / packetsDuration;
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
	private boolean endTraceTimeFound;
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
	 * Collects trace data
	 * 
	 * @param traceDir
	 *            Directory where trace files are located
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

			// Read standalone pcap file
			new PCapAdapter(traceDir, packetListener);
			this.pcapFile = traceDir;

			if (allPackets.size() > 0) {

				// Sort and set IDs on packets
				Collections.sort(allPackets);
				int i = 0;
				for (Iterator<PacketInfo> iter = allPackets.iterator(); iter
						.hasNext(); ++i) {
					PacketInfo packet = iter.next();

					// Filter out non-IP packets
					if (!(packet.getPacket() instanceof IPPacket)) {
						iter.remove();
						continue;
					}

					// Set packet ID to match Wireshark ID
					packet.setId(i + 1);

					// Set blank app name so that burst analysis is done
					packet.setAppName("");

				}

				pcapTime0 = allPackets.get(0).getTimeStamp();
				this.traceDateTime = new Date((long) (pcapTime0 * 1000));
				syncTime();
			} else {
				pcapTime0 = pcapFile.lastModified() / 1000;
				this.traceDateTime = new Date(pcapFile.lastModified());
			}
		}

	}

	/**
	 * Returns trace directory.
	 * 
	 * @return traceDir.
	 */
	public File getTraceDir() {
		return traceDir;
	}

	/**
	 * Returns date time of trace data.
	 * 
	 * @return traceDateTime.
	 */
	public Date getTraceDateTime() {
		return traceDateTime;
	}

	/**
	 * Returns the total duration of the loaded trace file.
	 * 
	 * @return The traceDuration
	 */
	public double getTraceDuration() {
		return traceDuration;
	}

	/**
	 * Returns the name of the collector.
	 * 
	 * @return The collectorName
	 */
	public String getCollectorName() {
		return collectorName;
	}

	/**
	 * Returns the model of the device.
	 * 
	 * @return The deviceModel
	 */
	public String getDeviceModel() {
		return deviceModel;
	}

	/**
	 * Returns the make of the device.
	 * 
	 * @return The deviceMake
	 */
	public String getDeviceMake() {
		return deviceMake;
	}

	/**
	 * Returns the type of the operating system.
	 * 
	 * @return The osType
	 */
	public String getOsType() {
		return osType;
	}

	/**
	 * Returns the version of the operating system.
	 * 
	 * @return The osVersion
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * Returns the version of the collector.
	 * 
	 * @return The collectorVersion
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
	 * Returns the active duration of GPS peripheral.
	 * 
	 * @return The gpsActiveDuration
	 */
	public double getGPSActiveDuration() {
		return gpsActiveDuration;
	}

	/**
	 * Returns the active duration of WiFi peripheral.
	 * 
	 * @return The wifiActiveDuration
	 */
	public double getWiFiActiveDuration() {
		return wifiActiveDuration;
	}

	/**
	 * Returns the active duration of bluetooth peripheral.
	 * 
	 * @return The bluetoothActiveDuration
	 */
	public double getBluetoothActiveDuration() {
		return bluetoothActiveDuration;
	}

	/**
	 * Returns the active duration of camera peripheral.
	 * 
	 * @return The cameraActiveDuration
	 */
	public double getCameraActiveDuration() {
		return cameraActiveDuration;
	}

	/**
	 * Returns all apps included in the trace including apps filtered from the
	 * analysis
	 * 
	 * @return The allAppNames
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
	 * Returns the list of GPS info.
	 * 
	 * @return The gpsInfos
	 */
	public List<GpsInfo> getGpsInfos() {
		return Collections.unmodifiableList(gpsInfos);
	}

	/**
	 * Returns the list of bluetooth info.
	 * 
	 * @return The bluetoothInfos
	 */
	public List<BluetoothInfo> getBluetoothInfos() {
		return Collections.unmodifiableList(bluetoothInfos);
	}

	/**
	 * Returns the list of WiFi info.
	 * 
	 * @return The wifiInfos
	 */
	public List<WifiInfo> getWifiInfos() {
		return Collections.unmodifiableList(wifiInfos);
	}

	/**
	 * Returns the list of camera info.
	 * 
	 * @return The cameraInfos
	 */
	public List<CameraInfo> getCameraInfos() {
		return Collections.unmodifiableList(cameraInfos);
	}

	/**
	 * Returns the list of screen state info.
	 * 
	 * @return The screenStateInfos
	 */
	public List<ScreenStateInfo> getScreenStateInfos() {
		return Collections.unmodifiableList(screenStateInfos);
	}

	/**
	 * Returns the list of battery info.
	 * 
	 * @return The batteryInfos
	 */
	public List<BatteryInfo> getBatteryInfos() {
		return Collections.unmodifiableList(batteryInfos);
	}

	/**
	 * Returns the list of userEvents.
	 * 
	 * @return The userEvents
	 */
	public List<UserEvent> getUserEvents() {
		return Collections.unmodifiableList(userEvents);
	}

	/**
	 * Returns the list of radioInfos.
	 * 
	 * @return The radioInfos
	 */
	public List<RadioInfo> getRadioInfos() {
		return Collections.unmodifiableList(radioInfos);
	}

	/**
	 * Returns the list of cpuActivity.
	 * 
	 * @return The cpuActivityList
	 */
	public List<CpuActivity> getCpuActivityList() {
		return Collections.unmodifiableList(cpuActivityList);
	}

	/**
	 * Returns the start time of video.
	 * 
	 * @return The videoStartTime
	 */
	public double getVideoStartTime() {
		return videoStartTime;
	}

	/**
	 * Returns the pcapFile.
	 * 
	 * @return The pcapFile
	 */
	public File getPcapFile() {
		return pcapFile;
	}

	/**
	 * Returns the set of missingFiles.
	 * 
	 * @return The missingFiles
	 */
	public Set<String> getMissingFiles() {
		return Collections.unmodifiableSet(missingFiles);
	}

	/**
	 * Runs analysis on the trace for the specified app names
	 * 
	 * @param profile
	 *            Device profile settings
	 * @param appSel
	 *            The collection of selected applications.
	 */
	public synchronized Analysis runAnalysis(Profile profile,
			Collection<ApplicationSelection> appSel) throws IOException {
		return new Analysis(profile, appSel);
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
	 * Reads the time form the time trace file.
	 * 
	 * @throws IOException
	 */
	private void readTime() throws IOException {

		File file = new File(traceDir, TIME_FILE);
		if (!file.exists()) {
			this.missingFiles.add(TIME_FILE);
		}

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String s;

			// Ignore first line
			br.readLine();

			// Second line is pcap time
			s = br.readLine();
			if (s != null) {
				this.pcapTime0 = Double.parseDouble(s);

				s = br.readLine();
				if (s != null) {
					this.eventTime0 = Double.parseDouble(s) / 1000.0;
				}

				s = br.readLine();
				if (s != null) {
					this.traceDuration = Double.parseDouble(s) - this.pcapTime0;
					this.endTraceTimeFound = true;
				}
			}
			this.traceDateTime = new Date((long) (pcapTime0 * 1000));

		} finally {
			br.close();
		}

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
	 * @throws IOException
	 */
	private void readPcapTrace() throws IOException {

		File pcap = new File(traceDir, PCAP_FILE);
		if (!pcap.exists() || pcap.length() == 0) {
			this.missingFiles.add(PCAP_FILE);
			logger.warning("No TCP data found in trace");
			return;
		}
		this.pcapFile = pcap;
		new PCapAdapter(pcap, packetListener);

		// Ignore the last packet (nexus one data collector's problem)
		// Don't know why this is done in prototype
		// Commented out so results match wireshark
		// allPackets.remove(allPackets.size() - 1);

		// Determine application name associated with each packet
		List<Integer> appIds = readAppIDs();
		int i = 0;
		for (Iterator<PacketInfo> iter = allPackets.iterator(); iter.hasNext(); ++i) {
			PacketInfo packet = iter.next();

			// Filter out non-IP packets
			if (!(packet.getPacket() instanceof IPPacket)) {
				iter.remove();
				continue;
			}

			if (i < appIds.size()) {
				int appId = appIds.get(i);

				// Check for valid application
				if (appId >= 0) {
					assert (appId < appInfos.size());

					String appName = appId < appInfos.size() ? appInfos
							.get(appId) : null;
					packet.setAppName(appName);
					this.allAppNames.add(appName);
				} else {

					// Should indicate unknown app ID
					assert (appId == PACKET_UNKNOWN_APP);
					this.allAppNames.add(null);
				}
			}

			// Set packet ID to match Wireshark ID
			packet.setId(i + 1);

		}

		Collections.sort(allPackets);
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
						checkLastEvent(dTimeStamp);
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
				double dTimeStamp = Double.parseDouble(strFields[0]);
				if (dTimeStamp > 1.0e9) {
					dTimeStamp = normalizeTime(dTimeStamp);
				} else {

					// Old data collector method (backward compatible)
					dTimeStamp -= eventTime0;
				}

				UserEventType eventType = null;

				if (strFields[1].contains(KEY_LANDSCAPE)) {
					eventType = UserEventType.SCREEN_LANDSCAPE;
				} else if (strFields[1].contains(KEY_PORTRAIT)) {
					eventType = UserEventType.SCREEN_PORTRAIT;
				}

				userEvents.add(new UserEvent(eventType, dTimeStamp,
						dTimeStamp + 0.5));
			}

		} finally {
			reader.close();
		}

	}

	/**
	 * Method to Read the data from the trace folder for all the files.
	 */
	private synchronized void readData() throws IOException {

		readAppInfo();

		// Read the time file
		readTime();

		readPcapTrace();

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

		syncTime();

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
		// Integer beginIdx = 0 ;

		// Integer endIdx = 0 ;
		// Double dShiftTS = 0.0 ;

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double dTimeStamp = 0.0;
			double dUsage = 0.0;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					dTimeStamp = Double.parseDouble(strFields[0]);
					dUsage = Double.parseDouble(strFields[1]);
					CpuActivity cpuActivity = new CpuActivity(dTimeStamp,
							dUsage);
					cpuActivityList.add(cpuActivity);
				}
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
			double dTimeStamp;
			double dLastActiveTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			GpsState gpsState = null;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					try {
						dTimeStamp = normalizeTime(Double
								.parseDouble(strFields[0]));
						if (GPS_STANDBY.equals(strFields[1])) {
							gpsState = GpsState.GPS_STANDBY;
						} else if (GPS_DISABLED.equals(strFields[1])) {
							gpsState = GpsState.GPS_DISABLED;
						} else if (GPS_ACTIVE.equals(strFields[1])) {
							gpsState = GpsState.GPS_ACTIVE;
							if (0.0 == dLastActiveTimeStamp) {
								dLastActiveTimeStamp = dTimeStamp;
							}
						} else {
							logger.warning("Invalid GPS state: " + strLineBuf);
							gpsState = GpsState.GPS_UNKNOWN;
						}
						gpsInfos.add(new GpsInfo(dTimeStamp, gpsState));

						if ((!GPS_ACTIVE.equals(strFields[1]))
								&& dLastActiveTimeStamp > 0.0) {
							dActiveDuration += (dTimeStamp - dLastActiveTimeStamp);
							dLastActiveTimeStamp = 0.0;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing GPS event: "
										+ strLineBuf, e);
					}
				} else {
					logger.warning("Invalid GPS trace entry: " + strLineBuf);
				}
			}

			// Duration calculation should probably be done in analysis
			if (gpsState == GpsState.GPS_ACTIVE) {
				dActiveDuration += Math.max(0, getTraceDuration()
						- dLastActiveTimeStamp);
			}

			this.gpsActiveDuration = dActiveDuration;
			Collections.sort(gpsInfos);
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
			double dTimeStamp;
			double dLastTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			BluetoothState btState;
			BluetoothState lastState = null;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					try {
						dTimeStamp = normalizeTime(Double
								.parseDouble(strFields[0]));
						if (BLUETOOTH_CONNECTED.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_CONNECTED;
						} else if (BLUETOOTH_DISCONNECTED.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_DISCONNECTED;
						} else if (BLUETOOTH_OFF.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_TURNED_OFF;
						} else {
							logger.warning("Unknown bluetooth state: "
									+ strLineBuf);
							btState = BluetoothState.BLUETOOTH_UNKNOWN;
						}
						bluetoothInfos.add(new BluetoothInfo(dTimeStamp,
								btState));

						if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
							dActiveDuration += (dTimeStamp - dLastTimeStamp);
						}
						lastState = btState;
						dLastTimeStamp = dTimeStamp;
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

			// Duration calculation should probably be done in analysis
			if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
				dActiveDuration += Math.max(0, getTraceDuration()
						- dLastTimeStamp);
			}

			this.bluetoothActiveDuration = dActiveDuration;
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
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			double dTimeStamp;
			double dLastTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			WifiState wifiState;
			WifiState lastWifiState = null;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				try {
					if (strFields.length >= 2) {
						String macAddress = null;
						String rssi = null;
						String ssid = null;
						dTimeStamp = normalizeTime(Double
								.parseDouble(strFields[0]));
						if (WIFI_OFF.equals(strFields[1])) {
							wifiState = WifiState.WIFI_DISABLED;
						} else if (WIFI_CONNECTED.equals(strFields[1])) {
							wifiState = WifiState.WIFI_CONNECTED;
							Matcher matcher = wifiPattern.matcher(strLineBuf);
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
							logger.warning("Unknown wifi state: " + strLineBuf);
							wifiState = WifiState.WIFI_UNKNOWN;
						}

						if (wifiState != lastWifiState) {
							wifiInfos.add(new WifiInfo(dTimeStamp, wifiState,
									macAddress, rssi, ssid));
							if (lastWifiState == WifiState.WIFI_CONNECTED
									|| lastWifiState == WifiState.WIFI_CONNECTING
									|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
								dActiveDuration += (dTimeStamp - dLastTimeStamp);
							}
							lastWifiState = wifiState;
							dLastTimeStamp = dTimeStamp;
						}
					} else {
						logger.warning("Invalid WiFi trace entry: "
								+ strLineBuf);
					}
				} catch (Exception e) {
					logger.log(
							Level.WARNING,
							"Unexpected error parsing GPS event: " + strLineBuf,
							e);
				}

			}

			// Duration calculation should probably be done in analysis
			if (lastWifiState == WifiState.WIFI_CONNECTED
					|| lastWifiState == WifiState.WIFI_CONNECTING
					|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
				dActiveDuration += Math.max(0, getTraceDuration()
						- dLastTimeStamp);
			}

			this.wifiActiveDuration = dActiveDuration;
			Collections.sort(wifiInfos);
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
			double dTimeStamp;
			double dLastActiveTimeStamp = 0.0;
			double dActiveDuration = 0.0;
			CameraState cameraState = null;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					try {
						dTimeStamp = normalizeTime(Double
								.parseDouble(strFields[0]));
						if (CAMERA_ON.equals(strFields[1])) {
							cameraState = CameraState.CAMERA_ON;
							if (0.0 == dLastActiveTimeStamp) {
								dLastActiveTimeStamp = dTimeStamp;
							}
						} else if (CAMERA_OFF.equals(strFields[1])) {
							cameraState = CameraState.CAMERA_OFF;
						} else {
							logger.warning("Unknown camera state: "
									+ strLineBuf);
							cameraState = CameraState.CAMERA_UNKNOWN;
						}
						cameraInfos
								.add(new CameraInfo(dTimeStamp, cameraState));

						if ((!CAMERA_ON.equals(strFields[1]))
								&& dLastActiveTimeStamp > 0.0) {
							dActiveDuration += (dTimeStamp - dLastActiveTimeStamp);
							dLastActiveTimeStamp = 0.0;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error in camera events: "
										+ strLineBuf, e);
					}
				} else {
					logger.warning("Unrecognized camera event: " + strLineBuf);
				}
			}

			// Duration calculation should probably be done in analysis
			if (cameraState == CameraState.CAMERA_ON) {
				dActiveDuration += Math.max(0, getTraceDuration()
						- dLastActiveTimeStamp);
			}

			this.cameraActiveDuration = dActiveDuration;
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
			double dTimeStamp;
			ScreenState screenState;
			String brightness;
			int timeout;
			for (String strLineBuf = br.readLine(); strLineBuf != null; strLineBuf = br
					.readLine()) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length >= 2) {
					try {
						dTimeStamp = normalizeTime(Double
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
								dTimeStamp, screenState, brightness, timeout);
						screenStateInfos.add(screenInfo);
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
		} finally {
			br.close();
		}
	}

	/**
	 * Method to read the Battery data from the trace file and store it in the
	 * batteryInfos list.
	 */
	private void readBattery() throws IOException {
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
						batteryInfos.add(new BatteryInfo(bTimeStamp, bState,
								bLevel, bTemp));
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
				if (strFields.length == 2) {
					try {
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
					} catch (Exception e) {
						logger.log(Level.WARNING,
								"Unexpected error parsing radio event: "
										+ strLineBuf, e);
					}
				} else {
					logger.warning("Invalid radio_events entry: " + strLineBuf);
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
	 * Syncs the time.
	 */
	private void syncTime() {
		// MyAssert(nPackets > 0, 220);
		// MyAssert(pcapTime0 <= packets[0].ts, 221);
		for (PacketInfo packet : allPackets) {
			double dTimeStamp = packet.getTimeStamp() - pcapTime0;
			packet.setTimestamp(dTimeStamp);
			checkLastEvent(dTimeStamp);

			if (packet.getPacket() instanceof IPPacket) {
				IPPacket ip = (IPPacket) packet.getPacket();
				packet.setDir(determinePacketDirection(ip.getSourceIPAddress(),
						ip.getDestinationIPAddress()));
			}
		}
		int nCPU = cpuActivityList.size();
		if (nCPU > 0) {
			for (int i = 0; i < nCPU; i++) {
				double dNewTimeStamp = cpuActivityList.get(i).getCpuTimeStamp()
						- pcapTime0;
				cpuActivityList.get(i).setCpuTimeStamp(dNewTimeStamp);
				checkLastEvent(dNewTimeStamp);
			}
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
		checkLastEvent(time);
		if (time < 0) {
			return 0.0;
		}
		return time;
	}

	/**
	 * validate the end of trace time.
	 * 
	 * @param time
	 */
	private void checkLastEvent(double time) {
		if (!endTraceTimeFound) {
			this.traceDuration = Math.max(this.traceDuration, time);
		}
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
