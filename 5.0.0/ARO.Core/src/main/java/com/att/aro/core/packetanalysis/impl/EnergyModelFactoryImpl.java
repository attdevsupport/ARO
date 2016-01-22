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
package com.att.aro.core.packetanalysis.impl;

import java.util.Iterator;
import java.util.List;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.IEnergyModelFactory;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo.GpsState;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;

/**
 * implementing method to generate EnergyModel
 */
public class EnergyModelFactoryImpl implements IEnergyModelFactory {

	@Override
	public EnergyModel create(Profile profile, double totalRrcEnergy,
			List<GpsInfo> gpsinfos, List<CameraInfo> camerainfos,
			List<BluetoothInfo> bluetoothinfos,
			List<ScreenStateInfo> screenstateinfos) {
		double gpsActiveEnergy = 0, gpsStandbyEnergy = 0, totalGpsEnergy = 0, totalCameraEnergy = 0;
		double bluetoothActiveEnergy = 0, bluetoothStandbyEnergy = 0, totalBluetoothEnergy =0;
		double totalScreenEnergy = 0;
		EnergyModel model = new EnergyModel();
		model.setTotalRrcEnergy(totalRrcEnergy);
		
		// GPS Energy
		Iterator<GpsInfo> gpsIter = gpsinfos.iterator();
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
				default:
					break;
				}
			}
		}
		totalGpsEnergy = gpsActiveEnergy + gpsStandbyEnergy;

		// Camera Energy
		Iterator<CameraInfo> cameraIter = camerainfos.iterator();
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
		Iterator<BluetoothInfo> bluetoothIter = bluetoothinfos.iterator();
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
				default:
					break;
				}
				
			}
		}
		totalBluetoothEnergy = bluetoothActiveEnergy + bluetoothStandbyEnergy;

		// Screen Energy
		Iterator<ScreenStateInfo> screenIter = screenstateinfos.iterator();
		if (screenIter.hasNext()) {
			while (screenIter.hasNext()) {
				ScreenStateInfo screenInfo = screenIter.next();
				if (screenInfo.getScreenState() == ScreenStateInfo.ScreenState.SCREEN_ON) {
					totalScreenEnergy += profile.getPowerScreenOn()
							* (screenInfo.getEndTimeStamp() - screenInfo.getBeginTimeStamp());
				}
			}
		}
		model.setBluetoothActiveEnergy(bluetoothActiveEnergy);
		model.setBluetoothStandbyEnergy(bluetoothStandbyEnergy);
		model.setGpsActiveEnergy(gpsActiveEnergy);
		model.setGpsStandbyEnergy(gpsStandbyEnergy);
		model.setTotalBluetoothEnergy(totalBluetoothEnergy);
		model.setTotalCameraEnergy(totalCameraEnergy);
		model.setTotalGpsEnergy(totalGpsEnergy);
		model.setTotalRrcEnergy(totalRrcEnergy);
		model.setTotalScreenEnergy(totalScreenEnergy);
		
		return model;
	}

}
