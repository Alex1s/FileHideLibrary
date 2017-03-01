package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
public class NotFHFileException extends FHException {

	NotFHFileException() {
		super("The file is not a FHFile.");
	}

}
