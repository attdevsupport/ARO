package com.att.aro.core.datacollector.pojo;

/**
 * Enumeration that defines valid states for this data collector bridge
 */
public enum CollectorStatus {
	/**
	 * The Data Collector is ready to run on the device/emulator.
	 */
	READY,
	/**
	 * The Data Collector is starting to run on the device/emulator.
	 */
	STARTING,
	/**
	 * The Data Collector has started running on the device/emulator.
	 */
	STARTED,
	/**
	 * The Data Collector is preparing to stop running on the device/emulator.
	 */
	STOPPING,
	/**
	 * The Data Collector has stopped running on the device/emulator.
	 */
	STOPPED,
	/**
	 * The Data Collector is pulling trace data from the sdcard of the device/emulator.
	 */
	PULLING
}
