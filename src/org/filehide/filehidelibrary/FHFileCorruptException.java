package org.filehide.filehidelibrary;

/**
 * Signals an attempt to open a {@link #FHFile} which is currupt.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class FHFileCorruptException extends FHException {

	/**
	 * Constructs a {@code FHFileCorruptException} with a appropriate detail message. The string s can be retrieved later by the {@link #getMessage()} method.
	 */
	FHFileCorruptException() {
		super("The FHFile is corrupted.");
	}

}
