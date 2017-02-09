package org.filehide.filehidelibrary;

/**
 * This class is the general class of exceptions produced by failed or interrupted FileHide operations.
 * @author alex1s
 */
public class FHException extends Exception {

	public FHException() {
	}

	public FHException(String message) {
		super(message);
	}

	public FHException(Throwable cause) {
		super(cause);
	}

	public FHException(String message, Throwable cause) {
		super(message, cause);
	}

	public FHException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}