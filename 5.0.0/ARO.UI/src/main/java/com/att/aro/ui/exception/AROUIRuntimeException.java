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
package com.att.aro.ui.exception;

public class AROUIRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -8786852894857814735L;

	public enum ExceptionType {
		unknown,
		panel
	}

	protected final ExceptionType exceptionType;

	protected AROUIRuntimeException(ExceptionType exceptionType) {
		super();
		this.exceptionType = exceptionType;
	}
	protected AROUIRuntimeException(ExceptionType exceptionType, String message) {
		super(message);
		this.exceptionType = exceptionType;
	}
	protected AROUIRuntimeException(ExceptionType exceptionType, Throwable cause) {
		super(cause);
		this.exceptionType = exceptionType;
	}
	protected AROUIRuntimeException(ExceptionType exceptionType, String message, Throwable cause) {
		super(message, cause);
		this.exceptionType = exceptionType;
	}

	public AROUIRuntimeException() {
		this(ExceptionType.unknown);
	}
	public AROUIRuntimeException(String message) {
		this(ExceptionType.unknown, message);
	}
	public AROUIRuntimeException(Throwable cause) {
		this(ExceptionType.unknown, cause);
	}
	public AROUIRuntimeException(String message, Throwable cause) {
		this(ExceptionType.unknown, message, cause);
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}
}
