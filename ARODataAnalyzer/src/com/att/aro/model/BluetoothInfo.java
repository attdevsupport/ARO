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
	 * The BluetoothInfo.BluetoothState Enumeration specifies constant values that describe the connectivity 
	 * state of the Bluetooth peripheral on a device. This enumeration is part of the 
	 * BluetoothInfo class.
	 */
	public enum BluetoothState {
		/**
		 * The Bluetooth peripheral is of an unknown type.
		 */
		BLUETOOTH_UNKNOWN,
		/**
		 * The Bluetooth peripheral is disconnected.
		 */
		BLUETOOTH_DISCONNECTED,
		/**
		 * The Bluetooth peripheral is connected.
		 */
		BLUETOOTH_CONNECTED,
		/**
		 * The Bluetooth peripheral is switched off. 
		 */
		BLUETOOTH_TURNED_OFF,
	}

	// Bluetooth Time stamp
	private double beginTimeStamp;
	private double endTimeStamp;

	// Current Bluetooth State
	private BluetoothState bluetoothState;

	/**
	 * Initializes an instance of the BluetoothInfo class, using the specified time stamp, 
	 * and Bluetooth state.
	 * 
	 *  @param beginTimeStamp
	 *            The starting timestamp for the Bluetooth state.
	 * @param endTimeStamp
	 *            The ending timestamp for the Bluetooth state.
	 * @param bluetoothState - The activation state of Bluetooth on the device.
	 */
	public BluetoothInfo(double beginTimeStamp , double endTimeStamp , BluetoothState bluetoothState) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.bluetoothState = bluetoothState;
	}

	/**
	 * Returns the starting timestamp for the Bluetooth activation state. 
	 * 
	 * @return The starting timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}
	
	 /** Returns the ending timestamp for the Bluetooth activation state.  
	 * 
	 * @return The ending timestamp.
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
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
