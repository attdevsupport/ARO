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

import java.io.Serializable;

/**
 * Encapsulates Radio info
 */
public class RadioInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// Radio time stamp
	private double radioTimeStamp;

	// Radio signal strength
	private double signalStrength;

	/**
	 * Constructor
	 * 
	 * @param radioTimeStamp
	 * @param signalStrength
	 */
	public RadioInfo(double radioTimeStamp, double signalStrength) {
		this.radioTimeStamp = radioTimeStamp;
		this.signalStrength = signalStrength;
	}

	/**
	 * Returns radio timestamp.
	 * 
	 * @return radio timestamp.
	 */
	public double getTimeStamp() {
		return radioTimeStamp;
	}

	/**
	 * Returns radio signal strength.
	 * 
	 * @return radio signal strength.
	 */
	public double getSignalStrength() {
		return signalStrength;
	}

}
