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

/**
 * Thrown when an error occurs reading content
 */
public class ContentException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ContentException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            Exception message.
	 * @param cause
	 *            Throwable object.
	 */
	public ContentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            Exception message
	 */
	public ContentException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            Throwable object.
	 */
	public ContentException(Throwable cause) {
		super(cause);
	}

}
