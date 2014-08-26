/**
 * 
 */
package com.att.aro.analytics;

import java.util.logging.Logger;

/**
 * @author hy0910
 *
 */
public class DefaultGoogleAnalyticsImpl implements IGoogleAnalytics{
	
	private static final Logger LOGGER = Logger.getLogger(DefaultGoogleAnalyticsImpl.class.getName());

	@Override
	public void applicationInfo(String gaTracker, String applicationName,
			String applicationVersion) {
		// TODO Auto-generated method stub
		LOGGER.info("Default impl of Application Info Tracker ID, Application Name, Application Version "+gaTracker + " " +applicationName + " "+applicationVersion);
		
	}

	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction) {
		// TODO Auto-generated method stub
		LOGGER.info("Default impl Analytics Events event Category, eventAction "+eventCategory + " " +eventAction);
	}

	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction,
			String eventLable) {
		// TODO Auto-generated method stub
		LOGGER.info("Default impl Analytics Events event Category, eventAction, event lable "+eventCategory + ", " +eventAction +", " +eventLable );
	}

	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction,
			String eventLable, String eventValue) {
		// TODO Auto-generated method stub
		LOGGER.info("Default impl Analytics Events event Category, eventAction, event lable, event value "+eventCategory + ", " +eventAction +", " +eventLable + ", "+eventValue);
	}
	
	

}
