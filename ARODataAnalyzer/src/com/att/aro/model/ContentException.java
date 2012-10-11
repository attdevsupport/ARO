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
 * An exception that is thrown when an error occurs while reading content.
 */
public class ContentException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes an instance of the ContentException class..
	 */
	public ContentException() {
		super();
	}

	/**
	 * Initializes an instance of the ContentException class using the specified exception 
	 * message, and cause. 
	 * 
	 * @param message - The exception message. 
	 * 
	 * @param cause A Throwable object that indicates the cause of the exception.
	 */
	public ContentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Initializes an instance of the ContentException class using the specified 
	 * exception message. 
	 * 
	 * @param message The exception message. 
	 */
	public ContentException(String message) {
		super(message);
	}

	/**
	 * Initializes an instance of the ContentException class using the specified cause. 
	 * 
	 * @param cause A Throwable object that indicates the cause of the exception.
	 */
	public ContentException(Throwable cause) {
		super(cause);
	}

}
