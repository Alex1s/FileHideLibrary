package org.filehide.filehidelibrary;

/**
 * Exception which is beeing thrown if a File is tried to be read unencrypted without password but the content is encrypted.
 * @author alex1s
 */
class FHFileEncryptedException extends FHException {

	public FHFileEncryptedException() {
		super("The FHFile is encrypted. A valid password is needed to read it´s content.");
	}

}