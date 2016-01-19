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
