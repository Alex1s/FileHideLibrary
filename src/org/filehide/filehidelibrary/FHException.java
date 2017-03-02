package org.filehide.filehidelibrary;

/**
 * Signals that an FH exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted FH operations.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class FHException extends Exception {

	/**
	 * Constructs an {@code FHException} with the specified detail message.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
	 */
	FHException(String message) {
		super(message);
	}

}
