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

import com.att.aro.core.packetreader.pojo.PacketDirection;

public class BidirDataChunk {
	private PacketDirection direction;
	private int nBytes;
	private int nPrevBytes;
	public BidirDataChunk(){
		this.direction = PacketDirection.UNKNOWN;
		this.nBytes = 0;
		this.nPrevBytes = 0;
	}
	public PacketDirection getDirection() {
		return direction;
	}
	public void setDirection(PacketDirection direction) {
		this.direction = direction;
	}
	public int getnBytes() {
		return nBytes;
	}
	public void setnBytes(int nBytes) {
		this.nBytes = nBytes;
	}
	public int getnPrevBytes() {
		return nPrevBytes;
	}
	public void setnPrevBytes(int nPrevBytes) {
		this.nPrevBytes = nPrevBytes;
	}
	
}
