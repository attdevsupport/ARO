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
package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.INetworkTypeReader;
import com.att.aro.core.peripheral.pojo.NetworkType;
import com.att.aro.core.peripheral.pojo.NetworkTypeObject;
import com.att.aro.core.util.Util;

public class NetworkTypeReaderImpl extends PeripheralBase implements INetworkTypeReader {
	
	@InjectLogger
	private static ILogger logger;

	@Override
	public NetworkTypeObject readData(String directory, double startTime, double traceDuration) {
		NetworkTypeObject obj = null;
		List<NetworkBearerTypeInfo> networkTypeInfos = new ArrayList<NetworkBearerTypeInfo>();
		List<NetworkType> networkTypesList = new ArrayList<NetworkType>();

		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.NETWORKINFO_FILE;
		if (!filereader.fileExist(filepath)) {
			return obj;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to read network info file: " + filepath);
		}

		String line;
		if (lines != null && lines.length > 0) {
			line = lines[0];
			// Clear any data that may have been added by device_details
			networkTypeInfos.clear();

			NetworkType networkType;
			double beginTime;
			double endTime;
			String[] fields = line.split(" ");
			if (fields.length == 2) {
				beginTime = Util.normalizeTime(Double.parseDouble(fields[0]), startTime);
				try {
					networkType = getNetworkTypeFromCode(Integer.parseInt(fields[1]));
				} catch (NumberFormatException e) {
					networkType = NetworkType.none;
					logger.warn("Invalid network type [" + fields[1] + "]");
				}
				networkTypesList.add(networkType);
				for (int i = 1; i < lines.length; i++) {
					line = lines[i];
					fields = line.split(" ");
					if (fields.length == 2) {
						endTime = Util.normalizeTime(Double.parseDouble(fields[0]), startTime);
						networkTypeInfos.add(new NetworkBearerTypeInfo(beginTime, endTime, networkType));
						try {
							networkType = getNetworkTypeFromCode(Integer.parseInt(fields[1]));
						} catch (NumberFormatException e) {
							networkType = NetworkType.none;
							logger.warn("Invalid network type [" + fields[1] + "]");
						}
						beginTime = endTime;
						if (!networkTypesList.contains(networkType)) {
							networkTypesList.add(networkType);
						}
					}
				}
				networkTypeInfos.add(new NetworkBearerTypeInfo(beginTime, traceDuration, networkType));
			}
		}
		obj = new NetworkTypeObject();
		obj.setNetworkTypeInfos(networkTypeInfos);
		obj.setNetworkTypesList(networkTypesList);
		return obj;
	}

	private NetworkType getNetworkTypeFromCode(int networkTypeCode) {
		switch (networkTypeCode) {
		case TraceDataConst.TraceNetworkType.WIFI:
			return NetworkType.WIFI;
		case TraceDataConst.TraceNetworkType.EDGE:
			return NetworkType.EDGE;
		case TraceDataConst.TraceNetworkType.GPRS:
			return NetworkType.GPRS;
		case TraceDataConst.TraceNetworkType.UMTS:
			return NetworkType.UMTS;
		case TraceDataConst.TraceNetworkType.ETHERNET:
			return NetworkType.ETHERNET;
		case TraceDataConst.TraceNetworkType.HSDPA:
			return NetworkType.HSDPA;
		case TraceDataConst.TraceNetworkType.HSUPA:
			return NetworkType.HSUPA;
		case TraceDataConst.TraceNetworkType.HSPA:
			return NetworkType.HSPA;
		case TraceDataConst.TraceNetworkType.HSPAP:
			return NetworkType.HSPAP;
		case TraceDataConst.TraceNetworkType.LTE:
			return NetworkType.LTE;
		case TraceDataConst.TraceNetworkType.NONE:
			return NetworkType.none;
		default:
			return NetworkType.none;
		}

	}
}//end class
