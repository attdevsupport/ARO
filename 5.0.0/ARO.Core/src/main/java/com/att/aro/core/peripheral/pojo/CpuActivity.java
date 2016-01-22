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
package com.att.aro.core.peripheral.pojo;

import java.util.List;

/**
 * Encapsulates information about CPU activity.
 */
public class CpuActivity {

	public static final int PROCESS_INFO_IDX = 2;
	public static final String SPLIT_LINE_REG_EXPR = "[ =]";

	public static final int TIMESTAMP_IDX = 0;
	public static final int TOTAL_CPU_INFO_IDX = 1;

	// CPU utilization of processes not captured in the CPU file
	private double cpuUsageOther;
	// total CPU utilization
	private double cpuUsageTotal;
	// Recalculated total CPU utilization after the end user added/removed
	// processes
	private double cpuUsageTotalFiltered;
	// list of processes and...
	private List<String> processNames;
	// ...their individual CPU utilization.
	private List<Double> cpuProcessUsages;
	// timestamp when the CPU utilization snapshot was taken
	private double timeStamp;

	public CpuActivity(){}


	/**
	 * Returns CPU usage consumed by processes which were not collected, 'other'
	 * 
	 * @return other CPU usage
	 */
	public double getCpuUsageOther() {
		return cpuUsageOther;
	}
	public void setCpuUsageOther(double cpuUsageOther){
		this.cpuUsageOther = cpuUsageOther;
	}

	/**
	 * Get list of individual process CPU usages.
	 * 
	 * @return List of process CPU usages.
	 */
	public List<Double> getCpuUsages() {
		return cpuProcessUsages;
	}
	/**
	 * Sets CPU process usages.
	 * @param cpuProcessUsages
	 */
	public void setCpuUsages(List<Double> cpuProcessUsages) {
		this.cpuProcessUsages = cpuProcessUsages;
	}


	/**
	 * @return the cpuUsageTotalFiltered
	 */
	public double getCpuUsageTotalFiltered() {
		return cpuUsageTotalFiltered;
	}
	/**
	 * @param cpuUsageTotalFiltered
	 *            the cpuUsageTotalFiltered to set
	 */
	public void setCpuUsageTotalFiltered(double cpuUsageTotalFiltered) {
		this.cpuUsageTotalFiltered = cpuUsageTotalFiltered;
	}

	/**
	 * Get list of processes.
	 * 
	 * @return List of processes.
	 */
	public List<String> getProcessNames() {
		return processNames;
	}
	/**
	 * Sets process names.
	 * 
	 * @param processNames
	 *            Process names.
	 */
	public void setProcessNames(List<String> processNames) {
		this.processNames = processNames;
	}

	/**
	 * Returns the starting timestamp for the CPU activity event.
	 * 
	 * @return The starting timestamp.
	 */
	public double getTimeStamp() {
		return timeStamp;
	}
	public void setTimestamp(double timestamp){
		this.timeStamp = timestamp;
	}

	/**
	 * Returns the total CPU usage value.
	 * 
	 * @return A double that is the total CPU usage value.
	 */
	public double getTotalCpuUsage() {
		return cpuUsageTotal;
	}
	public void setTotalCpuUsage(double cpuUsagetotal){
		this.cpuUsageTotal = cpuUsagetotal;
	}

	
}
