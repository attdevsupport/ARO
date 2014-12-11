package com.att.aro.analytics;

public interface IGoogleAnalytics {

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#applicationInfo(java.lang.String, java.lang.String)
	 */
	public abstract void applicationInfo(String analyticsTracker, String applicationName,
			String applicationVersion);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsStartSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);
	
	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable);

	/* (non-Javadoc)
	 * @see com.att.aro.analytics.IGoogleAnalytics#collectAnalyticsEvents(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract void sendAnalyticsEndSessionEvents(String eventCategory,
			String eventAction, String eventLable, String eventValue);
	
	public abstract void close();
}