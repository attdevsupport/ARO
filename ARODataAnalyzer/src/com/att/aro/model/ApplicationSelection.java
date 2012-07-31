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

/**
 * Contains methods for managing the configuration of how applications are displayed 
 * in the TCP Flows table.
 */
public class ApplicationSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private String appName;
	private boolean selected = true;
	private Color color = Color.GRAY;
	private Map<InetAddress, IPAddressSelection> ipAddressSelection;

	/**
	 * Initializes an instance of the ApplicationSelection class, using the specified application name.
	 * 
	 * @param appName – The application name.
	 */
	public ApplicationSelection(String appName, Collection<InetAddress> ips) {
		this.appName = appName;
		this.ipAddressSelection = new HashMap<InetAddress, IPAddressSelection>();
		if (ips != null) {
			for (InetAddress ip : ips) {
				ipAddressSelection.put(ip, new IPAddressSelection(ip));
			}
		}
	}

	/**
	 * Copy constructor
	 * 
	 * @param app – An ApplicationSelection object.
	 */
	public ApplicationSelection(ApplicationSelection app) {
		this.appName = app.appName;
		this.selected = app.selected;
		this.color = app.color;
		this.ipAddressSelection = new HashMap<InetAddress, IPAddressSelection>(app.ipAddressSelection.size());
		for (IPAddressSelection sel : app.ipAddressSelection.values()) {
			ipAddressSelection.put(sel.getIpAddress(), new IPAddressSelection(sel));
		}
	}

	/**
	 * Returns the application name. 
	 * 
	 * @return A string that is the application name.
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Retrieves the selection status of the application.
	 * 
	 * @return A boolean value that is true if this application is selected, and  false otherwise.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Registers the specifed selected status with the local selected status for this application.
	 * 
	 * @param selected – A boolean value that indicates the selected status to set for this 
	 * application.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Returns the Color associated with this application. 
	 * 
	 * @return The Color object associated with this application.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Registers the specified color with the local color object for this application.
	 * 
	 * @param color - The Color to set for this application.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the IP address selection settings for the specified IP address accessed
	 * by this app
	 * @param ip The IP address for which selection info is requested
	 * @return The IP selection info for the IP or null if no setting available
	 */
	public IPAddressSelection getIPAddressSelection(InetAddress ip) {
		return ipAddressSelection.get(ip);
	}

	/**
	 * Returns all IP address selections for this application.
	 * @return The collection of ip address selections.
	 */
	public Collection<IPAddressSelection> getIPAddressSelections() {
		return ipAddressSelection.values();
	}

}
