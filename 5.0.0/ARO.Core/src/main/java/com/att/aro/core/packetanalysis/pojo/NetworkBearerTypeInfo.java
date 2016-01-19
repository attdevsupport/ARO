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

import com.att.aro.core.peripheral.pojo.NetworkType;

public class NetworkBearerTypeInfo {

	private NetworkType networkType;
	private double beginTimestamp;
	private double endTimestamp;

	public NetworkBearerTypeInfo(double beginTimestamp, double endTimestamp, NetworkType networkType) {
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
		this.networkType = networkType;
	}

	/**
	 * Returns the network bearer type.
	 * 
	 * @return The bearer type.
	 */
	public NetworkType getNetworkType() {
		return networkType;
	}

	/**
	 * Sets the network bearer type.
	 * 
	 * @param networkType
	 *            The bearer type.
	 */
	public void setNetworkType(NetworkType networkType) {
		this.networkType = networkType;
	}

	/**
	 * Returns the begin time stamp for the network bearer type.
	 * 
	 * @return The begin time stamp.
	 */
	public double getBeginTimestamp() {
		return beginTimestamp;
	}

	/**
	 * Sets the begin time stamp for the network bearer type
	 * 
	 * @param beginTimestamp
	 *            The begin time stamp.
	 */
	public void setBeginTimestamp(double beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end time stamp for the network bearer type.
	 * 
	 * @return The end time stamp.
	 */

	public double getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Sets the end time stamp for the network bearer type.
	 * 
	 * @param endTimestamp
	 *            The end time stamp.
	 */
	public void setEndTimestamp(double endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

}
