package org.filehide.filehidelibrary;

/**
 * Signals an attempt to open a {@link FHFile} with a incompatible {@link FHFile} version.
 * 
 * <p> This exception will be thrown by the {@link FHFile#FHFile(File)} constructor when the {@link FHFile} has a incompatible {@link FHFile} version.
 * @author alex1s
 */
@SuppressWarnings("serial")
public class IncompatibleFHFileVersionException extends FHException {
	
	/**
	 * Constructs a {@code IncompatibleFHFileVersionException} with a appropriate detail message. The detail message can be retrieved later by the {@link #getMessage()} method.
	 */
	IncompatibleFHFileVersionException() {
		super("The FHFile version is incompatible with this version of FileHide.");
	}

}
