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

/**
 * Encapsulates information about the Screen State of the device.
 */
public class ScreenStateInfo {

	/**
	 * The ScreenStateInfo.ScreenState Enumeration specifies constant values
	 * that describe the operational state of the device screen. This
	 * enumeration is part of the ScreenStateInfo class.
	 */
	public enum ScreenState {
		/**
		 * The Screen is in the on state.
		 */
		SCREEN_ON,
		/**
		 * The Screen is in the off state.
		 */
		SCREEN_OFF,
		/**
		 * The Screen state is unknown.
		 */
		SCREEN_UNKNOWN
	}

	// Screen Time stamp
	private double beginTimeStamp;
	private double endTimeStamp;

	// Current Screen State
	private ScreenState screenState;

	// Screen Time stamp
	private String screenBrightness;

	// Screen Time stamp
	private int screenTimeout;

	/**
	 * Initializes an instance of the ScreenStateInfo class, using the specified
	 * timestamp, ScreenState value, screen brightness, and timeout value.
	 * 
	 * @param beginTimeStamp
	 *            – The begin time stamp for the screen state .
	 * @param endTimeStamp
	 *            – The end time stamp for the screen state .
	 * @param screenState
	 *            –A ScreenState enumeration value that indicates whether the
	 *            screen is On, Off, or in an unknown state.
	 * 
	 * @param screenBrightness
	 *            –A string that describes the screen brightness.
	 * 
	 * @param screenTimeout
	 *            –The screen timeout value.
	 */
	public ScreenStateInfo(double beginTimeStamp, double endTimeStamp, ScreenState screenState,
			String screenBrightness, int screenTimeout) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.screenState = screenState;
		this.screenBrightness = screenBrightness;
		this.screenTimeout = screenTimeout;
	}

	/**
	 * Returns the start timestamp for the GPS state.
	 * 
	 * @return A double that is the GPS timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}

	/**
	 * Returns the end timestamp for the GPS state.
	 * 
	 * @return A double that is the GPS timestamp.
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
	}

	/**
	 * Returns the screen state.
	 * 
	 * @return A ScreenState enumeration value that indicates whether the screen
	 *         is On, Off, or in an unknown state.
	 */
	public ScreenState getScreenState() {
		return screenState;
	}

	/**
	 * Returns the screen brightness.
	 * 
	 * @return The screen brightness expressed as a string.
	 */
	public String getScreenBrightness() {
		return screenBrightness;
	}

	/**
	 * Returns the screen timeout.
	 * 
	 * @return The screen timeout value.
	 */
	public int getScreenTimeout() {
		return screenTimeout;
	}

}
