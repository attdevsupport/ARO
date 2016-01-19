/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.datacollector.pojo;

import com.att.aro.core.pojo.ErrorCode;

/**
 * StatusResult is used to contain the success or failure of some task. The
 * status should always be set.
 * <pre>
 * Whenever the status is a failure, then an ErrorCode must be stored as well.
 *
 * The generic data object can be used to contain data for any reason.
 * </pre>
 *
 */
public class StatusResult {

	private boolean success = false;
	private ErrorCode error;

	/**
	 * A generic object to contain more information. For use with either success
	 * or failure. Depending on the needs of the classes using an instance of
	 * StatusResult.
	 */
	private Object data;

	/**
	 * Returns true if success, false otherwise
	 * 
	 * @return true if success, false otherwise
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Set true for success, false otherwise
	 * 
	 * @param success
	 *            , true for success, false otherwise
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Set some generic data.
	 * 
	 * @return some generic data.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Set some generic data.
	 * 
	 * @param data
	 *            some generic data.
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Returns an ErrorCode
	 * 
	 * @return an ErrorCode
	 */
	public ErrorCode getError() {
		return error;
	}

	/**
	 * Sets an ErrorCode
	 * 
	 * @param error
	 *            , an ErrorCode
	 */
	public void setError(ErrorCode error) {
		this.error = error;
	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("status :");
		sBuffer.append(success ? "success" : "fail:");
		if (!success) {
			sBuffer.append(error);
		}
		return sBuffer.toString();
	}
}
