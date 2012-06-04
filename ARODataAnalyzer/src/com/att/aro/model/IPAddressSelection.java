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

/**
 * Manages the setting applied to each IP address listed in the Application/IP
 * Selection dialog.
 */
public class IPAddressSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private InetAddress ipAddress;
	private boolean selected = true;
	private Color color = Color.GRAY;

	/**
	 * Initializes an instance of the IPAddressSelection class, using the
	 * application name and IP address.
	 * 
	 * @param appName
	 *            The application name that accesses the specified IP address.
	 * @param ipAddress
	 *            The ip address for the packets corresponding to the selected
	 *            IP.
	 */
	public IPAddressSelection(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Copy constructor
	 * @param sel object to be copied
	 */
	public IPAddressSelection(IPAddressSelection sel) {
		this.ipAddress = sel.ipAddress;
		this.selected = sel.selected;
		this.color = sel.color;
	}
	
	/**
	 * Returns the IP address.
	 * 
	 * @return The IP address.
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}

	/**
	 * Retrieves the selection status of the IP address.
	 * 
	 * @return A boolean value that is true if this IP address is selected, and
	 *         false otherwise.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Registers the specified selected status with the local selected status for
	 * this IP address.
	 * 
	 * @param selected
	 *            – A boolean value that indicates the selected status to set
	 *            for this IP address.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Returns the Color associated with this IP address.
	 * 
	 * @return The Color object associated with this IP address.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Registers the specified color with the local color object for this IP
	 * address.
	 * 
	 * @param color
	 *            - The Color to set for this IP address.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

}
