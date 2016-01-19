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
package com.att.aro.core.packetanalysis.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.att.aro.core.peripheral.pojo.CpuActivityList;

/**
 * Represents end-user selected processes.
 */
public class FilteredProcessSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<ProcessSelection> selection;
	private TimeRange timeRange;

	/**
	 * Initializes an instance of the FilteredProcessSelection.
	 * 
	 * @param cpuActivityList
	 *            CPU activity list
	 */
	public FilteredProcessSelection(CpuActivityList cpuActivityList) {

		// all available process names
		Set<String> processNames = cpuActivityList.getAllProcesses();
		selection = new ArrayList<ProcessSelection>(processNames.size());
		for (String process : processNames) {
			selection.add(new ProcessSelection(process));
		}
	}

	/**
	 * Creates a new FilteredProcessSelection object as a copy of the provided
	 * object.
	 * 
	 * @param filteredProcessSelection
	 *            Original object.
	 */
	public FilteredProcessSelection(FilteredProcessSelection filteredProcessSelection) {

		this.timeRange = filteredProcessSelection.timeRange;
		List<ProcessSelection> fps = filteredProcessSelection.selection;
		ProcessSelection processSelection;
		this.selection = new ArrayList<ProcessSelection>(fps.size());
		for (ProcessSelection pSelection : fps) {
			processSelection = new ProcessSelection(pSelection.getProcessName(), pSelection.isSelected());
			this.selection.add(processSelection);
		}
	}

	/**
	 * Returns the selection of the processes.
	 * 
	 * @return A collection of ApplicationSelection objects.
	 */
	public Collection<ProcessSelection> getProcessSelection() {
		return selection;
	}

}
