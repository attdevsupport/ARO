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
package com.att.aro.datacollector.ioscollector.utilities;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.reader.ExternalProcessRunner;

public class XCodeInfo {

	ExternalProcessRunner runner = null;

	private ILogger log = new LoggerImpl("IOSCollector");

	@Autowired
	public void setLogger(ILogger logger) {
		this.log = logger;
	}

	public XCodeInfo() {
		runner = new ExternalProcessRunner();
	}

	public XCodeInfo(ExternalProcessRunner runner) {
		this.runner = runner;
	}

	/**
	 * Find out if component rvictl is available. This component come with XCode
	 * 4.2 and above.
	 * 
	 * @return true or false
	 */
	public boolean isRVIAvailable() {
		boolean yes = false;
		String[] cmd = new String[] { "bash", "-c", "which rvictl" };
		String result = "";
		try {
			result = runner.runCmd(cmd);

		} catch (IOException e) {
			log.debug("IOException:", e);
		}
		if (result.length() > 1) {
			yes = true;
		}
		return yes;
	}

	/**
	 * Find whether xcode is installed or not.
	 * 
	 * @return
	 */
	public boolean isXcodeAvailable() {
		boolean flag = false;
		String[] cmd = new String[] { "bash", "-c", "which xcodebuild" };
		String xCode = "";
		try {
			xCode = runner.runCmd(cmd);
			log.info("xCode Installation Dir : " + xCode);
		} catch (IOException ioE) {
			log.debug("IOException:", ioE);
		}
		if (xCode.length() > 1) {
			flag = true;
		}
		return flag;
	}

	/**
	 * ARO is supporting version 5 and above. This is method will check for
	 * supported version.
	 * 
	 * @return
	 */
	public boolean isXcodeSupportedVersionInstalled() {
		boolean supportedVersionFlag = false;
		String[] cmd = new String[] { "bash", "-c", "xcodebuild -version" };
		String xCodeVersion = "";
		try {
			xCodeVersion = runner.runCmd(cmd);
			log.info("xCode Version : " + xCodeVersion);
		} catch (IOException ex) {
			log.debug("IOException:", ex);
		}
		if (xCodeVersion.length() > 0) {
			String[] version = xCodeVersion.split("\\r?\\n");
			String xCode = version[0];
			String versionOfxCode = xCode.substring(xCode.indexOf(" "));
			log.info(" Version Code : " + versionOfxCode);
			int versionNumber = 0;
			try {
				versionNumber = Integer.parseInt(versionOfxCode.substring(0, versionOfxCode.indexOf(".")).trim());
				log.info(" Version Number : " + versionNumber);
			} catch (NumberFormatException e) {
				log.debug("NumberFormatException:", e);
			}
			if (versionNumber >= 5) {
				supportedVersionFlag = true;
			}

		}
		return supportedVersionFlag;
	}
}//end class

