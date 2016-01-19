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
import java.util.Collection;

/**
 * Represents one end-user selectable process on the Select Process dialog.
 */
public class ProcessSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private String processName;
	private boolean selected = true;

	/**
	 * Initializes an instance of user selectable process on the Select Process
	 * dialog.
	 * 
	 * @param processName
	 *            The process name.
	 */
	public ProcessSelection(String processName) {
		this.processName = processName;
	}

	/**
	 * Initializes an instance of user selectable process on the Select Process
	 * dialog.
	 * 
	 * @param processName
	 *            The process name.
	 * @param isSelected
	 *            Indicates whether the process is selected or not.
	 */
	public ProcessSelection(String processName, boolean isSelected) {
		this.processName = processName;
		this.selected = isSelected;
	}

	/**
	 * Returns true when end-user has selected the process for display.
	 * 
	 * @param proselection
	 *            The collection of all selectable processes.
	 * @param process
	 *            Process name
	 * @return Returns true when end-user has selected the process for display.
	 *         Returns false when end-user has deselected the process for
	 *         display.
	 */
	public static boolean isProcessSelected(Collection<ProcessSelection> proselection, String process) {

		if (proselection != null) {
			for (ProcessSelection processSelection : proselection) {
				if (processSelection.processName.equals(process)) {
					return processSelection.selected;
				}
			}
		} else {
			// Before the end user makes the selection the object is null
			// hence the process is selected by default.
			return true;
		}
		return false;
	}

	/**
	 * Returns the process name.
	 * 
	 * @return A string that is the process name.
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * Retrieves the selection status of the process.
	 * 
	 * @return A boolean value that is true if this process is selected and
	 *         false otherwise.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selection status of the process.
	 * 
	 * @param selected
	 *            A boolean value that indicates the selected status to set for
	 *            this process.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
