package org.filehide.filehidelibrary;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cipher to de- and encrypted the hidden contents of FHFiles.
 * @author Alex1s
 */
class FHCipher {
	/**
	 * Operation modes for this Cipher.
	 * @author Alex1s
	 */
	enum OperationMode {
		/**
		 * The mode to encrypt data with.
		 */
		ENCRYPT_MODE(1),
		/**
		 * The mode to decrypt data with.
		 */
		DECRYPT_MODE(2);
		
		/**
		 * Value of the mode (1 | 2).
		 */
		private int value;
		
		/**
		 * Constructs a Operation mode using it´s value.
		 * @param value
		 */
		private OperationMode(int value) {
			this.value = value;
		}
		
		/**
		 * getter for value
		 * @return the value of this OperationMode
		 */
		int value() {
			return this.value;
		}
	}
	
	// constants
	/**
	 * The size of a block of encrypted data.
	 * <p>
	 * Also the size of the FH_CRYPT as that is exactly one block.
	 */
	static Integer BYTES = 16;
	
	/**
	 * The encryption algorithm and padding used in this cipher.
	 */
	private final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	/**
	 * The algorithm used for the secret key.
	 */
	private final String ALGORITHM = "AES";
	/**
	 * The algorithm used to hash passwords with.
	 */
	private final String HASH_ALGORITHM = "SHA-256";
	
	/**
	 * The charset used to decode String-Passwords.
	 */
	static final Charset CHARSET = StandardCharsets.UTF_8;
	
	/**
	 * The cipher.
	 */
	private Cipher cipher;
	
	/**
	 * Constructs a FHCipher object using a string as password.
	 * @param opmode the Operation mode to run the cipher with
	 * @param password password used to de- or encrypt data with
	 */
	FHCipher(OperationMode opmode, String password) {
		this(opmode, password.getBytes(CHARSET));
	}
	
	/**
	 * Constructs a FHCipher object using a byte array as password.
	 * @param opmode the Operation mode to run the cipher with
	 * @param password password used to de- or encrypt data with
	 */
	FHCipher(OperationMode opmode, byte[] password) {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(getSecretKey(password), ALGORITHM);
			IvParameterSpec ivParamterSpec = new IvParameterSpec(getIV(password));
			this.cipher = Cipher.getInstance(TRANSFORMATION);
			this.cipher.init(opmode.value(), secretKeySpec, ivParamterSpec);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ignored) {}
	}
	
	/**
	 * Generates the iv, derived from the password.
	 * @param password the password to derive the iv from
	 * @return the iv
	 */
	private byte[] getIV(byte[] password) {
		return getSecretKey(getSecretKey(password));
	}
	
	/**
	 * Generates the secret key.
	 * @param password the password to generate the secret key from
	 * @return bytes to generate a valid secret key with
	 */
	private byte[] getSecretKey(byte[] password) {
		try {
			return Arrays.copyOf(MessageDigest.getInstance(HASH_ALGORITHM).digest(password), BYTES);
		} catch (NoSuchAlgorithmException ignored) {}
		return null;
	}
	
	/**
	 * getter for cipher
	 * @return the cipher
	 */
	Cipher getCipher() {
		return this.cipher;
	}
}