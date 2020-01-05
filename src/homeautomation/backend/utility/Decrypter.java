package homeautomation.backend.utility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Decrypter {

	public static void main(String[] args) throws Exception {
		String encodedCipherText = "";
		encodedCipherText = encodedCipherText.replace("\r\n", "");
		StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get("~/.ssh/private-pkcs8.pem"), StandardCharsets.UTF_8).forEach(sb::append);

		String originalText = Base64.getEncoder().encodeToString(decrypt(sb.toString(), encodedCipherText));
	    System.out.println(originalText);
	}
	
	public static String encrypt(String pkcs8PublicKey, String input)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(pkcs8PublicKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }
		
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PUBLIC KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PUBLIC KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
        
        byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);
        
		byte[] pubKey = pkcs8EncodedBytes;
		X509EncodedKeySpec x509EncKeySpecPubKey = new X509EncodedKeySpec(pubKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(x509EncKeySpecPubKey);
		Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

	    byte[] cipherText = encryptCipher.doFinal(input.getBytes());

		return Base64.getEncoder().encodeToString(cipherText);
	}

	public static byte[] decrypt(String pkcs8PrivateKey, String input)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] cipherText = Base64.getDecoder().decode(input);
		
		StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(pkcs8PrivateKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }
        
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");

        byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    decryptCipher.init(Cipher.DECRYPT_MODE, privKey);
	    
	    byte[] originalText = decryptCipher.doFinal(cipherText);
	    return originalText;
	}
}
