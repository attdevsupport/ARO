package com.att.aro.core.pojo;

import org.springframework.beans.factory.annotation.Value;


public class VersionInfo {
	
	@Value("${build.majorversion}")
	private String versionNumber;
	
	@Value("${build.timestamp}")
	private String buildTimestamp;

	public String getName(){
		return "Application Resource Optimizer";
	}
	public String getVersion(){
		return versionNumber;
	}
	
	public String getBuildTimestamp(){
		return buildTimestamp;
	}
}
