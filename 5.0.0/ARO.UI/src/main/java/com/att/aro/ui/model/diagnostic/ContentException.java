package com.att.aro.ui.model.diagnostic;

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