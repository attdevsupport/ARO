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
