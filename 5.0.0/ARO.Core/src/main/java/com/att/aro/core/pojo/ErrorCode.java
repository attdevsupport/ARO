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
package com.att.aro.core.pojo;

/**
 * Models errors.
 *   data fields:
 *     int code             // An int to identify an ErrorCode
 *     String name          // A descriptive name for ErrorCode
 *     String description   // A useful description of ErrorCode
 */
public class ErrorCode {
	
	/**
	 * An int to identify an ErrorCode
	 */
	private int code;
	
	/**
	 * A descriptive name for ErrorCode
	 */
	private String name;
	
	/**
	 * A useful description of ErrorCode
	 */
	private String description;

	/**
	 * 
	 * @return code
	 */
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * 
	 * @return name of ErrorCode
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return description of ErrorCode
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("code:");
		sBuffer.append(code);
		if (name != null) {
			sBuffer.append(", name:");
			sBuffer.append(name);
		}
		if (description != null) {
			sBuffer.append(", description:");
			sBuffer.append(description);
		}
		return sBuffer.toString();
	}
}
