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

import java.io.Serializable;

/**
 * Encapsulates data from user event
 */
public class UserEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ENUM to maintain the different user types.
	 */
	public enum UserEventType {
		/**
		 * Unknown event.
		 */
		EVENT_UNKNOWN,
		/**
		 * Screen touched state.
		 */
		SCREEN_TOUCH,
		/**
		 * Power key pressed state.
		 */
		KEY_POWER,
		/**
		 * Volume up key pressed state.
		 */
		KEY_VOLUP,
		/**
		 * Volume down key pressed state.
		 */
		KEY_VOLDOWN,
		/**
		 * Track ball moved state.
		 */
		KEY_BALL,
		/**
		 * Home key pressed state.
		 */
		KEY_HOME,
		/**
		 * Menu key pressed state.
		 */
		KEY_MENU,
		/**
		 * Back key pressed state.
		 */
		KEY_BACK,
		/**
		 * Search key pressed state.
		 */
		KEY_SEARCH,
		/**
		 * Call Accept/Green key pressed state.
		 */
		KEY_GREEN,
		/**
		 * Call Reject/Red key pressed state.
		 */
		KEY_RED,
		/**
		 * Generic key press
		 */
		KEY_KEY,
		/**
		 * Landscape screen orientation
		 */
		SCREEN_LANDSCAPE,
		/**
		 * Portrait screen orientation
		 */
		SCREEN_PORTRAIT
	}

	private UserEventType eventType;
	private double pressTime;
	private double releaseTime;

	/**
	 * Constructor
	 * 
	 * @param eventType
	 * @param pressTime
	 * @param releaseTime
	 */
	public UserEvent(UserEventType eventType, double pressTime, double releaseTime) {
		this.eventType = eventType;
		this.pressTime = pressTime;
		this.releaseTime = releaseTime;
	}

	/**
	 * Returns type of user event.
	 * 
	 * @return eventType.
	 */
	public UserEventType getEventType() {
		return eventType;
	}

	/**
	 * Returns time of press event.
	 * 
	 * @return pressTime.
	 */
	public double getPressTime() {
		return pressTime;
	}

	/**
	 * Returns time of release event.
	 * 
	 * @return releaseTime.
	 */
	public double getReleaseTime() {
		return releaseTime;
	}

	/**
	 * Sets time of press event.
	 * 
	 * @param dPressTime
	 * 
	 */
	public void setPressTime(double dPressTime) {
		pressTime = dPressTime;
	}

	/**
	 * Sets time of release event.
	 * 
	 * @param dReleaseTime
	 * 
	 */
	public void setReleaseTime(double dReleaseTime) {
		releaseTime = dReleaseTime;
	}

}
