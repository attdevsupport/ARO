/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

/**
 * Encapsulates information about the actrivity of the CPU.
 */
public class CpuActivity {

	private double cpuTimeStamp;
	private double cpuUsage;

	/**
	 * Initializes an instance of the CpuActivity class using the specified timestamp and 
	 * CPU usage value.
	 * 
	 * @param Timestamp – The timestamp for the CPU.
	 * 
	 * @param dCpuUsage – The CPU usage value.
	 */
	public CpuActivity(double dTimestamp, double dCpuUsage) {
		this.cpuTimeStamp = dTimestamp;
		this.cpuUsage = dCpuUsage;
	}

	/**
	 * Returns the CPU timestamp. 
	 * 
	 * @return The CPU timestamp.
	 */
	public double getCpuTimeStamp() {
		return cpuTimeStamp;
	}

	/**
	 * Sets the CPU timestamp to the specified value. 
	 * 
	 * @param timestamp - A double that specifies the CPU timestamp.
	 */
	public void setCpuTimeStamp(double dTimeStamp) {
		cpuTimeStamp = dTimeStamp;
	}

	/**
	 * Returns the CPU usage value. 
	 * 
	 * @return A double that is the CPU usage value.
	 */
	public double getUsage() {
		return cpuUsage;
	}
}
