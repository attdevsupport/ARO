package com.att.aro.analytics;

import java.util.logging.Logger;

public class DefaultPlatformImpl implements IPlatform {
	private static final Logger LOGGER = Logger.getLogger(DefaultPlatformImpl.class.getName());
	
	@Override
	public void saveOSName(String osname) {
		LOGGER.info("default impl for IPlatform, OSName: "+osname);
	}

	@Override
	public void saveJavaVersion(String version) {
		LOGGER.info("default impl for IPlatform, JavaVersion: "+version);
	}

}
