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

/**
 * Encapsulates camera information.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 1, 2014
 */
public class CameraInfo {

	// Camera Time stamp
	private double beginTimeStamp;
	private double endTimeStamp;
	// Current Camera State
	private CameraState cameraState;
	
	/**
	 * The CameraInfo.CameraState Enumeration specifies constant values that
	 * describe the operational state of the camera on a device. This
	 * enumeration is part of the CameraInfo class.
	 */
	public enum CameraState {
		/**
		 * The camera is in the on state.
		 */
		CAMERA_ON,
		/**
		 * The camera is in the off state.
		 */
		CAMERA_OFF,
		/**
		 * The camera is in an unknown state.
		 */
		CAMERA_UNKNOWN
	}


	/**
	 * Initializes an instance of the CameraInfo class using the specified
	 * timestamp, and camera state.
	 * 
	 * @param beginTimeStamp
	 *            The starting timestamp for the camera state.
	 * @param endTimeStamp
	 *            The ending timestamp for the camera state.
	 * @param cameraState
	 *            A CameraState enumeration value that indicates the state of
	 *            the camera.
	 */
	public CameraInfo(double beginTimeStamp, double endTimeStamp,
			CameraState cameraState) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.cameraState = cameraState;
	}

	/**
	 * Returns the starting timestamp for the Camera state.
	 * 
	 * @return The starting timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}

	/**
	 * Returns the ending timestamp for the Camera state.
	 * 
	 * @return The ending timestamp..
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
	}

	/**
	 * Returns the camera state.
	 * 
	 * @return A CameraState enumeration value that indicates the state of the
	 *         camera.
	 */
	public CameraState getCameraState() {
		return cameraState;
	}

}
