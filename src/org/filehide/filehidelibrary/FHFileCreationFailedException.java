package org.filehide.filehidelibrary;

/**
 * Signals a failed attempt to create a {@link FHFile}.
 * 
 * <p> This exception will be thrown by the {@code hideFile} functions of class {@link FHFile} when the creation failed.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class FHFileCreationFailedException extends FHException {

	/**
	 * Constructs a {@code FHFileCreationFailedException} with a appropriate detail message. The detail message can be retrieved later by the {@link #getMessage()} method.
	 */
	FHFileCreationFailedException() {
		super("The creation of the FHFile has failed");
	}

}
