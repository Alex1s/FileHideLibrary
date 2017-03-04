package org.filehide.filehidelibrary;

/**
 * Signals an attempt to access information of a {@link FHFile} although the file is not a FHFile anymore.
 * 
 * <p> This exception will be thrown by the many of the {@link FHFile} methods if they are beeing used after the {@link FHFile#deleteHiddenData()} has been called.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class NotFHFileAnymoreException extends FHRuntimeException {
	
	/**
	 * Constructs a {@code NotFHFileAnymoreException} with a appropriate detail message. The detail message can be retrieved later by the {@link #getMessage()} method.
	 */
	NotFHFileAnymoreException() {
		super("The file is not a FHFile anymore.");
	}

}

