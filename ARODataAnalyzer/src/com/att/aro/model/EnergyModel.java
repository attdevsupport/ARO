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

/**
 * Contains methods for generating energy statistics from race analysis data. 
 */
public class EnergyModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private TraceData.Analysis analysisData;
	private double gpsActiveEnergy;
	private double gpsStandbyEnergy;
	private double totalGpsEnergy;
	private double totalCameraEnergy;
	private double wifiActiveEnergy;
	private double wifiStandbyEnergy;
	private double totalWifiEnergy;
	private double bluetoothActiveEnergy;
	private double bluetoothStandbyEnergy;
	private double totalBluetoothEnergy;
	private double totalScreenEnergy;

	/**
	 * Initializes an instance of the EnergyModel class using the specified analysis data.
	 * 
	 * @param analysisData – The analysis data.
	 */
	public EnergyModel(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;

		Profile profile = analysisData.getProfile();
		TraceData td = analysisData.getTraceData();
		double endTime = td.getTraceDuration();

		// GPS Energy
		Iterator<GpsInfo> gpsIter = td.getGpsInfos().iterator();
		if (gpsIter.hasNext()) {
			GpsInfo gps = gpsIter.next();
			GpsInfo.GpsState gpsState = gps.getGpsState();
			double time = gps.getGpsTimeStamp();
			while (gpsIter.hasNext()) {
				gps = gpsIter.next();
				switch (gpsState) {
				case GPS_ACTIVE:
					gpsActiveEnergy += profile.getPowerGpsActive() * (gps.getGpsTimeStamp() - time);
					break;
				case GPS_STANDBY:
					gpsStandbyEnergy += profile.getPowerGpsStandby()
							* (gps.getGpsTimeStamp() - time);
					break;
				}
				gpsState = gps.getGpsState();
				time = gps.getGpsTimeStamp();
			}
			switch (gpsState) {
			case GPS_ACTIVE:
				gpsActiveEnergy += profile.getPowerGpsActive() * (endTime - gps.getGpsTimeStamp());
				break;
			case GPS_STANDBY:
				gpsStandbyEnergy += profile.getPowerGpsStandby()
						* (endTime - gps.getGpsTimeStamp());
				break;
			}
		}
		this.totalGpsEnergy = gpsActiveEnergy + gpsStandbyEnergy;

		// Camera Energy
		Iterator<CameraInfo> cameraIter = td.getCameraInfos().iterator();
		if (cameraIter.hasNext()) {
			CameraInfo camera = cameraIter.next();
			CameraInfo.CameraState cameraState = camera.getCameraState();
			double time = camera.getCameraTimeStamp();
			while (cameraIter.hasNext()) {
				camera = cameraIter.next();
				if (cameraState == CameraInfo.CameraState.CAMERA_ON) {
					totalCameraEnergy += profile.getPowerCameraOn()
							* (camera.getCameraTimeStamp() - time);
				}
				cameraState = camera.getCameraState();
				time = camera.getCameraTimeStamp();
			}
			if (cameraState == CameraInfo.CameraState.CAMERA_ON) {
				totalCameraEnergy += profile.getPowerCameraOn()
						* (endTime - camera.getCameraTimeStamp());
			}
		}

		// WiFi Energy
		Iterator<WifiInfo> wifiIter = td.getWifiInfos().iterator();
		if (wifiIter.hasNext()) {
			WifiInfo wifi = wifiIter.next();
			WifiInfo.WifiState wifiState = wifi.getWifiState();
			double time = wifi.getWifiTimeStamp();
			while (wifiIter.hasNext()) {
				wifi = wifiIter.next();
				switch (wifiState) {
				case WIFI_CONNECTED:
				case WIFI_CONNECTING:
				case WIFI_DISCONNECTING:
					wifiActiveEnergy += profile.getPowerWifiActive()
							* (wifi.getWifiTimeStamp() - time);
					break;
				case WIFI_DISCONNECTED:
				case WIFI_SUSPENDED:
					wifiStandbyEnergy += profile.getPowerWifiStandby()
							* (wifi.getWifiTimeStamp() - time);
					break;
				}
				wifiState = wifi.getWifiState();
				time = wifi.getWifiTimeStamp();
			}
			switch (wifiState) {
			case WIFI_CONNECTED:
			case WIFI_CONNECTING:
			case WIFI_DISCONNECTING:
				wifiActiveEnergy += profile.getPowerWifiActive()
						* (endTime - wifi.getWifiTimeStamp());
				break;
			case WIFI_DISCONNECTED:
			case WIFI_SUSPENDED:
				wifiStandbyEnergy += profile.getPowerWifiStandby()
						* (endTime - wifi.getWifiTimeStamp());
				break;
			}
		}
		this.totalWifiEnergy = wifiActiveEnergy + wifiStandbyEnergy;

		// Bluetooth Energy
		Iterator<BluetoothInfo> bluetoothIter = td.getBluetoothInfos().iterator();
		if (bluetoothIter.hasNext()) {
			BluetoothInfo bt = bluetoothIter.next();
			BluetoothInfo.BluetoothState btState = bt.getBluetoothState();
			double time = bt.getBluetoothTimeStamp();
			while (bluetoothIter.hasNext()) {
				bt = bluetoothIter.next();
				switch (btState) {
				case BLUETOOTH_CONNECTED:
					bluetoothActiveEnergy += profile.getPowerBluetoothActive()
							* (bt.getBluetoothTimeStamp() - time);
					break;
				case BLUETOOTH_DISCONNECTED:
					bluetoothStandbyEnergy += profile.getPowerBluetoothStandby()
							* (bt.getBluetoothTimeStamp() - time);
					break;
				}
				btState = bt.getBluetoothState();
				time = bt.getBluetoothTimeStamp();
			}
			switch (btState) {
			case BLUETOOTH_CONNECTED:
				bluetoothActiveEnergy += profile.getPowerBluetoothActive()
						* (endTime - bt.getBluetoothTimeStamp());
				break;
			case BLUETOOTH_DISCONNECTED:
				bluetoothStandbyEnergy += profile.getPowerBluetoothStandby()
						* (endTime - bt.getBluetoothTimeStamp());
				break;
			}
		}
		this.totalBluetoothEnergy = bluetoothActiveEnergy + bluetoothStandbyEnergy;

		// Screen Energy
		Iterator<ScreenStateInfo> screenIter = td.getScreenStateInfos().iterator();
		if (screenIter.hasNext()) {
			ScreenStateInfo screen = screenIter.next();
			ScreenStateInfo.ScreenState screenState = screen.getScreenState();
			double time = screen.getScreenTimeStamp();
			while (screenIter.hasNext()) {
				screen = screenIter.next();
				if (screenState == ScreenStateInfo.ScreenState.SCREEN_ON) {
					totalScreenEnergy += profile.getPowerScreenOn()
							* (screen.getScreenTimeStamp() - time);
				}
				screenState = screen.getScreenState();
				time = screen.getScreenTimeStamp();
			}
			if (screenState == ScreenStateInfo.ScreenState.SCREEN_ON) {
				totalScreenEnergy += profile.getPowerScreenOn()
						* (endTime - screen.getScreenTimeStamp());
			}
		}

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
	 * Returns the total amount of energy consumed while the camera state is ON. 
	 * 
	 * @return The total amount of Camera energy in joules.
	 */
	public double getTotalCameraEnergy() {
		return totalCameraEnergy;
	}

	/**
	 * Returns the amount of energy consumed while WiFi is active. 
	 * 
	 * @return The WiFi active energy consumption, in joules.
	 */
	public double getWifiActiveEnergy() {
		return wifiActiveEnergy;
	}

	/**
	 * Returns the amount of energy consumed while WiFi is in standby. 
	 * 
	 * @return The WiFi standby energy consumption, in joules.
	 */
	public double getWifiStandbyEnergy() {
		return wifiStandbyEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by WiFi. This value is the sum of the 
	 * energy consumption values for Wi-Fi Connected and Wi-Fi Inactive.
	 * 
	 * @return The total WiFi energy consumed, in joules.
	 */
	public double getTotalWifiEnergy() {
		return totalWifiEnergy;
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
				+ getTotalScreenEnergy() + getTotalWifiEnergy();
		return totalEnergy;
	}

}
