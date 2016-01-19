/**
 * 
 */
package com.att.aro.core.analytics.impl;

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.att.aro.core.analytics.IAnalyticsManager;
import com.att.aro.core.analytics.IGoogleAnalytics;

/**
 * @author Harikrishna Yaramachu
 *
 */
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
