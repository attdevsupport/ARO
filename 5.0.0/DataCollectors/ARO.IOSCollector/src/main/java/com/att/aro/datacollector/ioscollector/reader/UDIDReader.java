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
package com.att.aro.datacollector.ioscollector.reader;

import java.io.IOException;

/**
 * Execute Mac command to get Serial Number of connected iPhone/iPad/iPod.
 * Obviously, this class should be used on Mac OS only.
 *
 */
public class UDIDReader {
	ExternalProcessRunner runner = null;

	public UDIDReader() {
		runner = new ExternalProcessRunner();
	}

	public UDIDReader(ExternalProcessRunner runner) {
		this.runner = runner;
	}

	/**
	 * Get Serial Number or UDID of IOS device connected to Mac OS machine.
	 */
	public String getSerialNumber() throws IOException {
		String cmd = "system_profiler SPUSBDataType | sed -n -e '/iPad/,/Serial/p' -e '/iPhone/,/Serial/p' -e '/iPod/,/Serial/p' | grep \"Serial Number:\" | awk -F \": \" '{print $2}'";
		return runner.runCmd(new String[] { "bash", "-c", cmd });
	}
}
