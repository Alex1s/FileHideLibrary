package org.filehide.filehidelibrary;

/**
 * Signals an attempt to open a {@link FHFile} which actually is not a {@link FHFile}.
 * 
 * <p> This exception will be thrown by the {@link FHFile#FHFile(File)} constructor when the file is not actually a {@link FHFile}.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class NotFHFileException extends FHException {

	/**
	 * Constructs a {@code NotFHFileException} with a appropriate detail message. The detail message can be retrieved later by the {@link #getMessage()} method.
	 */
	NotFHFileException() {
		super("The file is not a FHFile.");
	}

}
