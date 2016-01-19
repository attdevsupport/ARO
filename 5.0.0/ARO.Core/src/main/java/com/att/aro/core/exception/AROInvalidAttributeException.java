/**
 * 
 */
package com.att.aro.core.exception;


/**
 * @author Nathan F Syfrig
 *
 */
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
