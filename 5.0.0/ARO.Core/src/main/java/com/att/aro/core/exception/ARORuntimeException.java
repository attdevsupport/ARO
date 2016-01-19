/**
 * 
 */
package com.att.aro.core.exception;

/**
 * @author Nathan F Syfrig
 *
 */
public class ARORuntimeException extends RuntimeException {
	private static final long serialVersionUID = -8786852894857814735L;

	protected final ExceptionType exceptionType;

	public enum ExceptionType {
		unknown,
		invalidAttribute
	}

	protected ARORuntimeException(ExceptionType exceptionType) {
		super();
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, String message) {
		super(message);
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, Throwable cause) {
		super(cause);
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, String message, Throwable cause) {
		super(message, cause);
		this.exceptionType = exceptionType;
	}

	public ARORuntimeException() {
		this(ExceptionType.unknown);
	}
	public ARORuntimeException(String message) {
		this(ExceptionType.unknown, message);
	}
	public ARORuntimeException(Throwable cause) {
		this(ExceptionType.unknown, cause);
	}
	public ARORuntimeException(String message, Throwable cause) {
		this(ExceptionType.unknown, message, cause);
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}
}
