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

/**
 * Contains  Bluetooth information for a device, and provides methods that indicate if and 
 * when Bluetooth was activated.
 */
public class BluetoothInfo {

	/**
	 * ENUM to maintain the bluetooth states.
	 */
	public enum BluetoothState {
		/**
		 * Unknown type.
		 */
		BLUETOOTH_UNKNOWN,
		/**
		 * Bluetooth pair device is disconnected.
		 */
		BLUETOOTH_DISCONNECTED,
		/**
		 * Bluetooth device connected.
		 */
		BLUETOOTH_CONNECTED,
		/**
		 * Bluetooth switched off.
		 */
		BLUETOOTH_TURNED_OFF,
	}

	// Bluetooth Time stamp
	private double bluetoothTimeStamp;

	// Current Bluetooth State
	private BluetoothState bluetoothState;

	/**
	 * Initializes an instance of the BluetoothInfo class, using the specified time stamp, 
	 * and Bluetooth state.
	 * 
	 * @param dTimestamp - The time at which the Bluetooth information was modified.
	 * @param bluetoothState - The activation state of Bluetooth on the device.
	 */
	public BluetoothInfo(double dTimestamp, BluetoothState bluetoothState) {
		this.bluetoothTimeStamp = dTimestamp;
		this.bluetoothState = bluetoothState;
	}

	/**
	 * Returns the time stamp of when the Bluetooth information was last modified. 
	 * 
	 * @return A double that is the time stamp of when the Bluetooth information was last 
	 * modified.
	 */
	public double getBluetoothTimeStamp() {
		return bluetoothTimeStamp;
	}

	/**
	 * Returns the Bluetooth activation state. 
	 * 
	 * @return A Boolean value that is true if Bluetooth is activated, and false if it is 
	 * not.
	 */
	public BluetoothState getBluetoothState() {
		return bluetoothState;
	}

}
