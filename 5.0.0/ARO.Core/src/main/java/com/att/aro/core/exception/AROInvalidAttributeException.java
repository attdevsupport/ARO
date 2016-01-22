/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.exception;


public class AROInvalidAttributeException extends ARORuntimeException {
	private static final long serialVersionUID = 686726072969324559L;

	public AROInvalidAttributeException() {
		super(ExceptionType.invalidAttribute);
	}
	public AROInvalidAttributeException(String message) {
		super(ExceptionType.invalidAttribute, message);
	}
	public AROInvalidAttributeException(Throwable cause) {
		super(ExceptionType.invalidAttribute, cause);
	}
	public AROInvalidAttributeException(String message, Throwable cause) {
		super(ExceptionType.invalidAttribute, message, cause);
	}

}
