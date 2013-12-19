package com.att.aro.plugin;

import com.att.aro.main.ApplicationResourceOptimizer;

/**
 * Interface to be implemented by target class of ARO_RTC_Adapter for automation of RTC export. 
 */
public interface ResultExportPlugin {
	boolean exportTestResultToRTC(ApplicationResourceOptimizer aro);
}
