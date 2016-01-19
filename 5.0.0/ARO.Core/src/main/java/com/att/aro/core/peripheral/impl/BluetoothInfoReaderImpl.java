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
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IBluetoothInfoReader;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo.BluetoothState;
import com.att.aro.core.util.Util;

/**
 * Method to read the Bluetooth data from the trace file and store it in the
 * bluetoothInfos list. It also updates the active duration for Bluetooth.
 * 
 * @author EDS team Refactored by Borey Sao Date: September 30, 2014
 *
 */
public class BluetoothInfoReaderImpl extends PeripheralBase implements IBluetoothInfoReader {
	@InjectLogger
	private static ILogger logger;

	private double activeBluetoothDuration = 0.0;

	@Override
	public List<BluetoothInfo> readData(String directory, double startTime, double traceDuration) {
		List<BluetoothInfo> bluetoothInfos = new ArrayList<BluetoothInfo>();
		this.activeBluetoothDuration = 0;
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.BLUETOOTH_FILE;

		if (!filereader.fileExist(filepath)) {
			return bluetoothInfos;
		}

		double beginTime = 0.0;
		double endTime;
		double dLastTimeStamp = 0.0;
		double dActiveDuration = 0.0;
		BluetoothState prevBtState = null;
		BluetoothState btState = null;
		BluetoothState lastState = null;
		String firstLine;
		String strLineBuf;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed reading Bluetooth info file: " + filepath);
		}
		if (lines != null && lines.length > 0) {
			firstLine = lines[0];
			String strFieldsFirstLine[] = firstLine.split(" ");
			if (strFieldsFirstLine.length == 2) {
				try {
					beginTime = Util.normalizeTime(Double.parseDouble(strFieldsFirstLine[0]), startTime);
					if (TraceDataConst.BLUETOOTH_CONNECTED.equals(strFieldsFirstLine[1])) {
						prevBtState = BluetoothState.BLUETOOTH_CONNECTED;
					} else if (TraceDataConst.BLUETOOTH_DISCONNECTED.equals(strFieldsFirstLine[1])) {
						prevBtState = BluetoothState.BLUETOOTH_DISCONNECTED;
					} else if (TraceDataConst.BLUETOOTH_OFF.equals(strFieldsFirstLine[1])) {
						prevBtState = BluetoothState.BLUETOOTH_TURNED_OFF;
					} else {
						logger.warn("Unknown bluetooth state: " + firstLine);
						prevBtState = BluetoothState.BLUETOOTH_UNKNOWN;
					}
					// It is not possible for lastState to not be null at this point
					//		if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
					//			dActiveDuration += (beginTime - dLastTimeStamp);
					//		}
					lastState = prevBtState;
					dLastTimeStamp = beginTime;
				} catch (Exception e) {
					logger.warn("Unexpected error parsing bluetooth event: " + firstLine, e);
				}
			} else {
				logger.warn("Invalid Bluetooth trace entry: " + firstLine);
			}

			for (int i = 1; i < lines.length; i++) {
				strLineBuf = lines[i];
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					try {
						endTime = Util.normalizeTime(Double.parseDouble(strFields[0]), startTime);
						if (TraceDataConst.BLUETOOTH_CONNECTED.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_CONNECTED;
						} else if (TraceDataConst.BLUETOOTH_DISCONNECTED.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_DISCONNECTED;
						} else if (TraceDataConst.BLUETOOTH_OFF.equals(strFields[1])) {
							btState = BluetoothState.BLUETOOTH_TURNED_OFF;
						} else {
							logger.warn("Unknown bluetooth state: " + strLineBuf);
							btState = BluetoothState.BLUETOOTH_UNKNOWN;
						}
						bluetoothInfos.add(new BluetoothInfo(beginTime, endTime, prevBtState));

						if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
							dActiveDuration += (endTime - dLastTimeStamp);
						}
						lastState = btState;
						dLastTimeStamp = endTime;
						prevBtState = btState;
						beginTime = endTime;
					} catch (Exception e) {
						logger.warn("Unexpected error parsing bluetooth event: " + strLineBuf, e);
					}
				} else {
					logger.warn("Invalid Bluetooth trace entry: " + strLineBuf);
				}
			}
			bluetoothInfos.add(new BluetoothInfo(beginTime, traceDuration, prevBtState));
			// Duration calculation should probably be done in analysis
			if (lastState == BluetoothState.BLUETOOTH_CONNECTED) {
				dActiveDuration += Math.max(0, traceDuration - dLastTimeStamp);
			}
			this.activeBluetoothDuration = dActiveDuration;
		}
		return bluetoothInfos;
	}

	@Override
	public double getBluetoothActiveDuration() {
		return activeBluetoothDuration;
	}

}
