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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IDeviceInfoReader;
import com.att.aro.core.util.Util;

public class DeviceInfoReaderImpl extends PeripheralBase implements IDeviceInfoReader {
	
	@InjectLogger
	private static ILogger logger;
	
	@Override
	public Set<InetAddress> readData(String directory) {
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.DEVICEINFO_FILE;
		Set<InetAddress> localIPAddresses = new HashSet<InetAddress>(1);
		if (!filereader.fileExist(filepath)) {
			return localIPAddresses;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to read Device Info file: "+filepath);
		}
		if(lines != null){
			for(String line: lines) {
	
				// In case of IPv6 scoped address, remove scope ID
				int index = line.indexOf('%');
				try {
					localIPAddresses.add(InetAddress.getByName(index > 0 ? line.substring(0, index) : line));
				} catch (UnknownHostException e) {
					logger.error("failed to read ip address: "+line);
				}
			}
		}
		return localIPAddresses;
	}

}
