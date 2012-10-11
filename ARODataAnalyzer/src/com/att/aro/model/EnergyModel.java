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

import java.io.Serializable;
import java.util.Iterator;

import com.att.aro.model.GpsInfo.GpsState;

/**
 * Contains methods for generating energy statistics from trace analysis data. 
 */
public class EnergyModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private TraceData.Analysis analysisData;
	private double gpsActiveEnergy;
	private double gpsStandbyEnergy;
	private double totalGpsEnergy;
	private double totalCameraEnergy;
	private double bluetoothActiveEnergy;
	private double bluetoothStandbyEnergy;
	private double totalBluetoothEnergy;
	private double totalScreenEnergy;

	/**
	 * Initializes an instance of the EnergyModel class using the specified analysis data.
	 * 
	 * @param analysisData The analysis data.
	 */
	public EnergyModel(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;

		Profile profile = analysisData.getProfile();
		// GPS Energy
		Iterator<GpsInfo> gpsIter = analysisData.getGpsInfos().iterator();
		if (gpsIter.hasNext()) {

			while (gpsIter.hasNext()) {
				GpsInfo gps = gpsIter.next();
				GpsState gpsState = gps.getGpsState();
				switch (gpsState) {
				case GPS_ACTIVE:
					gpsActiveEnergy += profile.getPowerGpsActive() * (gps.getEndTimeStamp() - gps.getBeginTimeStamp());
					break;
				case GPS_STANDBY:
					gpsStandbyEnergy += profile.getPowerGpsStandby()
							* (gps.getEndTimeStamp() - gps.getBeginTimeStamp());
					break;
				}
		}
		this.totalGpsEnergy = gpsActiveEnergy + gpsStandbyEnergy;

		// Camera Energy
		Iterator<CameraInfo> cameraIter = analysisData.getCameraInfos().iterator();
		if (cameraIter.hasNext()) {

			while (cameraIter.hasNext()) {
				CameraInfo camera = cameraIter.next();
				CameraInfo.CameraState cameraState = camera.getCameraState();
				
				if (cameraState == CameraInfo.CameraState.CAMERA_ON) {
					totalCameraEnergy += profile.getPowerCameraOn()
							* (camera.getEndTimeStamp() - camera.getBeginTimeStamp());
				}

			}
		}

		// Bluetooth Energy
		Iterator<BluetoothInfo> bluetoothIter = analysisData.getBluetoothInfos().iterator();
		if (bluetoothIter.hasNext()) {
			while (bluetoothIter.hasNext()) {
				BluetoothInfo btInfo = bluetoothIter.next();
				switch (btInfo.getBluetoothState()) {
				case BLUETOOTH_CONNECTED:
					bluetoothActiveEnergy += profile.getPowerBluetoothActive()
							* (btInfo.getEndTimeStamp() - btInfo.getBeginTimeStamp());
					break;
				case BLUETOOTH_DISCONNECTED:
					bluetoothStandbyEnergy += profile.getPowerBluetoothStandby()
							* (btInfo.getEndTimeStamp() - btInfo.getBeginTimeStamp());
					break;
				}
			}
		}
		this.totalBluetoothEnergy = bluetoothActiveEnergy + bluetoothStandbyEnergy;

		// Screen Energy
		Iterator<ScreenStateInfo> screenIter = analysisData.getScreenStateInfos().iterator();
		if (screenIter.hasNext()) {
			while (screenIter.hasNext()) {
				ScreenStateInfo screenInfo = screenIter.next();
				if (screenInfo.getScreenState() == ScreenStateInfo.ScreenState.SCREEN_ON) {
					totalScreenEnergy += profile.getPowerScreenOn()
							* (screenInfo.getEndTimeStamp() - screenInfo.getBeginTimeStamp());
				}
			}
		}}

	}

	/**
	 * Returns the amount of energy consumed during the  GPS active state. 
	 * 
	 * @return The amount of GPS active energy in joules.
	 */
	public double getGpsActiveEnergy() {
		return gpsActiveEnergy;
	}

	/**
	 * Returns the amount of energy consumed during GPS standby mode. 
	 * 
	 * @return The amount of GPS standby energy in joules.
	 */
	public double getGpsStandbyEnergy() {
		return gpsStandbyEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by GPS. This value is the sum of the 
	 * energy consumption amounts for GPS Active and GPS Standby.
	 * 
	 * @return The total amount of GPS energy consumed, in joules.
	 */
	public double getTotalGpsEnergy() {
		return totalGpsEnergy;
	}

	/**
	 * Returns the total amount of energy consumed while the Camera state is ON. 
	 * 
	 * @return The total amount of Camera energy in joules.
	 */
	public double getTotalCameraEnergy() {
		return totalCameraEnergy;
	}

	/**
	 * Returns the amount of energy consumed while Bluetooth is in the connected state.
	 * 
	 * @return The amount of Bluetooth active energy, in joules.
	 */
	public double getBluetoothActiveEnergy() {
		return bluetoothActiveEnergy;
	}

	/**
	 * Returns the amount of energy consumed while Bluetooth is in the disconnected state.
	 * 
	 * @return The amount of Bluetooth standby energy, in joules.
	 */
	public double getBluetoothStandbyEnergy() {
		return bluetoothStandbyEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by Bluetooth. This value is the sum of 
	 * the energy consumption values for Bluetooth Active and Bluetooth Standby.
	 * 
	 * @return The total amount of Bluetooth energy consumed, in joules.
	 */
	public double getTotalBluetoothEnergy() {
		return totalBluetoothEnergy;
	}

	/**
	 * Returns the amount of energy consumed while the device screen is in the ON state.
	 * 
	 * @return The total screen energy consumed, in joules.
	 */
	public double getTotalScreenEnergy() {
		return totalScreenEnergy;
	}

	/**
	 * Returns the total amount of energy consumed. 
	 * 
	 * @return The total energy consumed in joules.
	 */
	public double getTotalEnergyConsumed() {

		double totalEnergy = getTotalBluetoothEnergy() + getTotalCameraEnergy()
				+ getTotalGpsEnergy()
				+ analysisData.getRrcStateMachine().getTotalRRCEnergy()
				+ getTotalScreenEnergy();
		return totalEnergy;
	}

}
