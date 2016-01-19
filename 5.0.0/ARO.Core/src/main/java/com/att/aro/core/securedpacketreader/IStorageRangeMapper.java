package com.att.aro.core.securedpacketreader;

import java.util.List;
import com.att.aro.core.securedpacketreader.pojo.StorageRange;

public interface IStorageRangeMapper {
	void map(List<StorageRange> data, int ofsBegin, int ofsEnd, Integer[] mappedOfsBegin, Integer[] mappedOfsEnd);
	int findSRM(List<StorageRange> data, int what, int nFrom, int nTo);
}
