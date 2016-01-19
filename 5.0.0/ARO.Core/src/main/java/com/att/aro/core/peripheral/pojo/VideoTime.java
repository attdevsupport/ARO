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
 * 
 * @author Borey Sao
 * Date: October 7, 2014
 */
public class VideoTime {
	private boolean exVideoFound = false;
	private boolean exVideoTimeFileNotFound = false;
	private double videoStartTime = 0.0;
	private boolean nativeVideo = false;
	public boolean isExVideoFound() {
		return exVideoFound;
	}
	public void setExVideoFound(boolean exVideoFound) {
		this.exVideoFound = exVideoFound;
	}
	public boolean isExVideoTimeFileNotFound() {
		return exVideoTimeFileNotFound;
	}
	public void setExVideoTimeFileNotFound(boolean exVideoTimeFileNotFound) {
		this.exVideoTimeFileNotFound = exVideoTimeFileNotFound;
	}
	public double getVideoStartTime() {
		return videoStartTime;
	}
	public void setVideoStartTime(double videoStartTime) {
		this.videoStartTime = videoStartTime;
	}
	public boolean isNativeVideo() {
		return nativeVideo;
	}
	public void setNativeVideo(boolean nativeVideo) {
		this.nativeVideo = nativeVideo;
	}
	
}
