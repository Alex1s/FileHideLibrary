package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
class FHFileNotEncryptedException extends FHRuntimeException {

	FHFileNotEncryptedException() {
		super("The FHFile is not encrypted.");
	}

}
