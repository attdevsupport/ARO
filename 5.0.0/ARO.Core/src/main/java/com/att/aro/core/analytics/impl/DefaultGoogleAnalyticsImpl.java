/**
 * 
 */
package com.att.aro.core.analytics.impl;

import com.att.aro.core.analytics.IGoogleAnalytics;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class DefaultGoogleAnalyticsImpl implements IGoogleAnalytics {

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#applicationInfo(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void applicationInfo(String analyticsTracker,
			String applicationName, String applicationVersion) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction,
			String eventLable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEvents(String eventCategory, String eventAction,
			String eventLable, String eventValue) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsStartSessionEvents(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsStartSessionEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsStartSessionEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEndSessionEvents(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEndSessionEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#sendAnalyticsEndSessionEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IGoogleAnalytics#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendExceptionEvents(String exceptionDesc, String source,
			boolean isFatal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendCrashEvents(String crashDesc, String source) {
		// TODO Auto-generated method stub
		
	}

}
