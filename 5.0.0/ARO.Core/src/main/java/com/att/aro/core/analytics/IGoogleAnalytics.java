/**
 * 
 */
package com.att.aro.core.analytics;

/**
 * @author Harikrishna Yaramachu
 *
 */
public interface IGoogleAnalytics {
	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#applicationInfo(java.lang.String, java.lang.String)
	 */
	void applicationInfo(String analyticsTracker, String applicationName,
			String applicationVersion);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);
	
	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);
	
	void sendExceptionEvents(String exceptionDesc, String source, boolean isFatal);
	
    /* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#sendAnalyticsEvents(java.lang.String, java.lang.String)
	 */
//	@Override
	void sendCrashEvents(String crashDesc, String source);
	
	void close();
	
}
