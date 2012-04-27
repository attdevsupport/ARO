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
 * Encapsulates Screen State info
 */
public class ScreenStateInfo {

	/**
	 * ENUM to maintain the screen states.
	 */
	public enum ScreenState {
		/**
		 * Screen on state.
		 */
		SCREEN_ON,
		/**
		 * Screen off state.
		 */
		SCREEN_OFF,
		/**
		 * Unknown state.
		 */
		SCREEN_UNKNOWN
	}

	// Screen Time stamp
	private double screenTimeStamp;

	// Current Screen State
	private ScreenState screenState;

	// Screen Time stamp
	private String screenBrightness;

	// Screen Time stamp
	private int screenTimeout;

	/**
	 * Constructor
	 * 
	 * @param dTimestamp
	 * @param screenState
	 */
	public ScreenStateInfo(double dTimestamp, ScreenState screenState, String screenBrightness,
			int screenTimeout) {
		this.screenTimeStamp = dTimestamp;
		this.screenState = screenState;
		this.screenBrightness = screenBrightness;
		this.screenTimeout = screenTimeout;
	}

	/**
	 * Returns screen timestamp.
	 * 
	 * @return screen timestamp.
	 */
	public double getScreenTimeStamp() {
		return screenTimeStamp;
	}

	/**
	 * Returns screen state.
	 * 
	 * @return screen state.
	 */
	public ScreenState getScreenState() {
		return screenState;
	}

	/**
	 * Returns screen brightness.
	 * 
	 * @return screen brightness.
	 */
	public String getScreenBrightness() {
		return screenBrightness;
	}

	/**
	 * Returns screen timeout.
	 * 
	 * @return screen timeout.
	 */
	public int getScreenTimeout() {
		return screenTimeout;
	}

}
