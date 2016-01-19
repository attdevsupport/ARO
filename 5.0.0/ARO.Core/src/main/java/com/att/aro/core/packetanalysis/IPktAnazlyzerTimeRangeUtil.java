package com.att.aro.core.packetanalysis;

import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;


public interface IPktAnazlyzerTimeRangeUtil {

	AbstractTraceResult getTimeRangeResult(TraceDirectoryResult result,TimeRange timeRange);
	
}