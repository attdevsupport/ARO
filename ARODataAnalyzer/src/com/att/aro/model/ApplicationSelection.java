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

/**
 * Contains methods for managing the configuration of how applications are displayed 
 * in the TCP Flows table.
 */
public class ApplicationSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private String appName;
	private boolean selected = true;
	private Color color = Color.GRAY;

	/**
	 * Initializes an instance of the ApplicationSelection class, using the specified application name.
	 * 
	 * @param appName – The application name.
	 */
	public ApplicationSelection(String appName) {
		this.appName = appName;
	}

	/**
	 * Initializes an instance of the ApplicationSelection class, using another instance 
	 * of the ApplicationSelection class.
	 * 
	 * @param app – An ApplicationSelection object.
	 */
	public ApplicationSelection(ApplicationSelection app) {
		this.appName = app.appName;
		this.selected = app.selected;
		this.color = app.color;
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

}
