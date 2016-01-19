/**
 * 
 */
package com.att.aro.core.analytics;

import org.springframework.context.ApplicationContext;

/**
 * @author Harikrishna Yaramachu
 *
 */
public interface IAnalyticsManager {

	IGoogleAnalytics getAvailableAnalytics(ApplicationContext context);
}
