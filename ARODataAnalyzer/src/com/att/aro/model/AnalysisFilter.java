/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.awt.Color;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Used to filter specific information in a trace from the analysis
 */
public class AnalysisFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, ApplicationSelection> appSelections;
	private TimeRange timeRange;

	/**
	 * Constructs default analysis filter for specified trace
	 * @param trace the trace.  Cannot be null
	 */
	public AnalysisFilter(TraceData trace) {
		
		// Create default application selections
		Collection<String> appNames = trace.getAllAppNames();
		this.appSelections = new HashMap<String, ApplicationSelection>(appNames.size());
		Map<String, Set<InetAddress>> m = trace.getAppIps();
		for (String app : appNames) {
			appSelections.put(app, new ApplicationSelection(app, m.get(app)));
		}
	}

	/**
	 * Copy constructor
	 * @param filter filter to be copied.  Cannot be null
	 */
	public AnalysisFilter(AnalysisFilter filter) {
		this.timeRange = filter.timeRange;
		this.appSelections = new HashMap<String, ApplicationSelection>(filter.appSelections.size());
		for (ApplicationSelection sel : filter.appSelections.values()) {
			appSelections.put(sel.getAppName(), new ApplicationSelection(sel));
		}
	}
	
	/**
	 * Gets the application selection settings for this filter
	 * @return The collection of application selctions.
	 */
	public Collection<ApplicationSelection> getApplicationSelections() {
		return appSelections.values();
	}

	/**
	 * Gets the application selection setting for the specified app
	 * @param appName The name of the application
	 * @return The application selection setting or null if app not found
	 */
	public ApplicationSelection getApplicationSelection(String appName) {
		return appSelections.get(appName);
	}

	/**
	 * Based upon the filter returns the color that should be used to
	 * display the specified packet
	 * @param packet The packet.
	 * @return The color that is appplied to the specified packet.
	 */
	public Color getPacketColor(PacketInfo packet) {

		// Check to see if application is selected
		ApplicationSelection appSel = getApplicationSelection(packet.getAppName());
		if (appSel != null) {
			
			// IP address may be selected
			IPAddressSelection ipSel = appSel.getIPAddressSelection(packet.getRemoteIPAddress());
			if (ipSel != null && ipSel.isSelected()) {
				return ipSel.getColor();
			}
			if (appSel.isSelected()) {
				return appSel.getColor();
			}
		}
		return null;
		
	}
	
	/**
	 * @return the timeRange
	 */
	public TimeRange getTimeRange() {
		return timeRange;
	}

	/**
	 * @param timeRange the timeRange to set
	 */
	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

}
