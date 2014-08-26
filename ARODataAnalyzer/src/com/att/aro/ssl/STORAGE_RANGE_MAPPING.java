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

import java.util.List;

public class STORAGE_RANGE_MAPPING {
	//a mapping from [x,y] to [nx, ny]
		public int x;
		public int y;

		public int nx;
		public int ny;

		static void Map(List<STORAGE_RANGE_MAPPING> db,
			int ofsBegin, int ofsEnd, Integer[] mappedOfsBegin, Integer[] mappedOfsEnd) {
			
			int p = -1;
			p = SRMFind(db, ofsBegin, 0, db.size() - 1);
			mappedOfsBegin[0] = db.get(p).nx;

			p = SRMFind(db, ofsEnd, 0, db.size() - 1);
			mappedOfsEnd[0] = db.get(p).ny;
		}
		
		static int SRMFind(List<STORAGE_RANGE_MAPPING> db, int what, int nFrom, int nTo) {
			if (nTo <= (nFrom + 5)) {
				for (int i=nFrom; i<=nTo; i++) {
					if ((db.get(i).x <= what) && (what <= db.get(i).y)) {
						return i;
					}
				}
				return -1;
			} else {
				int j = (nFrom + nTo) / 2;
				if ((db.get(j).x <= what) && (what <= db.get(j).y)) {
					return j;
				} else if (what < db.get(j).x) {
					return SRMFind(db, what, nFrom, j-1);
				} else {
					return SRMFind(db, what, j+1, nTo);
				}
			}
		}
}
