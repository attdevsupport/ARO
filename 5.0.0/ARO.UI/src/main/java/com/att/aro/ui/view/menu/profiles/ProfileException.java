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
package com.att.aro.ui.view.menu.profiles;

import java.util.Map;

import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * A customized Exception that should be raised when an exception occurs while handling 
 * device profile information. 
 */
public class ProfileException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param errorLog
	 * @return
	 */
	private static String createMessage(Map<String, String> errorLog) {
		StringBuffer message = new StringBuffer();
		for (Map.Entry<String, String> entry : errorLog.entrySet()) {
			message.append(entry.getKey());
			message.append('=');
			message.append(entry.getValue());
			message.append('\n');
		}
		message.deleteCharAt(message.length() - 2);
		return message.toString();
	}

	/**
	 * Initializes an instance of the ProfileException class. This is the default constructor for an invalid profile.
	 */
	public ProfileException() {
		super(ResourceBundleHelper.getMessageString("Exception.ProfileException"));
	}
	
	/**
	 * Initializes an instance of the ProfileException class using the specified error log.
	 * 
	 * @param errorLog A Map of Profile.Attribute objects and strings that form an error 
	 * log of device profile attribute information.
	 */
	public ProfileException(Map<String, String> errorLog) {
		super(createMessage(errorLog));
	}

}// end ProfileException Class
