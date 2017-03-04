package org.filehide.filehidelibrary;

/**
 * Signals an attempt to open a {@link FHFile} which is unencrypted.
 * 
 * <p> This exception will be thrown by the {@link FHFile#extractHiddenData(Path, String)} function when the {@link FHFile} is unencrypted. For unencrypted files use {@link FHFile#extractHiddenData(Path)} instead.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class FHFileUnencryptedException extends FHRuntimeException {

	/**
	 * Constructs a {@code FHFileUnencryptedException} with a appropriate detail message. The detail message can be retrieved later by the {@link #getMessage()} method.
	 */
	FHFileUnencryptedException() {
		super("The FHFile is unencrypted.");
	}

}
