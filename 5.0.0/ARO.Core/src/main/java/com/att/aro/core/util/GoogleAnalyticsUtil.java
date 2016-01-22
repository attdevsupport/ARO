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

package com.att.aro.core.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.core.AROConfig;
import com.att.aro.core.analytics.AnalyticsEvents;
import com.att.aro.core.analytics.IAnalyticsManager;
import com.att.aro.core.analytics.IGoogleAnalytics;
import com.att.aro.core.settings.IAROSettings;

public class GoogleAnalyticsUtil {

	private static GoogleAnalyticsUtil gauInstance = null;
	
    ApplicationContext configContext = new AnnotationConfigApplicationContext(AROConfig.class);
	
    private static IGoogleAnalytics sendAnalytics;
	
    private static AnalyticsEvents  allEvents;
    
    private static IAROSettings aroConfigSetting;
		
	private GoogleAnalyticsUtil(){
		sendAnalytics = getAvailableAnalyticsImplementor();
		
	}
	
	private IGoogleAnalytics getAvailableAnalyticsImplementor() {
		IAnalyticsManager colmg = configContext.getBean(IAnalyticsManager.class);
		return colmg.getAvailableAnalytics(configContext);
	}
	
	public static IGoogleAnalytics getGoogleAnalyticsInstance(){
		if(gauInstance == null){
			gauInstance = new GoogleAnalyticsUtil();
		}
		return sendAnalytics;
	}
	
	public static AnalyticsEvents getAnalyticsEvents(){
		if(allEvents == null){
			//allEvents = new AnalyticsEvents();
			if(gauInstance == null){
				gauInstance = new GoogleAnalyticsUtil();
			}
			allEvents = gauInstance.configContext.getBean(AnalyticsEvents.class);
		}
		return allEvents;
	}
	
	public static IAROSettings getConfigSetting(){
		if(aroConfigSetting == null){
			if(gauInstance == null){
				gauInstance = new GoogleAnalyticsUtil();
			}
			aroConfigSetting = gauInstance.configContext.getBean(IAROSettings.class);
		}
		return aroConfigSetting;
	}
	

}
