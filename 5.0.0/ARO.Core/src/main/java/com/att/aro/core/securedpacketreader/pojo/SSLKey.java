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
package com.att.aro.core.securedpacketreader.pojo;

public class SSLKey implements Comparable<SSLKey> {
	private int bUsed;
	private double tsvalue;
	private int preMasterLen;
	private int masterLen;
	private byte[] preMaster;
	private byte[] master;
	
	@Override
	public int compareTo(SSLKey arg0) {
		return Double.valueOf(getTsvalue()).compareTo(Double.valueOf(arg0.getTsvalue()));
	}

	public int getbUsed() {
		return bUsed;
	}

	public void setbUsed(int bUsed) {
		this.bUsed = bUsed;
	}

	public double getTsvalue() {
		return tsvalue;
	}

	public void setTs(double tsvalue) {
		this.tsvalue = tsvalue;
	}

	public int getPreMasterLen() {
		return preMasterLen;
	}

	public void setPreMasterLen(int preMasterLen) {
		this.preMasterLen = preMasterLen;
	}

	public int getMasterLen() {
		return masterLen;
	}

	public void setMasterLen(int masterLen) {
		this.masterLen = masterLen;
	}

	public byte[] getPreMaster() {
		return preMaster;
	}

	/**
	 * allocate a new array and copy data to new array
	 * @param preMaster
	 */
	public void setPreMaster(byte[] preMaster) {
		this.preMaster = new byte[preMaster.length];
		System.arraycopy(preMaster, 0, this.preMaster, 0, preMaster.length);
	}

	public byte[] getMaster() {
		return master;
	}

	/**
	 * allocate a new array and copy data to it.
	 * @param master
	 */
	public void setMaster(byte[] master) {
		this.master = new byte[master.length];
		System.arraycopy(master, 0, this.master, 0, master.length);
	}
	
	
}
