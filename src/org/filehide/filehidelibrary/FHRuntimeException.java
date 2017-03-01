package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
public class FHRuntimeException extends RuntimeException {

	public FHRuntimeException() {
	}

	public FHRuntimeException(String message) {
		super(message);
	}

	public FHRuntimeException(Throwable cause) {
		super(cause);
	}

	public FHRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FHRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
