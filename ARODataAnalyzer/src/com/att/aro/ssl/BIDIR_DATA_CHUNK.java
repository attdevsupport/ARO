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
package com.att.aro.ssl;

import com.att.aro.model.PacketInfo;
import com.att.aro.model.PacketInfo.Direction;

public class BIDIR_DATA_CHUNK {
	private Direction dir;
	private int nBytes;
	private int nPrevBytes;
	
	public BIDIR_DATA_CHUNK() {
		this.dir = Direction.UNKNOWN;
		this.nBytes = 0;
		this.nPrevBytes = 0;
	}
	
	public void setDir(Direction dir) {
		this.dir = dir;
	}
	
	public Direction getDir() {
		return this.dir;
	}
	
	public void setBytes(int Bytes) {
		this.nBytes = Bytes;
	}
	
	public int getBytes() {
		return this.nBytes;
	}
	
	public void setPrevBytes(int PrevBytes) {
		this.nPrevBytes = PrevBytes;
	}
	
	public int getPrevBytes() {
		return this.nPrevBytes;
	}
}