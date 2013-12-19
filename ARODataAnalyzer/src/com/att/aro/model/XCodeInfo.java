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

import java.io.IOException;

public class XCodeInfo {
	ExternalProcessRunner runner = null;
	public XCodeInfo(){
		runner = new ExternalProcessRunner();
	}
	public XCodeInfo(ExternalProcessRunner runner){
		this.runner = runner;
	}
	/**
	 * Find out if component rvictl is available. This component come with XCode 4.2 and above.
	 * @return true or false
	 */
	public boolean isRVIAvailable(){
		boolean yes = false;
		String[] cmd = new String[]{"bash","-c","which rvictl"};
		String result = "";
		try {
			result = runner.runCmd(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(result.length() > 1){
			yes = true;
		}
		return yes;
	}
}//end class
