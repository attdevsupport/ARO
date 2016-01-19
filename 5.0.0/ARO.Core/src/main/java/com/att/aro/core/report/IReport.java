package com.att.aro.core.report;

import com.att.aro.core.pojo.AROTraceData;

public interface IReport {

	boolean reportGenerator(String resultFilePath,AROTraceData results);
	
}
