/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.peripheral.pojo;

import java.io.Serializable;

/**
 * Encapsulates the data from a user generated event. 
 */
public class UserEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private UserEventType eventType;
	private double pressTime;
	private double releaseTime;

	
	/**
	 * The UserEvent.UserEventType Enumeration specifies constant values that describe 
	 * different types of user generated events. This enumeration is part of the UserEvent 
	 * class.
	 */
	public enum UserEventType {
		/**
		 * The event is of an Unknown type. 
		 */
		EVENT_UNKNOWN,
		/**
		 * The Screen was touched.
		 */
		SCREEN_TOUCH,
		/**
		 * The Power key was pressed. 
		 */
		KEY_POWER,
		/**
		 * Volume Up key was pressed. 
		 */
		KEY_VOLUP,
		/**
		 * The Volume Down key was pressed. 
		 */
		KEY_VOLDOWN,
		/**
		 * The Track Ball was moved. 
		 */
		KEY_BALL,
		/**
		 * The Home key was pressed. 
		 */
		KEY_HOME,
		/**
		 * The Menu key was pressed. 
		 */
		KEY_MENU,
		/**
		 * The Back key was pressed. 
		 */
		KEY_BACK,
		/**
		 * The Search key was pressed. 
		 */
		KEY_SEARCH,
		/**
		 * The Call Accept/Green key was pressed. 
		 */
		KEY_GREEN,
		/**
		 * The Call Reject/Red key was pressed. 
		 */
		KEY_RED,
		/**
		 * A generic key press.
		 */
		KEY_KEY,
		/**
		 * The screen orientation was changed to Landscape.
		 */
		SCREEN_LANDSCAPE,
		/**
		 * The screen orientation was changed to Portrait.
		 */
		SCREEN_PORTRAIT
	}

	/**
	 * Initializes an instance of the UserEvent class, using the specified event type, 
	 * press time, and release time.
	 * 
	 * @param eventType The event type. One of the values of the UserEventType enumeration.
	 * @param pressTime The time at which the event was initiated (such as a key being pressed down).
	 * @param releaseTime The time at which the event was ended (such as a key being released).
	 */
	public UserEvent(UserEventType eventType, double pressTime, double releaseTime) {
		this.eventType = eventType;
		this.pressTime = pressTime;
		this.releaseTime = releaseTime;
	}

	/**
	 * Returns the type of user event. 
	 * 
	 * @return The event type. One of the values of the UserEventType enumeration.
	 */
	public UserEventType getEventType() {
		return eventType;
	}

	/**
	 * Returns the time at which the event was initiated (such as a key being pressed down).
	 * 
	 * @return The press time.
	 */
	public double getPressTime() {
		return pressTime;
	}

	/**
	 * Returns time at which the event was ended (such as a key being released). 
	 * 
	 * @return The release time.
	 */
	public double getReleaseTime() {
		return releaseTime;
	}

	/**
	 * Sets the press time of the event. 
	 * 
	 * @param dPressTime The time at which the event was initiated.
	 * 
	 */
	public void setPressTime(double dPressTime) {
		pressTime = dPressTime;
	}

	/**
	 * Sets the release time of therelease event. 
	 * 
	 * @param dReleaseTime The time at which the event was ended.
	 * 
	 */
	public void setReleaseTime(double dReleaseTime) {
		releaseTime = dReleaseTime;
	}

}
