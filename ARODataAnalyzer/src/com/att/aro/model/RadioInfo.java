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
 * Encapsulates information about the Radio signal.
 */
public class RadioInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// Radio time stamp
	private double radioTimeStamp;

	// Radio signal strength
	private boolean lte;
	private double signalStrength;
	private int lteSignalStrength;
	private int lteRsrp;
	private int lteRsrq;
	private int lteRssnr;
	private int lteCqi;

	/**
	 * Initializes an instance of the RadioInfo class using the specified timestamp and signal 
	 * strength.
	 * 
	 * @param radioTimeStamp – The timestamp at which the radio information is evaluated.
	 * 
	 * @param signalStrength – The radio signal strength, expressed in Dbm.
	 */
	public RadioInfo(double radioTimeStamp, double signalStrength) {
		this.radioTimeStamp = radioTimeStamp;
		this.signalStrength = signalStrength;
	}

	/**
	 * Initializes an instance of RadioInfo with LTE signal parameters
	 * @param radioTimeStamp
	 * @param lteSignalStrength
	 * @param lteRsrp
	 * @param lteRsrq
	 * @param lteRssnr
	 * @param lteCqi
	 */
	public RadioInfo(double radioTimeStamp, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi) {
		this.radioTimeStamp = radioTimeStamp;
		this.lte = true;
		this.signalStrength = lteRsrp > 0 ? - lteRsrp : lteRsrp;
		this.lteSignalStrength = lteSignalStrength;
		this.lteRsrp = lteRsrp;
		this.lteRsrq = lteRsrq;
		this.lteRssnr = lteRssnr;
		this.lteCqi = lteCqi;
	}

	/**
	 * Returns the radio timestamp. 
	 * 
	 * @return The radio timestamp.
	 */
	public double getTimeStamp() {
		return radioTimeStamp;
	}

	/**
	 * Returns the radio signal strength. 
	 * 
	 * @return The radio signal strength, in Dbm.
	 */
	public double getSignalStrength() {
		return signalStrength;
	}

	/**
	 * @return the lte
	 */
	public boolean isLte() {
		return lte;
	}

	/**
	 * @return the lteSignalStrength
	 */
	public int getLteSignalStrength() {
		return lteSignalStrength;
	}

	/**
	 * @return the lteRsrp
	 */
	public int getLteRsrp() {
		return lteRsrp;
	}

	/**
	 * @return the lteRsrq
	 */
	public int getLteRsrq() {
		return lteRsrq;
	}

	/**
	 * @return the lteRssnr
	 */
	public int getLteRssnr() {
		return lteRssnr;
	}

	/**
	 * @return the lteCqi
	 */
	public int getLteCqi() {
		return lteCqi;
	}

}
