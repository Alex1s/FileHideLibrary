package org.filehide.filehidelibrary;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import javax.crypto.CipherOutputStream;

import org.filehide.filehidelibrary.FHCipher.OperationMode;
/**
 * OutputStream to write the hidden data in of a file.
 * @author alex1s
 *
 */
class FHOutputStream extends FilterOutputStream {
	/**
	 * The file where the hidden data will be written to.
	 */
	private File file;
	/**
	 * The cipher that which the hidden data will be encrypted with.
	 */
	private FHCipher cipher;
	/**
	 * The original length of data before the hidden data has been added to it.
	 */
	private long originalFileLength;
	
	/**
	 * Weather the next written data should be encrypted or not.
	 */
	private boolean encrypt = false;
	
	//# MARK - constructors
	
	/**
	 * The private main constructor that is beeing by the public ones.
	 * @param file the file to write the hidden data to
	 * @param cipher the cipher used to decrypt with hidden data
	 * @param dummy	is just there to avoid it ambiguosity: {@code this(x, null)}
	 * @throws IOException if an I/O error occurs
	 */
	private FHOutputStream(File file, FHCipher cipher, boolean dummy) throws IOException {
		super(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true))));
		this.file = file;
		this.cipher = cipher;
		this.originalFileLength = file.length();
		
		writeFHHead();
		
		writeEncrypted(true);
	}
	
	/**
	 * Constructor for an unencrpyted output stream.
	 * @param file the file to write the hidden data to
	 * @throws IOException if an I/O error occurs
	 */
	FHOutputStream(File file) throws IOException {
		this(file, null, false);
	}
	
	/**
	 * Constructor for an encrypted output stream using raw bytes as password.
	 * @param file file the file to write the hidden data to
	 * @param password the password to decrypt hidden data with
	 * @throws IOException if an I/O error occurs
	 */
	FHOutputStream(File file, byte[] password) throws IOException {
		this(file, new FHCipher(OperationMode.ENCRYPT_MODE, password), true);
	}
	
	/**
	 * Constructor for an encrypted output stream using a String as password.
	 * @param file file the file to write the hidden data to
	 * @param password the password the password to decrypt hidden data with
	 * @throws IOException if an I/O error occurs
	 */
	FHOutputStream(File file, String password) throws IOException {
		this(file, new FHCipher(OperationMode.ENCRYPT_MODE, password), true);
	}
	
	@Override // writes the end of the FHFile and then closes the stream
	public void close() throws IOException {
		writeFHEnd();
		super.close();
	}
	
	/**
	 * Writes the head of the FHFile, takes all needed information from {@code this.file}.
	 * @throws IOException if an I/O error occurs
	 */
	private void writeFHHead() throws IOException {
		out.write(FHFile.FH_START);
		((DataOutputStream) out).writeInt(FHFile.CURRENT_FILE_VERSION);
		
		writeEncrypted(true);
		out.write(FHFile.FH_CRYPT);
		writeEncrypted(false); // need to stop here so that padding will be written so that it can be read on its own later
	}
	
	/**
	 * Writes the end of the FHFile, takes all needed information from {@code this.file}.
	 * @throws IOException if an I/O error occurs
	 */
	 private void writeFHEnd() throws IOException {
		 writeEncrypted(false);
		 
		 ((DataOutputStream) out).writeLong(originalFileLength);
		 out.write(FHFile.FH_END);
	 }
	 
	 /**
	  * Dis- or enable encrypted writing.
	  * @param encrypt weather to encrypt or not
	 * @throws IOException if an I/O error occurs
	  */
	 private void writeEncrypted(boolean encrypt) throws IOException {
			if(encrypt) {
				if(this.cipher == null) return; // return as there is no cipher eg. it is not wished that the data should get encrypted
				if(this.encrypt) return; // return if already writing encrypted
				
				this.out.close();
				this.out = new DataOutputStream(new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(this.file, true)), this.cipher.getCipher()));
				this.encrypt = true;
			} else {
				if(!this.encrypt) return; // return if already not writing encrypted
				
				this.out.close();
				this.out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file, true)));
				this.encrypt = false;
			}
	 }
}