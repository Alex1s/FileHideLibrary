package org.filehide.filehidelibrary;

import java.io.File;
import java.io.IOError;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

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
class FHFile extends File {
	// magic numbers
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
	
	// constants
	/**
	 * The current file version that is supported by this version of FileHide.
	 */
	static final int CURRENT_FILE_VERSION = 0;
	
	// offsets
	/**
	 * The offset that points to the beginning of the hidden data.
	 */
	private long offsetStart;
	/**
	 * The offset that points to the end of the hidden data.
	 */
	private long offsetEnd;
	
	// crypt
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
	
	
	/**
	 * Deletes the data hidden inside of this file.
	 * <p>
	 * Note: Do not use this object anymore in any way after this method has been called.
	 * @throws IOException if an I/O error occurs
	 */
	void deleteHiddenData() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(this, "w");
		raf.setLength(this.offsetStart());
		raf.close();
	}
	
	/**
	 * Checks wheather the given password string can be used to decrypt the hidden data of this file.
	 * @param password the password string to check
	 * @return true if the password can be used, false if not
	 * @throws FHFileNotEncryptedException if the FHFile is not encrypted
	 */
	boolean checkPassword(String password) throws FHFileNotEncryptedException {
		return checkPassword(password.getBytes(FHCipher.CHARSET));
	}
	
	/**
	 * Checks wheather the given password string can be used to decrypt the hidden data of this file.
	 * @param password the given byte array password to check
	 * @return true if the password can be used, false if not
	 * @throws FHFileNotEncryptedException if the FHFile is not encrypted
	 */
	private boolean checkPassword(byte[] password) throws FHFileNotEncryptedException {
		if(!this.encrypted()) throw new FHFileNotEncryptedException();
		FHCipher cipher = new FHCipher(OperationMode.DECRYPT_MODE, password);
		try {
			cipher.getCipher().doFinal(this.cryptoBytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Extracts the encrypted hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to The path to where the hidden data should be extracted to
	 * @param password The password whith which the hidden data is encrypted with.
	 * @throws FHFileNotEncryptedException if this FHFile is not encrypted
	 * @throws IOException if an I/O error occurs
	 */
	void extractHiddenData(Path to, String password) throws FHFileNotEncryptedException, IOException {
		extractHiddenData(to, password.getBytes(FHCipher.CHARSET));
	}
	
	/*
	 * Extracts the encrypted hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to The path to where the hidden data should be extracted to
	 * @param password The password whith which the hidden data is encrypted with.
	 * @throws FHFileNotEncryptedException if this FHFile is not encrypted
	 * @throws IOException if an I/O error occurs
	 */
	void extractHiddenData(Path to, byte[] password) throws FHFileNotEncryptedException, IOException {
		if(!this.encrypted()) throw new FHFileNotEncryptedException();
		Files.copy(new FHInputStream(this, new FHCipher(OperationMode.DECRYPT_MODE, password)), to, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Extracts the hidden data of this FHFile to the given Path and replaces any existing files.
	 * @param to
	 * @throws FHFileNotEncryptedException
	 * @throws FHFileEncryptedException
	 * @throws IOException
	 */
	void extractHiddenData(Path to) throws FHFileEncryptedException, IOException {
		if(this.encrypted()) throw new FHFileEncryptedException();
		Files.copy(new FHInputStream(this), to, StandardCopyOption.REPLACE_EXISTING);
	}
	
	static FHFile hideFile(File origin, File toBeHiddenIn) throws IOException, FHFileCreationFailedException {
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
	
	//# MARK - getters
	
	/**
	 * getter for offsetStart
	 * @return
	 */
	long offsetStart() {
		return this.offsetStart;
	}
	
	/**
	 * getter for offsetEnd
	 * @return
	 */
	long offsetEnd() {
		return this.offsetEnd;
	}
	
	
	/**
	 * getter for encrypted
	 * @return
	 */
	boolean encrypted() {
		return this.encrypted;
	}
	
	/**
	 * getter for cryptoBytes
	 * @return
	 * @throws FHFileNotEncryptedException 
	 */
	byte[] cryptoBytes() throws FHFileNotEncryptedException {
		if(!this.encrypted()) throw new FHFileNotEncryptedException();
		return this.cryptoBytes;
	}
	
	//# MARK - computed constants
	
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
}