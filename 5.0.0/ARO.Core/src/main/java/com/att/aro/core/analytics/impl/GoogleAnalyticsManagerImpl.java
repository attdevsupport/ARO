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

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.att.aro.core.analytics.IAnalyticsManager;
import com.att.aro.core.analytics.IGoogleAnalytics;

public class GoogleAnalyticsManagerImpl implements IAnalyticsManager {
	
	private static IGoogleAnalytics analytics;
	public List<IGoogleAnalytics> analyticsPlugins;
	
	public GoogleAnalyticsManagerImpl(){
		super();
	}

	public List<IGoogleAnalytics> getAnalyticsPlugins() {
		return analyticsPlugins;
	}

	public void setAnalyticsPlugins(List<IGoogleAnalytics> analyticsPlugins) {
		this.analyticsPlugins = analyticsPlugins;
	}

	/* (non-Javadoc)
	 * @see com.att.aro.core.analytics.IAnalyticsManager#getAvailableAnalytics()
	 */
	@Override
	public IGoogleAnalytics getAvailableAnalytics(ApplicationContext context) {
		if (analytics==null){
			String[] list = context.getBeanNamesForType(IGoogleAnalytics.class);
			IGoogleAnalytics defaultAnalytics = null;
			if (list != null && list.length > 0) {
				for (String analyticsStr : list) {
					
					IGoogleAnalytics analyticsTemp = (IGoogleAnalytics)context.getBean(analyticsStr);
					if(analyticsTemp instanceof DefaultGoogleAnalyticsImpl){
						defaultAnalytics = analyticsTemp;
					} else {
						analytics = analyticsTemp;
					}
				}
				if(analytics == null){
					analytics = defaultAnalytics;
				}
	
			}
		}
		return analytics;
	}

}
