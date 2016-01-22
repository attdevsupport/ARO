/**
 * Copyright 2016 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.att.aro.core.analytics;


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
