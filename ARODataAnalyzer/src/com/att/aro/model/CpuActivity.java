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
 * Encapsulates information about the activity of the CPU.
 */
public class CpuActivity {

	// Radio time stamp
	private double beginTimeStamp;
	private double endTimeStamp;
	private double cpuUsage;

	/**
	 * Initializes an instance of the CpuActivity class using the specified timestamps and 
	 * CPU usage value.
	 * 
	  
	 * @param beginTimeStamp The starting timestamp for the cpu event.
	 * @param endTimeStamp The ending timestamp for the cpu event.
	 * 
	 * @param dCpuUsage The CPU usage value.
	 */
	public CpuActivity(double beginTimeStamp , double endTimeStamp, double dCpuUsage) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.cpuUsage = dCpuUsage;
	}

	/**
	 * Returns the starting timestamp for the CPU activity event. 
	 * 
	 * @return The starting timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}
	
	 /** Returns the ending timestamp for the CPU activity event.  
	 * 
	 * @return The ending timestamp.
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
	}

	/**
	 * Sets the start timestamp for the cpu event. 
	 * 
	 * @param beginTimeStamp - The start timestamp.
	 */
	public void setCpuBeginTimeStamp(double beginTimeStamp) {
		this.beginTimeStamp = beginTimeStamp;
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
