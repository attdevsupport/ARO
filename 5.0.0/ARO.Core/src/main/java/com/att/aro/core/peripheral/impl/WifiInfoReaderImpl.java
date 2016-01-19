/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IWifiInfoReader;
import com.att.aro.core.peripheral.pojo.WifiInfo;
import com.att.aro.core.peripheral.pojo.WifiInfo.WifiState;
import com.att.aro.core.util.Util;

/**
 * Method to read the WIFI data from the trace file and store it in the
 * wifiInfos list. It also updates the active duration for Wifi.
 * 
 * @author EDS team Refactored by Borey Sao Date: September 30, 2014
 */
public class WifiInfoReaderImpl extends PeripheralBase implements IWifiInfoReader {

	@InjectLogger
	private static ILogger logger;
	
	private double wifiActiveDuration = 0.0;
	private Pattern wifiPattern = Pattern.compile("\\S*\\s*\\S*\\s*(\\S*)\\s*(\\S*)\\s*(.*)");

	@Override
	public List<WifiInfo> readData(String directory, double startTime, double traceDuration) {
		this.wifiActiveDuration = 0;
		List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.WIFI_FILE;

		if (!filereader.fileExist(filepath)) {
			return wifiInfos;
		}
		double dLastTimeStamp = 0.0;
		double dActiveDuration = 0.0;
		double beginTime = 0.0;
		double endTime = 0.0;
		String prevMacAddress = null;
		String prevRssi = null;
		String prevSsid = null;
		WifiState prevWifiState = null;
		WifiState lastWifiState = null;
		String firstLine;
		String strLineBuf;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to read Wifi info file: " + filepath);
		}
		if (lines != null && lines.length > 0) {
			firstLine = lines[0];
			try {
				String strFieldsFirstLine[] = firstLine.split(" ");
				if (strFieldsFirstLine.length >= 2) {
					beginTime = Util.normalizeTime(Double.parseDouble(strFieldsFirstLine[0]), startTime);
					if (TraceDataConst.WIFI_OFF.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_DISABLED;
					} else if (TraceDataConst.WIFI_CONNECTED.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_CONNECTED;
						Matcher matcher = wifiPattern.matcher(firstLine);
						if (matcher.lookingAt()) {
							prevMacAddress = matcher.group(1);
							prevRssi = matcher.group(2);
							prevSsid = matcher.group(3);
						} else {
							logger.warn("Unable to parse wifi connection params: " + firstLine);
						}
					} else if (TraceDataConst.WIFI_DISCONNECTED.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_DISCONNECTED;
					} else if (TraceDataConst.WIFI_CONNECTING.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_CONNECTING;
					} else if (TraceDataConst.WIFI_DISCONNECTING.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_DISCONNECTING;
					} else if (TraceDataConst.WIFI_SUSPENDED.equals(strFieldsFirstLine[1])) {
						prevWifiState = WifiState.WIFI_SUSPENDED;
					} else {
						logger.warn("Unknown wifi state: " + firstLine);
						prevWifiState = WifiState.WIFI_UNKNOWN;
					}

					if (!prevWifiState.equals(lastWifiState)) {
						if (lastWifiState == WifiState.WIFI_CONNECTED || lastWifiState == WifiState.WIFI_CONNECTING || lastWifiState == WifiState.WIFI_DISCONNECTING) {
							dActiveDuration += (beginTime - dLastTimeStamp);
						}
						lastWifiState = prevWifiState;
						dLastTimeStamp = beginTime;
					}
				} else {
					logger.warn("Invalid WiFi trace entry: " + firstLine);
				}

			} catch (Exception e) {
				logger.warn("Unexpected error parsing GPS event: " + firstLine, e);
			}

			for (int i = 1; i < lines.length; i++) {
				strLineBuf = lines[i];
				String strFields[] = strLineBuf.split(" ");
				try {
					if (strFields.length >= 2) {
						String macAddress = null;
						String rssi = null;
						String ssid = null;
						WifiState wifiState = null;
						endTime = Util.normalizeTime(Double.parseDouble(strFields[0]), startTime);
						if (TraceDataConst.WIFI_OFF.equals(strFields[1])) {
							wifiState = WifiState.WIFI_DISABLED;
						} else if (TraceDataConst.WIFI_CONNECTED.equals(strFields[1])) {
							wifiState = WifiState.WIFI_CONNECTED;
							Matcher matcher = wifiPattern.matcher(strLineBuf);
							if (matcher.lookingAt()) {
								macAddress = matcher.group(1);
								rssi = matcher.group(2);
								ssid = matcher.group(3);
							} else {
								logger.warn("Unable to parse wifi connection params: " + strLineBuf);
							}
						} else if (TraceDataConst.WIFI_DISCONNECTED.equals(strFields[1])) {
							wifiState = WifiState.WIFI_DISCONNECTED;
						} else if (TraceDataConst.WIFI_CONNECTING.equals(strFields[1])) {
							wifiState = WifiState.WIFI_CONNECTING;
						} else if (TraceDataConst.WIFI_DISCONNECTING.equals(strFields[1])) {
							wifiState = WifiState.WIFI_DISCONNECTING;
						} else if (TraceDataConst.WIFI_SUSPENDED.equals(strFields[1])) {
							wifiState = WifiState.WIFI_SUSPENDED;
						} else {
							logger.warn("Unknown wifi state: " + strLineBuf);
							wifiState = WifiState.WIFI_UNKNOWN;
						}

						if (!wifiState.equals(lastWifiState)) {
							wifiInfos.add(new WifiInfo(beginTime, endTime, prevWifiState, prevMacAddress, prevRssi, prevSsid));
							if (lastWifiState == WifiState.WIFI_CONNECTED
									|| lastWifiState == WifiState.WIFI_CONNECTING
									|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
								dActiveDuration += (endTime - dLastTimeStamp);
							}
							lastWifiState = wifiState;
							dLastTimeStamp = endTime;
							beginTime = endTime;
							prevWifiState = wifiState;
							prevMacAddress = macAddress;
							prevRssi = rssi;
							prevSsid = ssid;
						}
					} else {
						logger.warn("Invalid WiFi trace entry: " + strLineBuf);
					}
				} catch (Exception e) {
					logger.warn("Unexpected error parsing GPS event: " + strLineBuf, e);
				}

			}
			wifiInfos.add(new WifiInfo(beginTime, traceDuration, prevWifiState, prevMacAddress, prevRssi, prevSsid));

			// Duration calculation should probably be done in analysis
			if (lastWifiState == WifiState.WIFI_CONNECTED 
					|| lastWifiState == WifiState.WIFI_CONNECTING 
					|| lastWifiState == WifiState.WIFI_DISCONNECTING) {
				dActiveDuration += Math.max(0, traceDuration - dLastTimeStamp);
			}

			this.wifiActiveDuration = dActiveDuration;
			Collections.sort(wifiInfos);
		}
		return wifiInfos;
	}

	@Override
	public double getWifiActiveDuration() {
		return this.wifiActiveDuration;
	}

}
