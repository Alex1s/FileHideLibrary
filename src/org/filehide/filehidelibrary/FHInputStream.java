package org.filehide.filehidelibrary;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.CipherInputStream;

// FIXME javax.crypto.IllegalBlockSizeException thrown when closing stream
/**
 * InputStream to read the hidden data out of a FHFile.
 * @author alex1s
 *
 */
class FHInputStream extends FilterInputStream {
	/**
	 * The file which the hidden data will be read from.
	 */
	FHFile file;
	
	/**
	 * The current pointer in the file.
	 */
	private long read;
	/**
	 * Information about wheather the EOF is reached or not.
	 */
	private boolean EOF = false;
	
	//# MARK - init
	
	/**
	 * Constructs a FHInputStream from a unencrypted FHFile.
	 * @param file - a unencrypted FHFile
	 * @throws IOException - if an I/O error occurs
	 * @throws FHFileEncryptedException 
	 */
	FHInputStream(FHFile file) throws IOException, FHFileEncryptedException {
		super(initSuper(file, null));
		if(file.encrypted()) throw new FHFileEncryptedException();
		
		this.file = file;
		this.read = file.offsetStart();
	}
	
	/**
	 * Constructs a FHInoutStream from a encrypted FHFile and a FHCipher.
	 * @param file - a encrpyted FHFile
	 * @param cipher - a fully FHCipher
	 * @throws IOException if an I/O error occurs
	 * @throws FHFileNotEncryptedException 
	 */
	FHInputStream(FHFile file, FHCipher cipher) throws IOException, FHFileNotEncryptedException {
		super(initSuper(file, cipher));
		if(!file.encrypted()) throw new FHFileNotEncryptedException();
		
		this.file = file;
		this.read = file.offsetStart();
	}
	
	/**
	 * Help function to initialize super.<br>
	 * NOTE: Wheather a encrypted super or a unecrypted super is created depends
	 * upon wheather the given cipher is null or not.
	 * @param file - a FHFile
	 * @param cipher - a FHCipher (should be null if the FHFile is unencrpyted)
	 * @return - the InputStream to initialize super with
	 * @throws IOException - if an I/O error occurs
	 * @throws FHFileNotEncryptedException 
	 * @throws FHFileEncryptedException 
	 */
	private static InputStream initSuper(FHFile file, FHCipher cipher) throws IOException {
		FileInputStream fileIs = new FileInputStream(file);
		fileIs.skip(file.offsetStart());
		
		FilterInputStream in;
		if(cipher != null) {
			in = new CipherInputStream(fileIs, cipher.getCipher());
		} else {
			in = new BufferedInputStream(fileIs);
		}
		return in;
	}

	//# MARK - overwriting super
	
	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		int response = read(b);
		
		if(response < 0)
			return response;
		else
			return b[0];
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(this.EOF) return -1;
		return super.read(b, off, checkLen(len));
	}
	
	//# MARK - help
	
	/**
	 * Helper function to check wheather the given length is acceptable and does not go further
	 * than the point where the hidden data ends within the hosting file.
	 * @param len - the length to check
	 * @return the eventually modified length that should be read
	 */
	private int checkLen(int len) {
		this.read += len;
		long diff = this.file.offsetEnd() - this.read;
		
		int response; 
		if(diff <= 0) {
			this.EOF = true;
			response = len + (int) diff;
		} else 
			response = len;
		System.out.println("respose: " + response);
		return response;
	}
}