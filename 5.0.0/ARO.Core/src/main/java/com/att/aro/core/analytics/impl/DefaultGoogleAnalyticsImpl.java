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

package com.att.aro.core.analytics.impl;

import com.att.aro.core.analytics.IGoogleAnalytics;

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
