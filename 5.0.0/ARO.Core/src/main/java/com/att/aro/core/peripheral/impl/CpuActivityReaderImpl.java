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

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.ICpuActivityParser;
import com.att.aro.core.peripheral.ICpuActivityReader;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.peripheral.pojo.CpuActivityList;
import com.att.aro.core.util.Util;

/**
 * Reads the CPU trace information from the CPU file.
 * 
 * @author Borey Sao Date: September 30, 2014
 */
public class CpuActivityReaderImpl extends PeripheralBase implements ICpuActivityReader {

	@Autowired
	private ICpuActivityParser cpuActivityParser;
	
	@InjectLogger
	private static ILogger logger;

	@Override
	public CpuActivityList readData(String directory, double startTime) {
		String cpuFileName = TraceDataConst.FileName.CPU_FILE;
		CpuActivityList cpuActivityList = new CpuActivityList();
		logger.debug("Reading CPU file...");
		String filepath = directory + Util.FILE_SEPARATOR + cpuFileName;
		if (!filereader.fileExist(filepath)) {
			logger.warn("cpu file not found: " + cpuFileName);
			return cpuActivityList;
		}
		String[] lines;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("Failed to read CPU activity file: " + filepath);
			return cpuActivityList;
		}

		for (String line : lines) {
			if (!line.trim().isEmpty()) {
				CpuActivity cpuact = cpuActivityParser.parseCpuLine(line, startTime);
				cpuActivityList.add(cpuact);
			}
		}
		return cpuActivityList;
	}

}
