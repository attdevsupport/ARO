/**
 * 
 */
package com.att.aro.ui.exception;


/**
 * @author Nathan F Syfrig
 *
 */
public class AROUIIllegalStateException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public AROUIIllegalStateException() {
		super();
	}
	public AROUIIllegalStateException(String message) {
		super(message);
	}
	public AROUIIllegalStateException(Throwable cause) {
		super(cause);
	}
	public AROUIIllegalStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
