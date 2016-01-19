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

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IDeviceDetailReader;
import com.att.aro.core.peripheral.pojo.DeviceDetail;
import com.att.aro.core.util.Util;

/**
 * Reads a device data from the device file in trace folder.
 * 
 * @author Borey Sao
 * Date: October 7, 2014
 */
public class DeviceDetailReaderImpl extends PeripheralBase implements IDeviceDetailReader {
	@InjectLogger
	private static ILogger logger;
	
	@Override
	public DeviceDetail readData(String directory) {
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.DEVICEDETAILS_FILE;
		DeviceDetail device = new DeviceDetail();
		
		if (!filereader.fileExist(filepath)) {
			return null;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to read device detail file: "+filepath);
		}

		if(lines == null || lines.length < 1){
			return null;
		}
		device.setTotalLines(lines.length);
		
		// line #1
		device.setCollectorName(lines[0]);
		// line #2
		device.setDeviceModel(lines[1]);
		// line #3
		device.setDeviceMake(lines[2]);
		// line #4
		device.setOsType(lines[3]);
		// line #5
		device.setOsVersion(lines[4]);
		// line #6
		device.setCollectorVersion(lines[5]);
//		if(lines.length > 6){
			// line #7
			//readNetworkType(lines[6]);
//		}
		if(lines.length > 7){
			// line #8
			device.setScreenSize(lines[7]);
		}
		return device;
	}

}
