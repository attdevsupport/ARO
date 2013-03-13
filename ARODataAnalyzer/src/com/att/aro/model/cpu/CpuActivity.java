/*
 *  Copyright 2013 AT&T
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

package com.att.aro.model.cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.att.aro.util.Util;

/**
 * Encapsulates information about CPU activity.
 */
public class CpuActivity {

	private static final Logger LOGGER = Logger.getLogger(CpuActivity.class.getName());

	private static final int PROCESS_INFO_IDX = 2;
	private static final String SPLIT_LINE_REG_EXPR = "[ =]";

	private static final int TIMESTAMP_IDX = 0;
	private static final int TOTAL_CPU_INFO_IDX = 1;

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

	/**
	 * Initializes an instance of the CpuActivity class using the specified line
	 * read from the CPU file.
	 * 
	 * @param splitLine
	 *            Array of values read from the CPU file.
	 * @param pcapTime
	 *            The start of the trace time.
	 */
	public CpuActivity(String[] splitLine, double pcapTime) {

		double time = Double.parseDouble(splitLine[TIMESTAMP_IDX]);
		this.timeStamp = Util.normalizeTime(time, pcapTime);
		this.cpuUsageTotal = Double.parseDouble(splitLine[TOTAL_CPU_INFO_IDX]);
		this.cpuUsageTotalFiltered = this.cpuUsageTotal;
	}

	/**
	 * Parses a line read from the CPU file and creates an instance representing
	 * the line as CPU activity.
	 * 
	 * @param cpuLine
	 *            Line read from the CPU file.
	 * @param pcapTime
	 *            The start of the trace time.
	 * @return Returns an instance representing CPU activity
	 */
	public static CpuActivity parseCpuLine(CpuActivityList cpuActivityList, String cpuLine, double pcapTime) {

		CpuActivity cpuActivity = null;

		String splitLine[] = cpuLine.split(SPLIT_LINE_REG_EXPR);
		int numOfElements = splitLine.length;

		if (numOfElements == (TOTAL_CPU_INFO_IDX + 1)) {

			cpuActivity = new CpuActivity(splitLine, pcapTime);

		} else if (numOfElements >= (PROCESS_INFO_IDX + 1)) {

			if (((numOfElements - PROCESS_INFO_IDX) % 2) == 0) {

				cpuActivity = new CpuActivity(splitLine, pcapTime);
				cpuActivity.addProcessNamesAndCpuUsage(cpuActivityList, splitLine);
				cpuActivity.addOtherCpuUsage();

			} else {
				LOGGER.severe("CPU file is not well formated, number of elements: " + numOfElements);
			}

		} else {
			LOGGER.severe("CPU file is missing time and total CPU information");
		}

		return cpuActivity;
	}

	/**
	 * Calculates CPU usage consumed by processes which were not collected.
	 */
	private void addOtherCpuUsage() {
		double other = 0.0;
		for (Double individualCpuUsage : this.cpuProcessUsages) {
			other += individualCpuUsage.doubleValue();
		}
		this.cpuUsageOther = this.cpuUsageTotal - other;
	}

	/**
	 * Adds names for processes and their individual CPU usage.
	 * 
	 * @param splitLine
	 *            The line read from the CPU file.
	 */
	private void addProcessNamesAndCpuUsage(CpuActivityList cpuActivityList, String[] splitLine) {

		List<String> processNameList = new ArrayList<String>();
		List<Double> cpuProcessUsageList = new ArrayList<Double>();
		String procName;
		String cpuUsage;
		for (int i = PROCESS_INFO_IDX; i < splitLine.length; i++) {
			procName = splitLine[i];
			cpuUsage = splitLine[++i];
			cpuActivityList.addProcess(procName);
			processNameList.add(procName);
			cpuProcessUsageList.add(Double.parseDouble(cpuUsage));
		}
		setProcessNames(processNameList);
		setCpuProcessUsages(cpuProcessUsageList);
	}

	/**
	 * Returns CPU usage consumed by processes which were not collected, 'other'
	 * 
	 * @return other CPU usage
	 */
	public double getCpuUsageOther() {
		return cpuUsageOther;
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
	 * @return the cpuUsageTotalFiltered
	 */
	public double getCpuUsageTotalFiltered() {
		return cpuUsageTotalFiltered;
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
	 * Returns the starting timestamp for the CPU activity event.
	 * 
	 * @return The starting timestamp.
	 */
	public double getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns the total CPU usage value.
	 * 
	 * @return A double that is the total CPU usage value.
	 */
	public double getTotalCpuUsage() {
		return cpuUsageTotal;
	}

	/**
	 * Sets CPU process usages.
	 * 
	 * @param cpuProcessUsages
	 *            CPU process usages.
	 */
	public void setCpuProcessUsages(List<Double> cpuProcessUsages) {
		this.cpuProcessUsages = cpuProcessUsages;
	}

	/**
	 * @param cpuUsageTotalFiltered
	 *            the cpuUsageTotalFiltered to set
	 */
	public void setCpuUsageTotalFiltered(double cpuUsageTotalFiltered) {
		this.cpuUsageTotalFiltered = cpuUsageTotalFiltered;
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

}
