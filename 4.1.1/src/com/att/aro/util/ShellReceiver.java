/*
 * Copyright 2012 AT&T
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

package com.att.aro.util;

import com.android.ddmlib.MultiLineReceiver;

/**
 * Monitor output from an AndroidDevice.executeShellCommand
 * 
 * @author bn153x@att.com
 *
 */
public class ShellReceiver extends MultiLineReceiver {
	
	private String compareText = null;
	
	public String getCompareText() {
		return compareText;
	}

	public void setCompareText(String compareText) {
		this.compareText = compareText;
	}

	private String responseString = null;
	private String[] responseStrings = null;
	
	/**
	 * 
	 * @return a response line that has a match with compareText
	 */
	public String getResponseString() {
		return responseString;
	}
	
	/**
	 * 
	 * @return all response lines
	 */
	public String[] getResponseStrings() {
		return responseStrings;
	}

	/**
	 * Instantiate with a default to return the first line of response
	 */
	public ShellReceiver() {
		compareText = null;
	}
	
	/**
	 * Instantiate with a string to match on 
	 * @param string
	 */
	public ShellReceiver(String string) {
		compareText = string;
	}

	@Override
	public void processNewLines(String[] lines) {
		
		responseStrings = lines;
		for (String line : lines) {
			if (compareText == null){
				this.responseString  = line;
			} else if (compareText != null && line.contains(compareText)){
				this.responseString  = line;
			}
		}
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}
};
