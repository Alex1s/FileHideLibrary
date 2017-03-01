package org.filehide.filehidelibrary;

@SuppressWarnings("serial")
public class NotFHFileAnymoreException extends FHRuntimeException {
	
	NotFHFileAnymoreException() {
		super("The file is not a FHFile anymore.");
	}

}

