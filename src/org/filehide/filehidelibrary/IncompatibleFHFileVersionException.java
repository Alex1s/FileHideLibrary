package org.filehide.filehidelibrary;

class IncompatibleFHFileVersionException extends FHException {
	public IncompatibleFHFileVersionException() {
		super("The FHFile version is incompatible with this version of FileHide.");
	}

}
