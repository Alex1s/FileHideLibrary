package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
class NotFHFileException extends FHException {

	NotFHFileException() {
		super("The file is not a FHFile.");
	}

}
