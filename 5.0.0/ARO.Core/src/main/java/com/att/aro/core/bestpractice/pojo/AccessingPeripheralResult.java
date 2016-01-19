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
package com.att.aro.core.bestpractice.pojo;

public class AccessingPeripheralResult extends AbstractBestPracticeResult {
	private double activeGPSRatio = 0.0;
	private double activeGPSDuration = 0.0;
	private double activeBluetoothRatio = 0.0;
	private double activeBluetoothDuration = 0.0;
	private double activeCameraRatio = 0.0;
	private double activeCameraDuration = 0.0;
	
	private String exportAllGPSDesc;
	private String exportAllBTDesc;
	private String exportAllCamDesc;
	
	public double getActiveGPSRatio() {
		return activeGPSRatio;
	}

	public void setActiveGPSRatio(double activeGPSRatio) {
		this.activeGPSRatio = activeGPSRatio;
	}

	public double getActiveBluetoothRatio() {
		return activeBluetoothRatio;
	}

	public void setActiveBluetoothRatio(double activeBluetoothRatio) {
		this.activeBluetoothRatio = activeBluetoothRatio;
	}

	public double getActiveCameraRatio() {
		return activeCameraRatio;
	}

	public void setActiveCameraRatio(double activeCameraRatio) {
		this.activeCameraRatio = activeCameraRatio;
	}
	
	public double getActiveGPSDuration() {
		return activeGPSDuration;
	}

	public void setActiveGPSDuration(double activeGPSDuration) {
		this.activeGPSDuration = activeGPSDuration;
	}

	public double getActiveBluetoothDuration() {
		return activeBluetoothDuration;
	}

	public void setActiveBluetoothDuration(double activeBluetoothDuration) {
		this.activeBluetoothDuration = activeBluetoothDuration;
	}

	public double getActiveCameraDuration() {
		return activeCameraDuration;
	}

	public void setActiveCameraDuration(double activeCameraDuration) {
		this.activeCameraDuration = activeCameraDuration;
	}

	
	public String getExportAllGPSDesc() {
		return exportAllGPSDesc;
	}

	public void setExportAllGPSDesc(String exportAllGPSDesc) {
		this.exportAllGPSDesc = exportAllGPSDesc;
	}

	public String getExportAllBTDesc() {
		return exportAllBTDesc;
	}

	public void setExportAllBTDesc(String exportAllBTDesc) {
		this.exportAllBTDesc = exportAllBTDesc;
	}

	public String getExportAllCamDesc() {
		return exportAllCamDesc;
	}

	public void setExportAllCamDesc(String exportAllCamDesc) {
		this.exportAllCamDesc = exportAllCamDesc;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.ACCESSING_PERIPHERALS;
	}

}
