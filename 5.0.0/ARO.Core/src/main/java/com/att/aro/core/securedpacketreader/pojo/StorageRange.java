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

public class StorageRange {
	//a mapping from [x,y] to [nx, ny]
		private int xvalue;
		private int yvalue;

		private int nxvalue;
		private int nyvalue;
		public int getXvalue() {
			return xvalue;
		}
		public void setXvalue(int xvalue) {
			this.xvalue = xvalue;
		}
		public int getYvalue() {
			return yvalue;
		}
		public void setYvalue(int yvalue) {
			this.yvalue = yvalue;
		}
		public int getNxvalue() {
			return nxvalue;
		}
		public void setNxvalue(int nxvalue) {
			this.nxvalue = nxvalue;
		}
		public int getNyvalue() {
			return nyvalue;
		}
		public void setNyvalue(int nyvalue) {
			this.nyvalue = nyvalue;
		}
		
}
