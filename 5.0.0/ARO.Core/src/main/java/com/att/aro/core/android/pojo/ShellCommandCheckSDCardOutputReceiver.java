/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.android.pojo;

import java.util.ArrayList;
import java.util.List;

import com.android.ddmlib.MultiLineReceiver;

/**
 * Class to get the output from the native process and display when
 * determining SD card available space.
 * It detects both emulator and USB device
 */

public class ShellCommandCheckSDCardOutputReceiver extends MultiLineReceiver{
	// name confused...
	private boolean sdCardAttached;
	private Long sdCardMemoryAvailable;
	private String[] resultOutput = new String[0];
	private List<String> list;
	public ShellCommandCheckSDCardOutputReceiver(){
		list = new ArrayList<String>();
	}
	public String[] getResultOutput() {
		return resultOutput;
	}

	public void setResultOutput(String[] resultOutput) {
		this.resultOutput = resultOutput;
	}

	@Override
	public void processNewLines(String[] arr) {		
		for(String line: arr){
			list.add(line);
		}
		this.resultOutput = list.toArray(new String[list.size()]);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	public boolean isSDCardAttached() {
		return sdCardAttached;
	}

	public boolean isSDCardEnoughSpace(long kbs) {
		return isSDCardAttached()
				&& (sdCardMemoryAvailable == null || sdCardMemoryAvailable > kbs);//questions
	}


}
