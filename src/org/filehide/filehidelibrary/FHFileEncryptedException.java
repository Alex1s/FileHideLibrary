package org.filehide.filehidelibrary;

import java.nio.file.Path;

/**
 * Signals an attempt to open a {@link FHFile} which is encrypted.
 * 
 * <p> This exception will be thrown by the {@link FHFile#extractHiddenData(Path)} function when the {@link FHFile} is encrypted. For encrypted files use the {@link FHFile#extractHiddenData(Path, String)} instead.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class FHFileEncryptedException extends FHRuntimeException {

	/**
	 * Constructs a {@code FHFileEncryptedException} with a appropriate detail message. The string s can be retrieved later by the {@link #getMessage()} method.
	 */
	FHFileEncryptedException() {
		super("The FHFile is encrypted. A valid password is needed to read its hidden file.");
	}

}
