package com.att.aro.core.packetanalysis.pojo;

/**
 * A data class that consist of constants and child classes for storing constants.
 * 
 * @author Borey Sao
 * Date: April 18, 2014
 *
 */
public class TraceDataConst {
	
	public static final String OFF = "OFF";
	// GPS State keywords
	public static final String GPS_DISABLED = OFF;
	public static final String GPS_ACTIVE = "ACTIVE";
	public static final String GPS_STANDBY = "STANDBY";

	// Camera State keywords
	public static final String CAMERA_OFF = OFF;
	public static final String CAMERA_ON = "ON";

	// WiFi State keywords
	public static final String WIFI_OFF = OFF;
	public static final String WIFI_CONNECTED = "CONNECTED";
	public static final String WIFI_DISCONNECTED = "DISCONNECTED";
	public static final String WIFI_CONNECTING = "CONNECTING";
	public static final String WIFI_DISCONNECTING = "DISCONNECTING";
	public static final String WIFI_SUSPENDED = "SUSPENDED";
	
	// Wakelock State keywords
	public static final String WAKELOCK_RELEASED = "-wake_lock";
	public static final String WAKELOCK_ACQUIRED = "+wake_lock";

	// Bluetooth State keywords
	public static final String BLUETOOTH_OFF = OFF;
	public static final String BLUETOOTH_CONNECTED = "CONNECTED";
	public static final String BLUETOOTH_DISCONNECTED = "DISCONNECTED";

	// Screen State keywords
	public static final String SCREEN_OFF = OFF;
	public static final String SCREEN_ON = "ON";

	public static final int VALID_UNKNOWN_APP_ID = -1;
	public static final int PACKET_EOF = -127;
		
	/**
	 * group of constants for User Event
	 *
	 */
	public class UserEvent{
		public static final String PRESS = "press";
		public static final String RELEASE = "release";
		public static final String SCREEN = "screen";
		public static final String KEY = "key";
		public static final String KEY_POWER = "power";
		public static final String KEY_VOLUP = "volup";
		public static final String KEY_VOLDOWN = "voldown";
		public static final String KEY_BALL = "ball";
		public static final String KEY_HOME = "home";
		public static final String KEY_MENU = "menu";
		public static final String KEY_BACK = "back";
		public static final String KEY_SEARCH = "search";
		public static final String KEY_GREEN = "green";
		public static final String KEY_RED = "red";
		public static final String KEY_KEY = "key";
		public static final String KEY_LANDSCAPE = "landscape";
		public static final String KEY_PORTRAIT = "portrait";
	}
	/**
	 * value of network type found when reading trace file
	 *
	 */
	public class TraceNetworkType{
		public static final int NONE = 0;
		public static final int WIFI = -1;
		public static final int GPRS = 1;
		public static final int EDGE = 2;
		public static final int UMTS = 3;
		public static final int ETHERNET = 5;
		public static final int HSDPA = 8;
		public static final int HSUPA = 9;
		public static final int HSPA = 10;
		public static final int HSPAP = 15;
		public static final int LTE = 13;
	}
	public class FileName{
		/**
		 * The name of the active_process file
		 */
		public static final String ACTIVE_PROCESS_FILE = "active_process";

		/**
		 * The name of the prop file
		 */
		public static final String PROP_FILE = "prop";

		/**
		 * The name of the user input log events file
		 */
		public static final String USER_INPUT_LOG_EVENTS_FILE = "user_input_log_events";

		
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

		public static final String TRAFFIC = "traffic";
		public static final String CAP_EXT = ".cap";

		/**
		 * The name of the device_info file
		 */
		public static final String DEVICEINFO_FILE = "device_info";
		
		/**
		 * The name of the ssl file
		 */
		public static final String SSLKEY_FILE = "keys.ssl";
		
		/**
		 * The name of the network_details file
		 */
		public static final String NETWORKINFO_FILE = "network_details";

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
		 * The name of the Wakelock file
		 */
		public static final String BATTERYINFO_FILE = "batteryinfo_dump";

		/**
		 * The name of the Kernel log file
		 */
		public static final String KERNEL_LOG_FILE = "dmesg";

		/**
		 * The name of the Alarm dumpsys file
		 */
		public static final String ALARM_START_FILE = "alarm_info_start";
		public static final String ALARM_END_FILE = "alarm_info_end";
		
		/**
		 * The name of the WiFi file
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
		 * The name of the external video time file
		 */
		public static final String EXVIDEO_TIME_FILE = "exVideo_time";

		/**
		 * The name of the video MOV file
		 */
		public static final String VIDEO_MOV_FILE = "video.mov";

		/**
		 * The name of the video MP4 file
		 */
		public static final String VIDEO_MP4_FILE = "video.mp4";
	}
}
