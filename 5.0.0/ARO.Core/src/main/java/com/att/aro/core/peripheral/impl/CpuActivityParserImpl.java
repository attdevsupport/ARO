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

import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.peripheral.ICpuActivityParser;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.util.Util;

/**
 * Helper classes for reading CPU activity from file
 * @author EDS team
 * Refactored by Borey Sao
 * Date: September 30, 2014
 */
public class CpuActivityParserImpl implements ICpuActivityParser {

	@Override
	public CpuActivity parseCpuLine(String cpuLine, double pcapTime) {
		CpuActivity cpuActivity = null;
		String pattern = "[\\s=]";
		String splitLine[] = cpuLine.split(pattern);
		int numOfElements = splitLine.length;

		if(numOfElements < (CpuActivity.TOTAL_CPU_INFO_IDX + 1)){
			return null;
		}
		cpuActivity = new CpuActivity();
		double time = Double.parseDouble(splitLine[CpuActivity.TIMESTAMP_IDX]);
		double timeStamp = Util.normalizeTime(time, pcapTime);
		double cpuUsageTotal = Double.parseDouble(splitLine[CpuActivity.TOTAL_CPU_INFO_IDX]);
		double cpuUsageTotalFiltered = cpuUsageTotal;
		
		cpuActivity.setTimestamp(timeStamp);
		cpuActivity.setTotalCpuUsage(cpuUsageTotal);
		cpuActivity.setCpuUsageTotalFiltered(cpuUsageTotalFiltered);

		if( (numOfElements >= (CpuActivity.PROCESS_INFO_IDX + 1)) &&
				(((numOfElements - CpuActivity.PROCESS_INFO_IDX) % 2) == 0)) {
			List<String> processNameList = new ArrayList<String>();
			List<Double> cpuProcessUsageList = new ArrayList<Double>();
			String procName;
			String cpuUsage;
			for (int i = CpuActivity.PROCESS_INFO_IDX; i < splitLine.length; i++) {
				procName = splitLine[i];
				cpuUsage = splitLine[++i];
				processNameList.add(procName);
				cpuProcessUsageList.add(Double.parseDouble(cpuUsage));
			}
			cpuActivity.setProcessNames(processNameList);
			cpuActivity.setCpuUsages(cpuProcessUsageList);
			
			double other = 0.0;
			for (Double individualCpuUsage : cpuActivity.getCpuUsages()) {
				other += individualCpuUsage.doubleValue();
			}
			other = cpuUsageTotal - other;
			cpuActivity.setCpuUsageOther(other);

		}

		return cpuActivity;
	}

}
