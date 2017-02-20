package org.filehide.filehidelibrary;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.filehide.filehidelibrary.FHCipher.OperationMode;

/**
 * Class that describes a FHFile.
 * <p>
 * A FHFIle (version 0) has following structure:<br>
 * 1. the bytes of the original file<br>
 * 2. 10 bytes: starting magic number ({@code 0x41 0x6C 0x65 0x78 0x31 0x73 0x42 0x69 0x67 0x44})<br>
 * 3. 4 byte two´s-complement integer: the FHFile´s version number<br>
 * 4. 16 bytes: ({@code 0x46 0x48 0x43 0x72 0x79 0x70 0x74 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00}) or this value encrypted if the file is encrypted<br>
 * 5. the bytes of the hidden file<br>
 * 6. 8 byte two´s-complement long: offset of the starting magic number (same as length of the original file)<br>
 * 7. 13 bytes: ending magic number ({@code 0x41 0x6C 0x65 0x78 0x31 0x73 0x42 0x69 0x67 0x44 0x45 0x6E 0x64)
 * @author Alex1s
 */
@SuppressWarnings("serial")
public class FHFile extends File {
	
	// MARK magic numbers
	
	/**
	 * The starting magic number of a FHFile.
	 */
	static final byte[] FH_START = {0x41, 0x6C, 0x65, 0x78, 0x31, 0x73, 0x42, 0x69, 0x67, 0x44};
	/**
	 * The ending magic number of a FHFile.
	 */
	static final byte[] FH_END = {0x41, 0x6C, 0x65, 0x78, 0x31, 0x73, 0x42, 0x69, 0x67, 0x44, 0x45, 0x6E, 0x64};
	/**
	 * The bytes that are encrypted if the FHFile is encrypted. This should make it easier to detect incorrect passwords.
	 */
	static final byte[] FH_CRYPT = {0x46, 0x48, 0x43, 0x72, 0x79, 0x70, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	
	
	// MARK constants
	
	/**
	 * The current file version that is supported by this version of FileHide.
	 */
	static final int CURRENT_FILE_VERSION = 0;
	
	
	// MARK offsets
	
	/**
	 * The offset that points to the beginning of the hidden data.
	 */
	private long offsetStart;
	
	/**
	 * The offset that points to the end of the hidden data.
	 */
	private long offsetEnd;
	
	
	// MARK crypt
	
	/**
	 * Weather the file is encrypted or not.
	 */
	private boolean encrypted;
	
	/**
	 * The CryptoBytes of the file. Only not null of the file is encrypted.
	 * <p>
	 * These bytes can be used to check if a password is correct to decrypt the hidden data.
	 */
	private byte[] cryptoBytes;
	
	
	// MARK other
	
	/**
	 * 
	 */
	private boolean hiddenDataDeleted = false;
	
	/**
	 * Constructor for a FHFile.
	 * @param file the FHFile
	 * @throws IOException if an I/O error occurs
	 * @throws NotFHFileException if the given file is not a FHFile
	 * @throws FHFileCorruptException if the given file is corrept, meaning it does not follow the structur of a FHFile
	 * @throws IncompatibleFHFileVersionException if the file version of the given file is not compatible with this version of FileHide
	 */
	public FHFile(File file) throws IOException, NotFHFileException, FHFileCorruptException, IncompatibleFHFileVersionException {
		super(file.getCanonicalPath());
		
		RandomAccessFile raf = new RandomAccessFile(this, "r");
		
		// ending magic number
		raf.seek(this.length() - FH_END.length);
		byte[] end = new byte[FH_END.length];
		raf.read(end);
		if(!Arrays.equals(FH_END, end)) {
			raf.close();
			throw new NotFHFileException();
		}
		
		// hidden data offset
		this.offsetEnd = (this.length() - FH_END_LENGTH());
		raf.seek(this.offsetEnd);
		long originalFileLength = raf.readLong();
		long hiddenDataLength = this.length() - originalFileLength - FH_HEAD_LENGTH() - FH_END_LENGTH();
		if(hiddenDataLength <= 0) {
			raf.close();
			throw new FHFileCorruptException();
		}
		
		// starting magic number
		raf.seek(originalFileLength);
		byte[] start = new byte[FH_START.length];
		raf.read(start);
		if(!Arrays.equals(FH_START, start)) {
			raf.close();
			throw new FHFileCorruptException();
		}
		
		// file version
		int fileVersion = raf.readInt();
		if(fileVersion < 0) {
			raf.close();
			throw new FHFileCorruptException();
		} else if(fileVersion != CURRENT_FILE_VERSION) {
			raf.close();
			throw new IncompatibleFHFileVersionException();
		}
		
		// crypto bytes
		byte[] cryptoBytes = new byte[FHCipher.BYTES];
		raf.read(cryptoBytes);
		if(!Arrays.equals(FH_CRYPT, cryptoBytes)) {
			if(hiddenDataLength % 16 != 0) {
				raf.close();
				throw new FHFileCorruptException();
			}
			this.cryptoBytes = cryptoBytes;
			this.encrypted = true;
		} else
			this.encrypted = false;
		
		this.offsetStart = raf.getFilePointer();
		
		raf.close();
	}
	
	
	// MARK public functions
	
	/**
	 * Deletes the data hidden inside of this file.
	 * <p>
	 * Note: Do not use this object anymore in any way after this method has been called.
	 * @throws IOException if an I/O error occurs
	 */
	public void deleteHiddenData() throws IOException {
		this.hiddenDataDeleted = true;
		RandomAccessFile raf = new RandomAccessFile(this, "w");
		raf.setLength(this.offsetStart());
		raf.close();
	}
	
	
	// MARK password checking
	
	/**
	 * Checks wheather the given password string can be used to decrypt the hidden data of this file.
	 * @param password the password string to check
	 * @return true if the password can be used, false if not
	 * @throws FHFileNotEncryptedException if the FHFile is not encrypted
	 */
	public boolean checkPassword(String password) throws FHFileNotEncryptedException {
		return checkPassword(password.getBytes(FHCipher.CHARSET));
	}
	
	/**
	 * Checks wheather the given password string can be used to decrypt the hidden data of this file.
	 * @param password the given byte array password to check
	 * @return true if the password can be used, false if not
	 */
	private boolean checkPassword(byte[] password) {
		hiddenDataDeleted();
		if(!this.encrypted()) throw new FHFileNotEncryptedException();
		FHCipher cipher = new FHCipher(OperationMode.DECRYPT_MODE, password);
		try {
			cipher.getCipher().doFinal(this.cryptoBytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return false;
		}
		return true;
	}
	
	
	// MARK extract hidden data (not password proteced)
	
	/**
	 * Extracts the hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to
	 * @throws IOException
	 */
	public void extractHiddenData(Path to) throws IOException {
		hiddenDataDeleted();
		if(this.encrypted()) throw new FHFileEncryptedException();
		Files.copy(new FHInputStream(this), to, StandardCopyOption.REPLACE_EXISTING);
	}
	
	
	// MARK extract hidden data (password protected)
	
	/**
	 * Extracts the encrypted hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to The path to where the hidden data should be extracted to
	 * @param password The password whith which the hidden data is encrypted with.
	 * @throws FHFileNotEncryptedException if this FHFile is not encrypted
	 * @throws IOException if an I/O error occurs
	 */
	public void extractHiddenData(Path to, String password) throws IOException {
		extractHiddenData(to, password.getBytes(FHCipher.CHARSET));
	}
	
	/*
	 * Extracts the encrypted hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to The path to where the hidden data should be extracted to
	 * @param password The password whith which the hidden data is encrypted with.
	 * @throws FHFileNotEncryptedException if this FHFile is not encrypted
	 * @throws IOException if an I/O error occurs
	 */
	private void extractHiddenData(Path to, byte[] password) throws IOException {
		hiddenDataDeleted();
		if(!this.encrypted()) throw new FHFileNotEncryptedException();
		Files.copy(new FHInputStream(this, new FHCipher(OperationMode.DECRYPT_MODE, password)), to, StandardCopyOption.REPLACE_EXISTING);
	}
	
	
	// MARK hide data in a file (inplace, not password protected)
	
	/**
	 * Hides a file inside a file and saves the result at a given location without touching the file the other file will be hidden in.
	 * @param origin the file which contains the data to be hidden
	 * @param toBehiddenIn the file which should contain the hidden file (will be left untouched)
	 * @param whereToSave location to save the result
	 * @return the created FHFile
	 * @throws IOException if an I/O error occurs
	 * @throws FHFileCreationFailedException if the creation of the FHFile failed
	 */
	public static FHFile hideFile(File origin, File toBehiddenIn, Path whereToSave) throws IOException, FHFileCreationFailedException {
		Files.copy(toBehiddenIn.toPath(), whereToSave, StandardCopyOption.REPLACE_EXISTING);
		try {
			return hideFile(origin, whereToSave.toFile());
		} catch (FHFileCreationFailedException e) {
			// cleanup
			Files.delete(whereToSave);
			throw new FHFileCreationFailedException();
		}
	}
	
	/**
	 * Hides a file inside a file.
	 * @param origin the file which contains the data to be hidden
	 * @param toBeHiddenIn the file which should contain the hidden file
	 * @return the created FHFile
	 * @throws IOException if an I/O error occurs
	 * @throws FHFileCreationFailedException if the creation of the FHFile failed
	 */
	public static FHFile hideFile(File origin, File toBeHiddenIn) throws IOException, FHFileCreationFailedException {
		long originalFileLegth = toBeHiddenIn.length();
		Files.copy(toBeHiddenIn.toPath(), new FHOutputStream(origin));
		try {
			return new FHFile(toBeHiddenIn);
		} catch (NotFHFileException | FHFileCorruptException | IncompatibleFHFileVersionException e) {
			// cleanup
			RandomAccessFile raf = new RandomAccessFile(toBeHiddenIn, "w");
			raf.setLength(originalFileLegth);
			raf.close();
			
			throw new FHFileCreationFailedException();
		}
	}
	
	
	// MARK getters
	
	/**
	 * getter for offsetStart
	 * @return
	 */
	long offsetStart() {
		hiddenDataDeleted();
		return this.offsetStart;
	}
	
	/**
	 * getter for offsetEnd
	 * @return
	 */
	long offsetEnd() {
		hiddenDataDeleted();
		return this.offsetEnd;
	}
	
	
	/**
	 * getter for encrypted
	 * @return
	 */
	boolean encrypted() {
		hiddenDataDeleted();
		return this.encrypted;
	}
	
	/**
	 * getter for cryptoBytes
	 * @return
	 */
	byte[] cryptoBytes() {
		hiddenDataDeleted();
		return this.cryptoBytes;
	}
	
	// MARK - computed constants
	
	/**
	 * The length of the head of the hidden content.
	 * @return
	 */
	int FH_HEAD_LENGTH() {
		return FH_START.length + Integer.BYTES + FHCipher.BYTES;
	}
	/**
	 * The length of the end of the hidden content.
	 * @return
	 */
	int FH_END_LENGTH() {
		return Long.BYTES + FH_END.length;
	}
	
	// MARK helper functions
	
	/**
	 * This functions checks wheather the hidden Data of this file have been removed and throws a NotFHFileAnymoreException if so.
	 * @return flase, otherwise a RuntimeException will be trown
	 */
	private boolean hiddenDataDeleted() {
		if(this.hiddenDataDeleted) throw new NotFHFileAnymoreException();
		return false;
	}

}
