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
 * Encapsulates camera information.
 * 
 */
public class CameraInfo {

	/**
	 * ENUM to maintain the Camera states.
	 */
	public enum CameraState {
		/**
		 * Camera on state.
		 */
		CAMERA_ON,
		/**
		 * Camera off state.
		 */
		CAMERA_OFF,
		/**
		 * Unknown statue.
		 */
		CAMERA_UNKNOWN
	}

	// Camera Time stamp
	private double cameraTimeStamp;
	// Current Camera State
	private CameraState cameraState;

	/**
	 * Initializes an instance of the CameraInfo class using the specified timestamp, and 
	 * camera state.
	 * 
	 * @param dTimestamp – A double that is the timestamp for the camera.
	 * 
	 * @param cameraState – A CameraState enumeration value that indicates the state of 
	 * the camera.
	 */
	public CameraInfo(double dTimestamp, CameraState cameraState) {
		this.cameraTimeStamp = dTimestamp;
		this.cameraState = cameraState;
	}

	/**
	 * Returns the camera timestamp. 
	 * 
	 * @return A double that is the timestamp for the camera.
	 */
	public double getCameraTimeStamp() {
		return cameraTimeStamp;
	}

	/**
	 * Returns the camera state. 
	 * 
	 * @return A CameraState enumeration value that indicates the state of the camera.
	 */
	public CameraState getCameraState() {
		return cameraState;
	}

}
