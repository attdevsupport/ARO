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
package com.att.aro.core.configuration.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a device profile that is used as a model of the device when analyzing trace data.
 */
public abstract class Profile implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * The Carrier/Network provider name.
	 */
	public static final String CARRIER = "CARRIER";

	/**
	 * The device name. 
	 */
	public static final String DEVICE = "DEVICE";

	/**
	 * The network type (i.e. 3G or LTE) of the Profile.
	 */
	public static final String PROFILE_TYPE = "PROFILE_TYPE";

	/**
	 * The threshold for the user input window (in seconds).
	 */
	public static final String USER_INPUT_TH = "USER_INPUT_TH";

	/**
	 * The average amount of power (in watts) that should be used when GPS is in the Active state. 
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
	 * The average amount of power (in watts) that should be used when Bluetooth is in the Active state. 
	 */
	public static final String POWER_BLUETOOTH_ACTIVE = "POWER_BLUETOOTH_ACTIVE";

	/**
	 * The average amount of power that should be used (in watts) when 
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
	 * The threshold for defining a long burst (in seconds).
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
	 * The threshold for duration of a large burst (in seconds).
	 */
	public static final String LARGE_BURST_DURATION = "LARGE_BURST_DURATION";

	/**
	 * The threshold for the size of a large burst (in bytes).
	 */
	public static final String LARGE_BURST_SIZE = "LARGE_BURST_SIZE";

	/**
	 * The threshold for close spaced bursts (sec).
	 */
	public static final String CLOSE_SPACED_BURSTS = "CLOSE_SPACED_BURSTS";

	/**
	 * The time delta for throughput calculations.
	 */
	public static final String W_THROUGHPUT = "W_THROUGHPUT";
	
	private Map<String, String> errorLog = new HashMap<String, String>();
	private boolean init;
	private String name;
	private String carrier = "AT&T";
	private String device = "Captivate - ad study";

	private double userInputTh = 1.0;
	private double powerGpsActive = 0.28;//1.0;
	private double powerGpsStandby = 0.02;//0.5;
	private double powerCameraOn = 0.95;//0.3;
	private double powerBluetoothActive = 0.761;//1.0;
	private double powerBluetoothStandby = 0.02;//0.5;
	private double powerScreenOn = 0.58;//0.3;
	private double burstTh = 1.5;
	private double longBurstTh = 5.0;
	private double periodMinCycle = 10.0;
	private double periodCycleTol = 1.0;
	private int periodMinSamples = 3;
	private double largeBurstDuration = 5.0;
	private int largeBurstSize = 100000;
	private double closeSpacedBurstThreshold = 10.0;
	private double throughputWindow = 0.5;

	/**
	 * Returns the type of profile. Subclasses of this class must identify the profile type.
	 * 
	 * @return The profile type. One of the values of the ProfileType enumeration.
	 */
	public abstract ProfileType getProfileType();

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
	 * Returns the minimum tolerable variation for periodical bursts (in seconds).
	 * 
	 * @return The minimum tolerable variation
	 */
	public double getPeriodMinCycle() {
		return periodMinCycle;
	}

	/**
	 * Returns the maximum tolerable variation for periodical bursts (in seconds).
	 * 
	 * @return The maximum tolerable variation.
	 */
	public double getPeriodCycleTol() {
		return periodCycleTol;
	}

	/**
	 * Returns the the minimum amount of observed samples for periodical transfers 
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
	 * Returns the threshold for close spaced bursts (sec).
	 * @return Threshold for close spaced bursts (sec)
	 */
	public double getCloseSpacedBurstThreshold() {
		return closeSpacedBurstThreshold;
	}

	/**
	 * Returns the throughput window.
	 * @return The throughput window value.
	 */
	public double getThroughputWindow() {
		return throughputWindow;
	}

	/**
	 * Sets the throughput window to the specified value.
	 * @param throughputWindow
	 *            - The throughput window value to set.
	 */
	public void setThroughputWindow(double throughputWindow) {
		this.throughputWindow = throughputWindow;
	}

	public Map<String, String> getErrorLog() {
		return errorLog;
	}

	public void setErrorLog(Map<String, String> errorLog) {
		this.errorLog = errorLog;
	}

	public boolean isInit() {
		return init;
	}

	public void setInit(boolean init) {
		this.init = init;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public void setUserInputTh(double userInputTh) {
		this.userInputTh = userInputTh;
	}

	public void setPowerGpsActive(double powerGpsActive) {
		this.powerGpsActive = powerGpsActive;
	}

	public void setPowerGpsStandby(double powerGpsStandby) {
		this.powerGpsStandby = powerGpsStandby;
	}

	public void setPowerCameraOn(double powerCameraOn) {
		this.powerCameraOn = powerCameraOn;
	}

	public void setPowerBluetoothActive(double powerBluetoothActive) {
		this.powerBluetoothActive = powerBluetoothActive;
	}

	public void setPowerBluetoothStandby(double powerBluetoothStandby) {
		this.powerBluetoothStandby = powerBluetoothStandby;
	}

	public void setPowerScreenOn(double powerScreenOn) {
		this.powerScreenOn = powerScreenOn;
	}

	public void setBurstTh(double burstTh) {
		this.burstTh = burstTh;
	}

	public void setLongBurstTh(double longBurstTh) {
		this.longBurstTh = longBurstTh;
	}

	public void setPeriodMinCycle(double periodMinCycle) {
		this.periodMinCycle = periodMinCycle;
	}

	public void setPeriodCycleTol(double periodCycleTol) {
		this.periodCycleTol = periodCycleTol;
	}

	public void setPeriodMinSamples(int periodMinSamples) {
		this.periodMinSamples = periodMinSamples;
	}

	public void setLargeBurstDuration(double largeBurstDuration) {
		this.largeBurstDuration = largeBurstDuration;
	}

	public void setLargeBurstSize(int largeBurstSize) {
		this.largeBurstSize = largeBurstSize;
	}

	public void setCloseSpacedBurstThreshold(double closeSpacedBurstThreshold) {
		this.closeSpacedBurstThreshold = closeSpacedBurstThreshold;
	}
	
}
