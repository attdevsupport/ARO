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
package com.att.aro.core.packetanalysis.pojo;

/**
 * helper class for TCPSession
 */

public class PacketRangeInStorage {
	private int offset;
	private int size;
	private int pktID;
	
	public PacketRangeInStorage(int offset, int size, int pktID) {
		this.offset = offset;
		this.size = size;
		this.pktID = pktID;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getPktID() {
		return pktID;
	}
	public void setPktID(int pktID) {
		this.pktID = pktID;
	}	
}
