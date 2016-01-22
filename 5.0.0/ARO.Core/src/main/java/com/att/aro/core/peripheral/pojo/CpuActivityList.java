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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.att.aro.core.packetanalysis.pojo.FilteredProcessSelection;
import com.att.aro.core.packetanalysis.pojo.ProcessSelection;

/**
 * Represents a collection of CPU utilization for each individual process.
 */
public class CpuActivityList {

	// all processes in the CPU file
	private Set<String> allProcesses = new TreeSet<String>();
	// Filtered trace begin and end time.
	private double beginTraceTime;
	private double endTraceTime;
	// Indicates whether all data or data filtered by time should be displayed.
	private boolean filterByTime = false;
	// end-user filtered processes
	private FilteredProcessSelection processSelection;
	// A collection of CPU activities.
	private List<CpuActivity> cpuActivities;

	/**
	 * Initializes a new instance of class which represents a collection of CPU
	 * utilization for each individual process.
	 */
	public CpuActivityList() {
		this.cpuActivities = new ArrayList<CpuActivity>();
	}

	/**
	 * Adds a process to the list of all processes.
	 * 
	 * @param processName
	 */
	public void addProcess(String processName) {
		this.allProcesses.add(processName);
	}

	/**
	 * Returns all processes contained in the CPU trace file.
	 * 
	 * @return the allProcesses
	 */
	public Set<String> getAllProcesses() {
		return this.allProcesses;
	}

	/**
	 * Gets beginning trace time specified by end-user used for graphical
	 * display.
	 * 
	 * @return the beginTraceTime
	 */
	public double getBeginTraceTime() {
		return this.beginTraceTime;
	}

	/**
	 * Gets end trace time specified by end-user used for graphical display.
	 * 
	 * @return the endTraceTime
	 */
	public double getEndTraceTime() {
		return this.endTraceTime;
	}

	/**
	 * @return the processSelection
	 */
	public FilteredProcessSelection getProcessSelection() {
		return this.processSelection;
	}

	/**
	 * Indicates whether the graph should be filtered by end-user specified time
	 * range.
	 * 
	 * @return filterByTime
	 */
	public boolean isFilterByTime() {
		return this.filterByTime;
	}

	public boolean isProcessSelected(String processName) {
		if (this.processSelection != null) {
			return ProcessSelection.isProcessSelected(this.processSelection.getProcessSelection(), processName);
		} else {
			return true;
		}
	}

	/**
	 * Store reference to the end-user filtered processes.
	 * 
	 * @param processSelection
	 *            the processSelection to set
	 */
	public void setProcessSelection(FilteredProcessSelection processSelection) {
		this.processSelection = processSelection;
	}

	/**
	 * Add CPU activity snapshot to the collection. also add process name from each collection in this CpuActivity.
	 * 
	 * @param cpuActivity
	 */
	public void add(CpuActivity cpuActivity) {
		cpuActivities.add(cpuActivity);
		if(cpuActivity.getProcessNames() != null){
			for(String procname : cpuActivity.getProcessNames()){
				this.addProcess(procname);
			}
		}
	}

	/**
	 * Returns a collection of CPU utilization for each individual process.
	 * 
	 * @return the cpuActivityList
	 */
	public List<CpuActivity> getCpuActivities() {
		return cpuActivities;
	}

	/**
	 * Calculate CPU usage of all end-user excluded processes & subtract it from
	 * the total CPU utilization
	 */
	public void recalculateTotalCpu() {

		double totalCpuUsage;
		double cpuUsageToExclude;
		List<String> processNames;
		List<Double> processCpu;
		for (CpuActivity cpuA : cpuActivities) {

			cpuUsageToExclude = 0;
			processNames = cpuA.getProcessNames();
			processCpu = cpuA.getCpuUsages();
			for (int i = 0; i < processNames.size(); i++) {

				if (!isProcessSelected(processNames.get(i))) {
					cpuUsageToExclude += processCpu.get(i);
				}
			}
			totalCpuUsage = cpuA.getTotalCpuUsage();
			cpuA.setCpuUsageTotalFiltered(totalCpuUsage - cpuUsageToExclude);
		}
	}

	/**
	 * Updates the trace beginning and end time as specified by end-user.
	 * 
	 * @param beginTime
	 * @param endTime
	 */
	public void updateTimeRange(double beginTime, double endTime) {
		this.beginTraceTime = beginTime;
		this.endTraceTime = endTime;
		this.filterByTime = true;
	}

}
