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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a filter used for filtering information from a trace analysis
 * based on a specified time range and set of ApplicationSelection objects.
 */
public class AnalysisFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, ApplicationSelection> appSelections;
	private TimeRange timeRange;
	private List<TCPSession> tcpSessions; // Adding TCPSessions for getting the domain Names
	private Map<InetAddress, String> domainNames;

	/**
	 * Initializes an instance of the AnalysisFilter class using the specified trace data.
	 * @param trace 
	 * 			- A TraceData object. This parameter cannot be null.
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
	 * Initializes an instance of the AnalysisFilter class, using another AnalysisFilter object.
	 * @param filter 
	 * 			- The AnalysisFilter object to be copied to the new object. 
	 * 			  This parameter cannot be null.
	 */
	public AnalysisFilter(AnalysisFilter filter) {
 
		if(filter.getDomainNames() == null){
			//call method to set domain names map
			this.generateDomainNames(filter);
		} else{
			this.domainNames = filter.getDomainNames();
		}
		
		this.timeRange = filter.timeRange;
		this.appSelections = new HashMap<String, ApplicationSelection>(filter.appSelections.size());
		for (ApplicationSelection sel : filter.appSelections.values()) {
			if(getDomainNames() != null){ //Greg Story Add domain names map to Application Selection
				sel.setDomainNames(getDomainNames());
			}
			appSelections.put(sel.getAppName(), new ApplicationSelection(sel));
		}
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
	 * Returns a TimeRange object defining the time range setting for the filter.
	 * @return The time range
	 */
	public TimeRange getTimeRange() {
		return timeRange;
	}

	/**
	 * Sets the time range for the filter.
	 * @param timeRange - The time range to set
	 */
	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	/**
	 * Return list of TCP sessions for the filter for getting the domain names
	 * @return tcpSessions - TCPSession list
	 */
	public List<TCPSession> getTcpSessions() {
		return tcpSessions;
	}

	/**
	 * Set the TCPSession list for the filter
	 * @param TCPSession - TCPSession list
	 */
	public void setTcpSessions(List<TCPSession> tcpSessions) {
		this.tcpSessions = tcpSessions;
	}

	/**
	 * Return the Domain name map with key as inetAddress
	 * @return Map - InetAddress and Domain name map
	 */
	public Map<InetAddress, String> getDomainNames() {
		return domainNames;
	}
	
	/**
	 * Prepare domain name map from the list of TCPSession. 
	 * @param AnalysisFiletr - Which has domain names set
	 */
	private void generateDomainNames(AnalysisFilter filter){
		
		domainNames = new HashMap<InetAddress, String>();
		if(filter.getTcpSessions() != null){
			for (TCPSession tcpSession : filter.getTcpSessions()) {
				if(! domainNames.containsKey(tcpSession.getRemoteIP())){
					domainNames.put(tcpSession.getRemoteIP(), tcpSession.getDomainName());
				}
			}
		}
		
	}
	

}
