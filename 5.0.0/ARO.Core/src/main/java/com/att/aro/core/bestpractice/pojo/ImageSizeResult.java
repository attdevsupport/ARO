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

import java.util.List;


public class ImageSizeResult extends AbstractBestPracticeResult {
	private List<ImageSizeEntry> results = null;
	private int deviceScreenSizeRangeX = 0;
	private int deviceScreenSizeRangeY = 0;
	private String exportNumberOfLargeImages;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.IMAGE_SIZE;
	}

	public List<ImageSizeEntry> getResults() {
		return results;
	}

	public void setResults(List<ImageSizeEntry> results) {
		this.results = results;
	}
	public int getDeviceScreenSizeRangeX() {
		return deviceScreenSizeRangeX;
	}

	public void setDeviceScreenSizeRangeX(int deviceScreenSizeRangeX) {
		this.deviceScreenSizeRangeX = deviceScreenSizeRangeX;
	}

	public int getDeviceScreenSizeRangeY() {
		return deviceScreenSizeRangeY;
	}

	public void setDeviceScreenSizeRangeY(int deviceScreenSizeRangeY) {
		this.deviceScreenSizeRangeY = deviceScreenSizeRangeY;
	}

	public String getExportNumberOfLargeImages() {
		return exportNumberOfLargeImages;
	}

	public void setExportNumberOfLargeImages(String exportNumberOfLargeImages) {
		this.exportNumberOfLargeImages = exportNumberOfLargeImages;
	}
	
}
