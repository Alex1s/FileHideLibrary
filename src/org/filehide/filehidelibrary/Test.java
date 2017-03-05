package org.filehide.filehidelibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.filehide.filehidelibrary.FHCipher.OperationMode;

class Test {
	
	private static String password = "mein_Passwort";
	
	private static String filepathUnencrypted = "data/IMG_5998.unenc.JPG";
	private static String filepathEncrypted = "data/IMG_5998.enc.JPG";
	private static String outputPath = "data/out.txt";
	private static byte[] data = "Das ist die krasse Nachricht!~".getBytes(StandardCharsets.US_ASCII);

	
	public static void main(String[] args) {
//		testInputStream(filepathEncrypted, password);
//		testExtract(filepathUnencrypted, outputPath);
		testExtract(filepathEncrypted, outputPath, password);
	}
	
	
	private static void testFHFIle(String filepath) {
		try {
			FHFile file = new FHFile(new File(filepath));
			System.out.println(file.encrypted());
			FHCipher cipher = new FHCipher(OperationMode.DECRYPT_MODE, password);
//			System.out.println("This should be \"FHCrypt\":" + new String(cipher.getCipher().doFinal(file.cryptoBytes())));
			System.out.println("Encrypted text: " + new String(cipher.getCipher().doFinal(DatatypeConverter.parseHexBinary("B9859BAB865327A50B499F5406C2BE48"))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFHFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileCorruptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncompatibleFHFileVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testInputStream(String filepath, String password) {
		try {
			byte[] buffer = new byte[1024];
			byte[] buffer2 = new byte[1024];
			//test
			new FHFile(new File(filepathUnencrypted));
			FHInputStream in;
			FHFile file = new FHFile(new File(filepath));
			System.out.println(file.offsetStart());
			System.out.println(file.offsetEnd());
			if(password != null) {
				FHCipher cipher = new FHCipher(OperationMode.DECRYPT_MODE, password);
				in = new FHInputStream(file, cipher);
			} else
				in = new FHInputStream(new FHFile(new File(filepath)));
			
			int bytesRead = in.read(buffer);
			int bytesRead2 = in.read(buffer2);
			
			System.out.println("Was reading " + bytesRead + " bytes");
			System.out.println("Data read (as ASCII): " + new String(Arrays.copyOf(buffer, bytesRead)));
			
			System.out.println("Was reading " + bytesRead2 + " bytes");
//			System.out.println("Data read (as ASCII): " + new String(Arrays.copyOf(buffer2, bytesRead2)));
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFHFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileCorruptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncompatibleFHFileVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileUnencryptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileEncryptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static private void testExtract(String in, String out) {
		try {
			FHFile file = new FHFile(new File(in));
			Path path = Paths.get(out);
			file.extractHiddenData(path);
		} catch (NotFHFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileCorruptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncompatibleFHFileVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FHFileEncryptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static private void testExtract(String in, String out, String password) {
		try {
			FHFile file = new FHFile(new File(in));
			Path path = Paths.get(out);
			file.extractHiddenData(path, password);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * For debug purpuse.
	 * Converts a byte array to a base64 encoded String.
	 * @return base64-string of given data
	 */
	static String toBase64(byte[] data) {
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(data);
	}
}