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
package com.att.aro.core.securedpacketreader.impl;

import java.util.List;

import com.att.aro.core.securedpacketreader.IStorageRangeMapper;
import com.att.aro.core.securedpacketreader.pojo.StorageRange;

public class StorageRangeMapperImpl implements IStorageRangeMapper {

	@Override
	public void map(List<StorageRange> datab, int ofsBegin, int ofsEnd,
			Integer[] mappedOfsBegin, Integer[] mappedOfsEnd) {
		int pvalue = -1;
		pvalue = findSRM(datab, ofsBegin, 0, datab.size() - 1);
		mappedOfsBegin[0] = datab.get(pvalue).getNxvalue();

		pvalue = findSRM(datab, ofsEnd, 0, datab.size() - 1);
		mappedOfsEnd[0] = datab.get(pvalue).getNyvalue();
		
	}

	@Override
	public int findSRM(List<StorageRange> datab, int what, int nFrom, int nTo) {
		if (nTo <= (nFrom + 5)) {
			for (int i=nFrom; i<=nTo; i++) {
				if ((datab.get(i).getXvalue() <= what) && (what <= datab.get(i).getYvalue())) {
					return i;
				}
			}
			return -1;
		} else {
			int jvalue = (nFrom + nTo) / 2;
			if ((datab.get(jvalue).getXvalue() <= what) && (what <= datab.get(jvalue).getYvalue())) {
				return jvalue;
			} else if (what < datab.get(jvalue).getXvalue()) {
				return findSRM(datab, what, nFrom, jvalue-1);
			} else {
				return findSRM(datab, what, jvalue+1, nTo);
			}
		}
	}

}
