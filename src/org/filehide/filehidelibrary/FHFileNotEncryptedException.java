package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
public class FHFileNotEncryptedException extends FHRuntimeException {

	FHFileNotEncryptedException() {
		super("The FHFile is not encrypted.");
	}

}
