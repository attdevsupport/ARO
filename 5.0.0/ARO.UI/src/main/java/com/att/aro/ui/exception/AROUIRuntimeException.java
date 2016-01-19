/**
 * 
 */
package com.att.aro.ui.exception;


/**
 * @author Nathan F Syfrig
 *
 */
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
