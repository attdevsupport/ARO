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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Customized Exception to handle profile exception. 
 */
public class ProfileException extends Exception {
	private static final long serialVersionUID = 1L;

	private Map<String, String> errorLog;

	/**
	 * 
	 * @param errorLog
	 * @return
	 */
	private static String createMessage(Map<String, String> errorLog) {
		StringBuffer message = new StringBuffer();
		for (Map.Entry<String, String> entry : errorLog.entrySet()) {
			message.append(entry.getKey());
			message.append("=");
			message.append(entry.getValue());
			message.append("; ");
		}
		return message.toString();
	}

	/**
	 * Constructor
	 * 
	 * @param errorLog
	 */
	public ProfileException(Map<String, String> errorLog) {
		super(createMessage(errorLog));
		this.errorLog = new HashMap<String, String>(errorLog);
	}

	/**
	 * @return the errorLog
	 */
	public Map<String, String> getErrorLog() {
		return Collections.unmodifiableMap(errorLog);
	}

}// end ProfileException Class
