/**
 * 
 */
package com.att.aro.ui.exception;



/**
 * @author Nathan F Syfrig
 *
 */
public class AROUIPanelException extends AROUIRuntimeException {
	private static final long serialVersionUID = 686726072969324559L;

	public AROUIPanelException() {
		super(ExceptionType.panel);
	}
	public AROUIPanelException(String message) {
		super(ExceptionType.panel, message);
	}
	public AROUIPanelException(Throwable cause) {
		super(ExceptionType.panel, cause);
	}
	public AROUIPanelException(String message, Throwable cause) {
		super(ExceptionType.panel, message, cause);
	}

}
