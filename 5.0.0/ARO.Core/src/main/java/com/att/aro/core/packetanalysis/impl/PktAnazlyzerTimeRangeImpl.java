/**
 * Copyright 2016 AT&T
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

import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.packetanalysis.IPktAnazlyzerTimeRangeUtil;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo.BluetoothState;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo.CameraState;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo.GpsState;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.WifiInfo;

/**
 * This is for Directory trace result time range setting in view tool. In 4.1.1
 * this is in Analysis() in TraceData.java class, and the method name is misleading but keep the name
 * for future maintenance
 */
public class PktAnazlyzerTimeRangeImpl implements IPktAnazlyzerTimeRangeUtil {

	public PktAnazlyzerTimeRangeImpl() {
	}

	@Override
	public AbstractTraceResult getTimeRangeResult(TraceDirectoryResult result,
			TimeRange timeRange) {
		TraceDirectoryResult resultForTimeRange = result;
		getUserEventsForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getScreenInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getBluetoothInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getGpsInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getRadioInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getBatteryInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getWifiInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getNetworkInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());
		getCameraInfosForTheTimeRange(resultForTimeRange,
				timeRange.getBeginTime(), timeRange.getEndTime());

		return resultForTimeRange;
	}

	private void getUserEventsForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<UserEvent> orifilteredUserEvents = result.getUserEvents();
		List<UserEvent> filteredUserEvents = new ArrayList<UserEvent>();
		for (UserEvent userEvent : orifilteredUserEvents) {
			if (userEvent.getPressTime() >= beginTime
					&& userEvent.getReleaseTime() <= endTime) {

				filteredUserEvents.add(userEvent);
			}
		}
		result.setUserEvents(filteredUserEvents);
	}

	private void getScreenInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<ScreenStateInfo> orifilteredScreenStateInfos = result
				.getScreenStateInfos();
		List<ScreenStateInfo> filteredScreenStateInfos = new ArrayList<ScreenStateInfo>();
		for (ScreenStateInfo screenStateInfo : orifilteredScreenStateInfos) {

			if (screenStateInfo.getBeginTimeStamp() >= beginTime
					&& screenStateInfo.getEndTimeStamp() <= endTime) {
				filteredScreenStateInfos.add(screenStateInfo);
			} else if (screenStateInfo.getBeginTimeStamp() <= beginTime
					&& screenStateInfo.getEndTimeStamp() <= endTime
					&& screenStateInfo.getEndTimeStamp() > beginTime) {
				filteredScreenStateInfos.add(new ScreenStateInfo(beginTime,
						screenStateInfo.getEndTimeStamp(), screenStateInfo
								.getScreenState(), screenStateInfo
								.getScreenBrightness(), screenStateInfo
								.getScreenTimeout()));
			} else if (screenStateInfo.getBeginTimeStamp() <= beginTime
					&& screenStateInfo.getEndTimeStamp() >= endTime) {
				filteredScreenStateInfos.add(new ScreenStateInfo(beginTime,
						endTime, screenStateInfo.getScreenState(),
						screenStateInfo.getScreenBrightness(), screenStateInfo
								.getScreenTimeout()));
			} else if (screenStateInfo.getBeginTimeStamp() >= beginTime
					&& screenStateInfo.getBeginTimeStamp() < endTime
					&& screenStateInfo.getEndTimeStamp() >= endTime) {
				filteredScreenStateInfos.add(new ScreenStateInfo(
						screenStateInfo.getBeginTimeStamp(), endTime,
						screenStateInfo.getScreenState(), screenStateInfo
								.getScreenBrightness(), screenStateInfo
								.getScreenTimeout()));
			}
		}
		result.setScreenStateInfos(filteredScreenStateInfos);
		// return filteredScreenStateInfos;
	}

	private void getRadioInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<RadioInfo> orifilteredRadioInfos = result.getRadioInfos();

		List<RadioInfo> filteredRadioInfos = new ArrayList<RadioInfo>();
		for (RadioInfo radioInfo : orifilteredRadioInfos) {

			if (radioInfo.getTimeStamp() >= beginTime
					&& radioInfo.getTimeStamp() <= endTime) {
				filteredRadioInfos.add(radioInfo);
			}
		}
		result.setRadioInfos(filteredRadioInfos);
		// return filteredRadioInfos;
	}

	private void getBatteryInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<BatteryInfo> orifilteredBatteryInfos = result.getBatteryInfos();

		List<BatteryInfo> filteredBatteryInfos = new ArrayList<BatteryInfo>();
		for (BatteryInfo batteryInfo : orifilteredBatteryInfos) {

			if (batteryInfo.getBatteryTimeStamp() >= beginTime
					&& batteryInfo.getBatteryTimeStamp() <= endTime) {
				filteredBatteryInfos.add(batteryInfo);
			}
		}
		result.setBatteryInfos(filteredBatteryInfos);
		// return filteredBatteryInfos;
	}

	private void getWifiInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<WifiInfo> orifilteredWifiInfos = result.getWifiInfos();
		List<WifiInfo> filteredWifiInfos = new ArrayList<WifiInfo>();
		for (WifiInfo wifiInfo : orifilteredWifiInfos) {

			if (wifiInfo.getBeginTimeStamp() >= beginTime
					&& wifiInfo.getEndTimeStamp() <= endTime) {
				filteredWifiInfos.add(wifiInfo);
			} else if (wifiInfo.getBeginTimeStamp() <= beginTime
					&& wifiInfo.getEndTimeStamp() <= endTime
					&& wifiInfo.getEndTimeStamp() > beginTime) {
				filteredWifiInfos.add(new WifiInfo(beginTime, wifiInfo
						.getEndTimeStamp(), wifiInfo.getWifiState(), wifiInfo
						.getWifiMacAddress(), wifiInfo.getWifiRSSI(), wifiInfo
						.getWifiSSID()));
			} else if (wifiInfo.getBeginTimeStamp() <= beginTime
					&& wifiInfo.getEndTimeStamp() >= endTime) {
				filteredWifiInfos.add(new WifiInfo(beginTime, endTime, wifiInfo
						.getWifiState(), wifiInfo.getWifiMacAddress(), wifiInfo
						.getWifiRSSI(), wifiInfo.getWifiSSID()));
			} else if (wifiInfo.getBeginTimeStamp() >= beginTime
					&& wifiInfo.getBeginTimeStamp() < endTime
					&& wifiInfo.getEndTimeStamp() >= endTime) {
				filteredWifiInfos.add(new WifiInfo(
						wifiInfo.getBeginTimeStamp(), endTime, wifiInfo
								.getWifiState(), wifiInfo.getWifiMacAddress(),
						wifiInfo.getWifiRSSI(), wifiInfo.getWifiSSID()));
			}
		}
		result.setWifiInfos(filteredWifiInfos);
		// return filteredWifiInfos;
	}

	private void getNetworkInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<NetworkBearerTypeInfo> orifilteredBearerInfos = result
				.getNetworkTypeInfos();

		List<NetworkBearerTypeInfo> filteredBearerInfos = new ArrayList<NetworkBearerTypeInfo>();
		for (NetworkBearerTypeInfo bearerInfo : orifilteredBearerInfos) {

			if (bearerInfo.getBeginTimestamp() >= beginTime
					&& bearerInfo.getEndTimestamp() <= endTime) {
				filteredBearerInfos.add(bearerInfo);
			} else if (bearerInfo.getBeginTimestamp() <= beginTime
					&& bearerInfo.getEndTimestamp() <= endTime
					&& bearerInfo.getEndTimestamp() > beginTime) {
				filteredBearerInfos.add(new NetworkBearerTypeInfo(beginTime,
						bearerInfo.getEndTimestamp(), bearerInfo
								.getNetworkType()));
			} else if (bearerInfo.getBeginTimestamp() <= beginTime
					&& bearerInfo.getEndTimestamp() >= endTime) {
				filteredBearerInfos.add(new NetworkBearerTypeInfo(beginTime,
						endTime, bearerInfo.getNetworkType()));
			} else if (bearerInfo.getBeginTimestamp() >= beginTime
					&& bearerInfo.getBeginTimestamp() < endTime
					&& bearerInfo.getEndTimestamp() >= endTime) {
				filteredBearerInfos.add(new NetworkBearerTypeInfo(bearerInfo
						.getBeginTimestamp(), endTime, bearerInfo
						.getNetworkType()));
			}
		}
		result.setNetworkTypeInfos(filteredBearerInfos);
		// return filteredBearerInfos;
	}

	private void getCameraInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {

		List<CameraInfo> cameraInfos = result.getCameraInfos();
		List<CameraInfo> filteredCameraInfos = new ArrayList<CameraInfo>();
		CameraInfo filteredCameraInfo = null;
		double tempCameraActiveDuration = 0.0;
		for (CameraInfo cameraInfo : cameraInfos) {

			if (cameraInfo.getBeginTimeStamp() >= beginTime
					&& cameraInfo.getEndTimeStamp() <= endTime) {
				filteredCameraInfo = cameraInfo;
				filteredCameraInfos.add(filteredCameraInfo);
				if (filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON) {
					tempCameraActiveDuration += filteredCameraInfo
							.getEndTimeStamp()
							- filteredCameraInfo.getBeginTimeStamp();
				}
			} else if (cameraInfo.getBeginTimeStamp() <= beginTime
					&& cameraInfo.getEndTimeStamp() <= endTime
					&& cameraInfo.getEndTimeStamp() > beginTime) {
				filteredCameraInfo = new CameraInfo(beginTime,
						cameraInfo.getEndTimeStamp(),
						cameraInfo.getCameraState());
				filteredCameraInfos.add(filteredCameraInfo);
				if (filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON) {
					tempCameraActiveDuration += filteredCameraInfo
							.getEndTimeStamp()
							- filteredCameraInfo.getBeginTimeStamp();
				}
			} else if (cameraInfo.getBeginTimeStamp() <= beginTime
					&& cameraInfo.getEndTimeStamp() >= endTime) {
				filteredCameraInfo = new CameraInfo(beginTime, endTime,
						cameraInfo.getCameraState());
				filteredCameraInfos.add(filteredCameraInfo);
				if (filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON) {
					tempCameraActiveDuration += filteredCameraInfo
							.getEndTimeStamp()
							- filteredCameraInfo.getBeginTimeStamp();
				}
			} else if (cameraInfo.getBeginTimeStamp() >= beginTime
					&& cameraInfo.getBeginTimeStamp() < endTime
					&& cameraInfo.getEndTimeStamp() >= endTime) {
				filteredCameraInfo = new CameraInfo(
						cameraInfo.getBeginTimeStamp(), endTime,
						cameraInfo.getCameraState());
				filteredCameraInfos.add(filteredCameraInfo);
				if (filteredCameraInfo.getCameraState() == CameraState.CAMERA_ON) {
					tempCameraActiveDuration += filteredCameraInfo
							.getEndTimeStamp()
							- filteredCameraInfo.getBeginTimeStamp();
				}
			}
		}
		result.setCameraActiveDuration(tempCameraActiveDuration);
		result.setCameraActiveDuration(tempCameraActiveDuration);
		// return filteredCameraInfos;
	}

	/**
	 * Returns the list of bluetooth events filtered based on the time range.
	 */
	private void getBluetoothInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<BluetoothInfo> orifilteredBluetoothInfos = result
				.getBluetoothInfos();

		List<BluetoothInfo> filteredBluetoothInfos = new ArrayList<BluetoothInfo>();

		BluetoothInfo filteredBluetoothInfo = null;
		double bluetoothActiveDuration = 0.0;
		for (BluetoothInfo bluetoothInfo : orifilteredBluetoothInfos) {

			if (bluetoothInfo.getBeginTimeStamp() >= beginTime
					&& bluetoothInfo.getEndTimeStamp() <= endTime) {
				filteredBluetoothInfo = bluetoothInfo;
				filteredBluetoothInfos.add(filteredBluetoothInfo);
				if (filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED) {
					bluetoothActiveDuration += filteredBluetoothInfo
							.getEndTimeStamp()
							- filteredBluetoothInfo.getBeginTimeStamp();
				}
			} else if (bluetoothInfo.getBeginTimeStamp() <= beginTime
					&& bluetoothInfo.getEndTimeStamp() <= endTime
					&& bluetoothInfo.getEndTimeStamp() > beginTime) {
				filteredBluetoothInfo = new BluetoothInfo(beginTime,
						bluetoothInfo.getEndTimeStamp(),
						bluetoothInfo.getBluetoothState());
				filteredBluetoothInfos.add(filteredBluetoothInfo);
				if (filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED) {
					bluetoothActiveDuration += filteredBluetoothInfo
							.getEndTimeStamp()
							- filteredBluetoothInfo.getBeginTimeStamp();
				}
			} else if (bluetoothInfo.getBeginTimeStamp() <= beginTime
					&& bluetoothInfo.getEndTimeStamp() >= endTime) {
				filteredBluetoothInfo = new BluetoothInfo(beginTime, endTime,
						bluetoothInfo.getBluetoothState());
				filteredBluetoothInfos.add(filteredBluetoothInfo);
				if (filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED) {
					bluetoothActiveDuration += filteredBluetoothInfo
							.getEndTimeStamp()
							- filteredBluetoothInfo.getBeginTimeStamp();
				}
			} else if (bluetoothInfo.getBeginTimeStamp() >= beginTime
					&& bluetoothInfo.getBeginTimeStamp() < endTime
					&& bluetoothInfo.getEndTimeStamp() >= endTime) {
				filteredBluetoothInfo = new BluetoothInfo(
						bluetoothInfo.getBeginTimeStamp(), endTime,
						bluetoothInfo.getBluetoothState());
				filteredBluetoothInfos.add(filteredBluetoothInfo);
				if (filteredBluetoothInfo.getBluetoothState() == BluetoothState.BLUETOOTH_CONNECTED) {
					bluetoothActiveDuration += filteredBluetoothInfo
							.getEndTimeStamp()
							- filteredBluetoothInfo.getBeginTimeStamp();
				}
			}
		}
		result.setBluetoothInfos(filteredBluetoothInfos);
		result.setBluetoothActiveDuration(bluetoothActiveDuration);
		// return filteredBluetoothInfos;
	}

	/**
	 * Returns the list of gps events filtered based on the time range.
	 */
	private void getGpsInfosForTheTimeRange(TraceDirectoryResult result,
			double beginTime, double endTime) {
		List<GpsInfo> orifilteredGpsInfos = result.getGpsInfos();

		List<GpsInfo> filteredGpsInfos = new ArrayList<GpsInfo>();

		GpsInfo filteredGpsInfo = null;
		double gpsActiveDuration = 0.0;
		for (GpsInfo gpsInfo : orifilteredGpsInfos) {

			if (gpsInfo.getBeginTimeStamp() >= beginTime
					&& gpsInfo.getEndTimeStamp() <= endTime) {
				filteredGpsInfo = gpsInfo;
				filteredGpsInfos.add(filteredGpsInfo);
				if (filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE) {
					gpsActiveDuration += filteredGpsInfo.getEndTimeStamp()
							- filteredGpsInfo.getBeginTimeStamp();
				}

			} else if (gpsInfo.getBeginTimeStamp() <= beginTime
					&& gpsInfo.getEndTimeStamp() <= endTime
					&& gpsInfo.getEndTimeStamp() > beginTime) {
				filteredGpsInfo = new GpsInfo(beginTime,
						gpsInfo.getEndTimeStamp(), gpsInfo.getGpsState());
				filteredGpsInfos.add(filteredGpsInfo);
				if (filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE) {
					gpsActiveDuration += filteredGpsInfo.getEndTimeStamp()
							- filteredGpsInfo.getBeginTimeStamp();
				}
			} else if (gpsInfo.getBeginTimeStamp() <= beginTime
					&& gpsInfo.getEndTimeStamp() >= endTime) {
				filteredGpsInfo = new GpsInfo(beginTime, endTime,
						gpsInfo.getGpsState());
				filteredGpsInfos.add(filteredGpsInfo);
				if (filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE) {
					gpsActiveDuration += filteredGpsInfo.getEndTimeStamp()
							- filteredGpsInfo.getBeginTimeStamp();
				}
			} else if (gpsInfo.getBeginTimeStamp() >= beginTime
					&& gpsInfo.getBeginTimeStamp() < endTime
					&& gpsInfo.getEndTimeStamp() >= endTime) {
				filteredGpsInfo = new GpsInfo(gpsInfo.getBeginTimeStamp(),
						endTime, gpsInfo.getGpsState());
				filteredGpsInfos.add(filteredGpsInfo);
				if (filteredGpsInfo.getGpsState() == GpsState.GPS_ACTIVE) {
					gpsActiveDuration += filteredGpsInfo.getEndTimeStamp()
							- filteredGpsInfo.getBeginTimeStamp();
				}

			}
		}
		result.setGpsInfos(filteredGpsInfos);
		result.setGpsActiveDuration(gpsActiveDuration);
		// return filteredGpsInfos;
	}

}
