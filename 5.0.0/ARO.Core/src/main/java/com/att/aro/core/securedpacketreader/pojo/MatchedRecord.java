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

public class MatchedRecord implements Comparable<MatchedRecord> {

	private int beginBDC = -1;
	private int beginOfs = -1;
	private int endBDC = -1;
	private int endOfs = -1;
	private PacketDirection direction = PacketDirection.UNKNOWN;
	private int bytes = -1;
	private int uniDirOffset = -1;
	
	@Override
	public int compareTo(MatchedRecord arg0) {
		int result = Integer.valueOf(this.endBDC).compareTo(arg0.beginBDC);
		if(result == 0){
			if (this.endBDC == arg0.beginBDC && this.endOfs < arg0.beginOfs) {
				result = -1;
			} else {
				if (this.direction == PacketDirection.UPLINK && arg0.direction == PacketDirection.DOWNLINK) {
					result = lessThan(this.beginBDC, this.beginOfs, arg0.endBDC, arg0.endOfs);
				} else if (this.direction == PacketDirection.DOWNLINK && arg0.direction == PacketDirection.UPLINK) {
					result = lessThan(this.endBDC, this.endOfs, arg0.beginBDC, arg0.beginOfs);
				} 
			}
		}
		return result;
	}
	int lessThan(int bdcX, int ofsX, int bdcY, int ofsY) {
		if (bdcX < bdcY) {
			return 1;
		}
		if (bdcX > bdcY) {
			return 0;
		}
		if (ofsX < ofsY) {
			return 1; 
		} else {
			return 0;
		}
		
	}
	public void setInput(int beginBDC, int beginOfs, PacketDirection dir, int unidirOffset) {
		this.beginBDC = beginBDC;
		this.beginOfs = beginOfs;
		this.direction = dir;
		this.uniDirOffset = unidirOffset;
	}
	
	public int getBeginBDC() {
		return this.beginBDC;
	}

	public void setBeginBDC(int beginBDC) {
		this.beginBDC = beginBDC;
	}

	public int getBeginOfs() {
		return this.beginOfs;
	}

	public void setBeginOfs(int beginOfs) {
		this.beginOfs = beginOfs;
	}

	public int getEndBDC() {
		return this.endBDC;
	}

	public void setEndBDC(int endBDC) {
		this.endBDC = endBDC;
	}

	public int getEndOfs() {
		return this.endOfs;
	}

	public void setEndOfs(int endOfs) {
		this.endOfs = endOfs;
	}

	public PacketDirection getDir() {
		return this.direction;
	}

	public void setDir(PacketDirection dir) {
		this.direction = dir;
	}

	public int getBytes() {
		return this.bytes;
	}

	public void setBytes(int bytes) {
		this.bytes = bytes;
	}

	public int getUniDirOffset() {
		return this.uniDirOffset;
	}

	public void setUniDirOffset(int uniDirOffset) {
		this.uniDirOffset = uniDirOffset;
	}
}//end
