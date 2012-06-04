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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a device profile that is used as a model of the device when analyzing trace data.
 */
public abstract class Profile implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Carrier/Network provider name.
	 */
	public static final String CARRIER = "CARRIER";

	/**
	 * The device name. 
	 */
	public static final String DEVICE = "DEVICE";

	/**
	 * Profile network type
	 */
	public static final String PROFILE_TYPE = "PROFILE_TYPE";

	/**
	 * Threshold for user input window (seconds).
	 */
	public static final String USER_INPUT_TH = "USER_INPUT_TH";

	/**
	 * The average amount of power (in watts) that should be used when the camera is on. 
	 */
	public static final String POWER_GPS_ACTIVE = "POWER_GPS_ACTIVE";

	/**
	 * The average amount of power (in watts) that should be used when GPS is in the 
	 * Standby state. 
	 */
	public static final String POWER_GPS_STANDBY = "POWER_GPS_STANDBY";

	/**
	 * The average amount of power (in watts) that should be used when the camera is on. 
	 */
	public static final String POWER_CAMERA_ON = "POWER_CAMERA_ON";

	/**
	 * The average amount of power (in watts) that should be used when WiFi is in the Active state. 
	 */
	public static final String POWER_WIFI_ACTIVE = "POWER_WIFI_ACTIVE";

	/**
	 * The average amount of power (in watts) that should be used when WiFi is in the Standby state. 
	 */
	public static final String POWER_WIFI_STANDBY = "POWER_WIFI_STANDBY";

	/**
	 * The minimum amount of observed samples for periodical bursts. 
	 */
	public static final String POWER_BLUETOOTH_ACTIVE = "POWER_BLUETOOTH_ACTIVE";

	/**
	 * The average amount of power (in watts) that should be used (in watts) when 
	 * Bluetooth is in the Standby state.
	 */
	public static final String POWER_BLUETOOTH_STANDBY = "POWER_BLUETOOTH_STANDBY";

	/**
	 * The average amount of power (in watts) that should be used when the screen is on. 
	 */
	public static final String POWER_SCREEN_ON = "POWER_SCREEN_ON";

	/**
	 * The threshold for defining a burst (in seconds). 
	 */
	public static final String BURST_TH = "BURST_TH";

	/**
	 * Threshold for defining a long burst (seconds).
	 */
	public static final String LONG_BURST_TH = "LONG_BURST_TH";

	/**
	 * The minimum tolerable variation for periodical bursts (in seconds). 
	 */
	public static final String PERIOD_MIN_CYCLE = "PERIOD_MIN_CYCLE";

	/**
	 * The maximum tolerable variation for periodical bursts (in seconds). 
	 */
	public static final String PERIOD_CYCLE_TOL = "PERIOD_CYCLE_TOL";

	/**
	 * The minimum amount of observed samples for periodical bursts. 
	 */
	public static final String PERIOD_MIN_SAMPLES = "PERIOD_MIN_SAMPLES";

	/**
	 * Threshold for duration of a large burst (seconds).
	 */
	public static final String LARGE_BURST_DURATION = "LARGE_BURST_DURATION";

	/**
	 * Threshold for size of a large burst (bytes).
	 */
	public static final String LARGE_BURST_SIZE = "LARGE_BURST_SIZE";

	/**
	 * Time delta for throughput calculations.
	 */
	public static final String W_THROUGHPUT = "W_THROUGHPUT";

	/**
	 * Factory method that creates a new profile of the proper type from the
	 * specified properties file
	 * 
	 * @param file
	 *            Properties file
	 * @return The result profile object
	 * @throws IOException
	 *             when an error occurs accessing the file
	 * @throws ProfileException
	 *             when an error occurs reading the profile data
	 */
	public static Profile createFromFile(File file) throws IOException,
			ProfileException {
		FileReader reader = new FileReader(file);
		try {
			Properties props = new Properties();
			props.load(reader);

			String stype = props.getProperty(PROFILE_TYPE);
			ProfileType type = stype != null ? ProfileType.valueOf(stype)
					: ProfileType.T3G;
			switch (type) {
			case T3G:
				return new Profile3G(file, props);
			case LTE:
				return new ProfileLTE(file, props);
			default:
				throw new IllegalArgumentException("Invalid profile type: "
						+ type);
			}
		} finally {
			reader.close();
		}
	}

	/**
	 * Factory method that creates a new profile of the proper type from the
	 * properties read from the specified InputStream.
	 * 
	 * @param name
	 * @param input
	 *            The input stream to be read.
	 * @return The profile that is created.
	 * @throws IOException
	 * @throws ProfileException
	 */
	public static Profile createFromInputStream(String name, InputStream input)
			throws IOException, ProfileException {
		Properties props = new Properties();
		props.load(input);
		String stype = props.getProperty(PROFILE_TYPE);
		ProfileType type = stype != null ? ProfileType.valueOf(stype)
				: ProfileType.T3G;
		switch (type) {
		case T3G:
			return new Profile3G(name, props);
		case LTE:
			return new ProfileLTE(name, props);
		default:
			throw new IllegalArgumentException("Invalid profile type: " + type);
		}
	}

	private Map<String, String> errorLog = new HashMap<String, String>();
	private boolean init;

	private File file;
	private String name;
	private String carrier = "AT&T";
	private String device = "Captivate - ad study";

	private double userInputTh = 1.0;
	private double powerGpsActive = 1.0;
	private double powerGpsStandby = 0.5;
	private double powerCameraOn = 0.3;
	private double powerWifiActive = 1.0;
	private double powerWifiStandby = 0.5;
	private double powerBluetoothActive = 1.0;
	private double powerBluetoothStandby = 0.5;
	private double powerScreenOn = 0.3;
	private double burstTh = 1.5;
	private double longBurstTh = 5.0;
	private double periodMinCycle = 10.0;
	private double periodCycleTol = 1.0;
	private int periodMinSamples = 3;
	private double largeBurstDuration = 5.0;
	private int largeBurstSize = 100000;
	private double throughputWindow = 0.5;

	/**
	 * Initializes an instance of the Profile class.
	 */
	public Profile() {
		super();
	}

	/**
	 * Initializes an instance of the Profile class, using the specified properties.
	 * 
	 * @param file - A file where the profile can be saved. This argument can be null. 
	 * 
	 * @param properties – A Properties object containing the profile properties.
	 * 
	 * @throws ProfileException
	 */
	public Profile(File file, Properties properties) throws ProfileException {
		this.file = file;
		if (file != null) {
			name = file.getAbsolutePath();
		}
		init(properties);
	}

	/**
	 * Initializes an instance of the Profile class, using the specified properties. 
	 * 
	 * @param name - The name of the profile. The name is either an absolute path to the file that 
	 * holds this profile, or the name of a pre-defined profile .
	 * 
	 * @param properties - A Properties object containing the profile properties.
	 * 
	 * @throws ProfileException
	 */
	public Profile(String name, Properties properties) throws ProfileException {
		this.name = name;
		init(properties);
	}

	/**
	 * A utility method for calculating RRC energy.
	 * 
	 * @param time1 – A beginning time value.
	 * 
	 * @param time2 – An ending time value.
	 * 
	 * @param state – An RRCState enumeration value that indicates the RRC energy state.
	 * 
	 * @param packets - List of packets passed over the timeline and may be used
	 * in determining energy used
	 * 
	 * @return The RRC energy value.
	 */
	public abstract double energy(double time1, double time2, RRCState state,
			List<PacketInfo> packets);

	/**
	 * Subclasses must identify profile type
	 * 
	 * @return The profile type
	 */
	public abstract ProfileType getProfileType();

	/**
	 * Stores the current profile values contained in this object, in the specified file. 
	 * 
	 * @param file – The absolute path to the location where the profile values should be stored. 
	 * 
	 * @throws IOException
	 */
	public synchronized final void saveToFile(File file) throws IOException {
		Properties props = new Properties();

		// Get sub-class data
		saveProperties(props);

		props.setProperty(CARRIER, carrier);
		props.setProperty(DEVICE, device);
		props.setProperty(PROFILE_TYPE, getProfileType().name());
		props.setProperty(USER_INPUT_TH, String.valueOf(userInputTh));
		props.setProperty(POWER_GPS_ACTIVE, String.valueOf(powerGpsActive));
		props.setProperty(POWER_GPS_STANDBY, String.valueOf(powerGpsStandby));
		props.setProperty(POWER_CAMERA_ON, String.valueOf(powerCameraOn));
		props.setProperty(POWER_WIFI_ACTIVE, String.valueOf(powerWifiActive));
		props.setProperty(POWER_WIFI_STANDBY, String.valueOf(powerWifiStandby));
		props.setProperty(POWER_BLUETOOTH_ACTIVE,
				String.valueOf(powerBluetoothActive));
		props.setProperty(POWER_BLUETOOTH_STANDBY,
				String.valueOf(powerBluetoothStandby));
		props.setProperty(POWER_SCREEN_ON, String.valueOf(powerScreenOn));
		props.setProperty(BURST_TH, String.valueOf(burstTh));
		props.setProperty(LONG_BURST_TH, String.valueOf(longBurstTh));
		props.setProperty(PERIOD_MIN_CYCLE, String.valueOf(periodMinCycle));
		props.setProperty(PERIOD_CYCLE_TOL, String.valueOf(periodCycleTol));
		props.setProperty(PERIOD_MIN_SAMPLES, String.valueOf(periodMinSamples));
		props.setProperty(LARGE_BURST_DURATION,
				String.valueOf(largeBurstDuration));
		props.setProperty(LARGE_BURST_SIZE, String.valueOf(largeBurstSize));

		props.setProperty(W_THROUGHPUT, String.valueOf(throughputWindow));
		props.store(new FileOutputStream(file), "Set what this comment is");
		this.file = file;
		this.name = file.getAbsolutePath();
	}

	/**
	 * Returns the name of this profile. The name is either an absolute path to the file that holds 
	 * this profile, or the name of a pre-defined profile.
	 * 
	 * @return The profile name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the profile file. 
	 * 
	 * @return The file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the carrier. 
	 * 
	 * @return The carrier name.
	 */
	public String getCarrier() {
		return carrier;
	}

	/**
	 * Returns the name of the device. 
	 * 
	 * @return A string containing the device name.
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * Returns the user input threshold. 
	 * 
	 * @return The user input threshold value.
	 */
	public double getUserInputTh() {
		return userInputTh;
	}

	/**
	 * Returns the amount of energy used when GPS is active. 
	 * 
	 * @return The GPS active power value.
	 */
	public double getPowerGpsActive() {
		return powerGpsActive;
	}

	/**
	 * Returns the amount of energy used when GPS is in standby mode. 
	 * 
	 * @return The GPS standby mode power value.
	 */
	public double getPowerGpsStandby() {
		return powerGpsStandby;
	}

	/**
	 * Returns the amount of energy used when the camera is ON. 
	 * 
	 * @return The amount of power used when the camera is ON.
	 */
	public double getPowerCameraOn() {
		return powerCameraOn;
	}

	/**
	 * Returns the amount of energy used when WiFi is active. 
	 * 
	 * @return The WiFi active power value.
	 */
	public double getPowerWifiActive() {
		return powerWifiActive;
	}

	/**
	 * Returns the amount of energy used when WiFi is in standby mode. 
	 * 
	 * @return The WiFi standby mode power value.
	 */
	public double getPowerWifiStandby() {
		return powerWifiStandby;
	}

	/**
	 * Returns the amount of energy used when Bluetooth is active. 
	 * 
	 * @return The Bluetooth active power value.
	 */
	public double getPowerBluetoothActive() {
		return powerBluetoothActive;
	}

	/**
	 * Returns the amount of energy used when Bluetooth is in standby mode. 
	 * 
	 * @return The Bluetooth standby mode power value.
	 */
	public double getPowerBluetoothStandby() {
		return powerBluetoothStandby;
	}

	/**
	 * Returns the total amount of energy used when the device screen is ON. 
	 * 
	 * @return The screen ON power value.
	 */
	public double getPowerScreenOn() {
		return powerScreenOn;
	}

	/**
	 * Returns the value of the burst threshold. 
	 * 
	 * @return The burst threshold.
	 */
	public double getBurstTh() {
		return burstTh;
	}

	/**
	 * Returns the value of the long burst threshold. 
	 * 
	 * @return The long burst threshold value.
	 */
	public double getLongBurstTh() {
		return longBurstTh;
	}

	/**
	 * Returns the minimum amount of energy used during the cycle period. 
	 * 
	 * @return The minimum cycle period value.
	 */
	public double getPeriodMinCycle() {
		return periodMinCycle;
	}

	/**
	 * Returns the amount of energy used during the total cycle period. 
	 * 
	 * @return The total cycle period value.
	 */
	public double getPeriodCycleTol() {
		return periodCycleTol;
	}

	/**
	 * Returns the value of the minimum sample period. 
	 * 
	 * @return The minimum sample period value.
	 */
	public int getPeriodMinSamples() {
		return periodMinSamples;
	}

	/**
	 * Returns the total duration of all large bursts. 
	 * 
	 * @return The large burst duration value.
	 */
	public double getLargeBurstDuration() {
		return largeBurstDuration;
	}

	/**
	 * Returns the total size of all large bursts. 
	 * 
	 * @return The large burst size value (in bytes).
	 */
	public int getLargeBurstSize() {
		return largeBurstSize;
	}

	/**
	 * @return the throughputWindow
	 */
	public double getThroughputWindow() {
		return throughputWindow;
	}

	/**
	 * @param throughputWindow
	 *            the throughputWindow to set
	 */
	public void setThroughputWindow(double throughputWindow) {
		this.throughputWindow = throughputWindow;
	}

	/**
	 * @param properties
	 *            The profile properties to be read.
	 * @param attribute
	 *            The attribute name whose value is to be read.
	 * @param defaultVal
	 *            The default value for the attribute.
	 * @return The value of the specified attribute for the profile.
	 */
	protected double readDouble(Properties properties, String attribute,
			double defaultVal) {
		String value = properties.getProperty(attribute);
		try {
			if (value != null) {
				init = true;
				return Double.parseDouble(value);
			} else {
				return defaultVal;
			}
		} catch (NumberFormatException e) {
			errorLog.put(attribute, value);
			return defaultVal;
		}
	}

	/**
	 * 
	 * @param properties
	 *            The profile properties to be read.
	 * @param attribute
	 *            The attribute name whose value is to be read.
	 * @param defaultVal
	 *            The default value for the attribute.
	 * @return The value of the specified attribute for the profile.
	 */
	protected int readInt(Properties properties, String attribute,
			int defaultVal) {
		String value = properties.getProperty(attribute);
		try {
			if (value != null) {
				init = true;
				return Integer.parseInt(value);
			} else {
				return defaultVal;
			}
		} catch (NumberFormatException e) {
			errorLog.put(attribute, value);
			return defaultVal;
		}
	}

	/**
	 * Sub-classes override this method to initialize class members from a
	 * properties object
	 * 
	 * @param properties
	 *            contains property values to set
	 */
	protected abstract void setProperties(Properties properties);

	/**
	 * Allows sub-classes to save member values to properties object for
	 * persistence.
	 * 
	 * @param properties
	 */
	protected abstract void saveProperties(Properties properties);

	/**
	 * Initialize the Profile values from the provided Properties object.
	 * 
	 * @param properties
	 *            Object that contains profile values.
	 * @throws ProfileException
	 */
	private synchronized void init(Properties properties)
			throws ProfileException {

		carrier = properties.getProperty(CARRIER);
		device = properties.getProperty(DEVICE);
		userInputTh = readDouble(properties, USER_INPUT_TH, userInputTh);
		powerGpsActive = readDouble(properties, POWER_GPS_ACTIVE,
				powerGpsActive);
		powerGpsStandby = readDouble(properties, POWER_GPS_STANDBY,
				powerGpsStandby);
		powerCameraOn = readDouble(properties, POWER_CAMERA_ON, powerCameraOn);
		powerWifiActive = readDouble(properties, POWER_WIFI_ACTIVE,
				powerGpsActive);
		powerWifiStandby = readDouble(properties, POWER_WIFI_STANDBY,
				powerGpsStandby);
		powerBluetoothActive = readDouble(properties, POWER_BLUETOOTH_ACTIVE,
				powerGpsActive);
		powerBluetoothStandby = readDouble(properties, POWER_BLUETOOTH_STANDBY,
				powerGpsStandby);
		powerScreenOn = readDouble(properties, POWER_SCREEN_ON, powerCameraOn);
		burstTh = readDouble(properties, BURST_TH, burstTh);
		longBurstTh = readDouble(properties, LONG_BURST_TH, longBurstTh);
		periodMinCycle = readDouble(properties, PERIOD_MIN_CYCLE,
				periodMinCycle);
		periodCycleTol = readDouble(properties, PERIOD_CYCLE_TOL,
				periodCycleTol);
		periodMinSamples = readInt(properties, PERIOD_MIN_SAMPLES,
				periodMinSamples);
		largeBurstDuration = readDouble(properties, LARGE_BURST_DURATION,
				largeBurstDuration);
		largeBurstSize = readInt(properties, LARGE_BURST_SIZE, largeBurstSize);
		throughputWindow = readDouble(properties, W_THROUGHPUT,
				throughputWindow);

		// Initialize sub-class members
		setProperties(properties);

		if (!errorLog.isEmpty()) {
			throw new ProfileException(errorLog);
		} else if (!init) {
			throw new ProfileException();
		}
	}

}// end Profile Class