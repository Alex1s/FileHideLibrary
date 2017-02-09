package org.filehide.filehidelibrary;

public class NotFHFileException extends FHException {

	public NotFHFileException() {
		super("The file is not a FHFile.");
	}
}