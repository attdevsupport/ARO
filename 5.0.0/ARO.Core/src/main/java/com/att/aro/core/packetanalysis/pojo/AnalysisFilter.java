/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.packetanalysis.pojo;

import java.awt.Color;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a filter used for filtering information from a trace analysis
 * based on a specified time range and set of ApplicationSelection objects.
 */

public class AnalysisFilter implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Map<String, ApplicationSelection> appSelections;
	private TimeRange timeRange;
	private Map<InetAddress, String> domainNames;
	
	private boolean ipv4Sel = true;
	private boolean ipv6Sel = true;
	private boolean udpSel = true; 
	
	
	public AnalysisFilter(Map<String, ApplicationSelection> appSelections, TimeRange timeRange, Map<InetAddress, String> domainNames){
		this.appSelections = appSelections;
		this.timeRange = timeRange;
		this.domainNames = domainNames;
	}
	
	public Map<String, ApplicationSelection> getAppSelections() {
		return appSelections;
	}
	public void setAppSelections(Map<String, ApplicationSelection> appSelections) {
		this.appSelections = appSelections;
	}
	public TimeRange getTimeRange() {
		return timeRange;
	}
	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}
	public Map<InetAddress, String> getDomainNames() {
		return domainNames;
	}
	public void setDomainNames(Map<InetAddress, String> domainNames) {
		this.domainNames = domainNames;
	}
	
	/**
	 * Returns all of the ApplicationSelection objects for this filter.
	 * @return A collection of ApplicationSelection objects.
	 */
	public Collection<ApplicationSelection> getApplicationSelections() {
		return appSelections.values();
	}

	/**
	 * Returns the ApplicationSelection containing selection settings for the specified application name.
	 * @param appName - The application name.
	 * @return The ApplicationSelection object or null if the application name is not found.
	 */
	public ApplicationSelection getApplicationSelection(String appName) {
		return appSelections.get(appName);
	}

	/**
	 * Returns the color used to display the specified packet based on 
	 * the settings in this filter.
	 * @param packet - A PacketInfo object that specifies the packet.
	 * @return The color used to display the specified packet.
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
	 * @return the ipv4Sel
	 */
	public boolean isIpv4Sel() {
		return ipv4Sel;
	}

	/**
	 * @param ipv4Sel the ipv4Sel to set
	 */
	public void setIpv4Sel(boolean ipv4Sel) {
		this.ipv4Sel = ipv4Sel;
	}

	/**
	 * @return the ipv6Sel
	 */
	public boolean isIpv6Sel() {
		return ipv6Sel;
	}

	/**
	 * @param ipv6Sel the ipv6Sel to set
	 */
	public void setIpv6Sel(boolean ipv6Sel) {
		this.ipv6Sel = ipv6Sel;
	}

	/**
	 * @return the udpSel
	 */
	public boolean isUdpSel() {
		return udpSel;
	}

	/**
	 * @param udpSel the udpSel to set
	 */
	public void setUdpSel(boolean udpSel) {
		this.udpSel = udpSel;
	}
	
}
