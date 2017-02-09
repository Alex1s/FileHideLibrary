package org.filehide.filehidelibrary;

class FHFileNotEncryptedException extends FHException {

	FHFileNotEncryptedException() {
		super("The FHFile is not encrypted.");
	}
}
