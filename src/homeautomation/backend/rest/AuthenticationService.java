package homeautomation.backend.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import homeautomation.backend.entity.User;

public class AuthenticationService {
	protected static EntityManagerFactory EMF = Persistence.createEntityManagerFactory("HomeAutomationPU");
	protected static EntityManager EM = null;
	private static int VALID_TIMESPAN_MS = 15000;

	public boolean authenticateDigest(String hashToAuthenticate, String email)
			throws NoResultException, NoSuchAlgorithmException, UnsupportedEncodingException {
		if (null == hashToAuthenticate)
			return false;
		final String encodedCredentials = hashToAuthenticate.replaceFirst("Digest" + " ", "");
		if (EM == null)
			EM = EMF.createEntityManager();
		User user = null;
		try {
			user = EM.createQuery("SELECT u FROM User u where u.email = :email", User.class)
					.setParameter("email", email).getSingleResult();

		} catch (NoResultException e) {
			return false;
		}
		if (!user.isActivated())
			return false;
		String username = user.getEmail();
		String secret = user.getSecret();
		byte[] nonce = user.getNonce();
		Timestamp time = user.getNonceCreationDate();
		byte[] decodedBytes = Base64.decodeBase64(encodedCredentials);
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		byte[] input = new byte[nonce.length + Base64.decodeBase64(secret).length + (username + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time)).getBytes().length];
		System.arraycopy(nonce, 0, input, 0, nonce.length);
		System.arraycopy(Base64.decodeBase64(secret), 0, input, nonce.length, Base64.decodeBase64(secret).length);
		System.arraycopy((username + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time)).getBytes(), 0, input,
				nonce.length + Base64.decodeBase64(secret).length,
				(username + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time)).getBytes().length);
		byte[] hash = digest.digest(input);
		if (Arrays.equals(decodedBytes, hash) && (System.currentTimeMillis() - time.getTime()) < VALID_TIMESPAN_MS) {
			nonce = createNonce(new Random().nextLong());
			updateNonceInDatabase(username, nonce, new Timestamp(System.currentTimeMillis()));
			return true;
		}
		return false;
	}

	public void updateNonceInDatabase(String email, byte[] nonce, Timestamp time) {
		EM.getTransaction().begin();
		User u = EM.find(User.class, email);
		if (u != null) {
			u.setNonce(nonce);
			u.setNonceCreationDate(time);
		}
		EM.getTransaction().commit();
	}

//	public boolean authenticateBasic(String authCredentials) {
//
//		if (null == authCredentials)
//			return false;
//		// header value format will be "Basic encodedstring" for Basic
//		// authentication. Example "Basic YWRtaW46YWRtaW4="
//		final String encodedUserPassword = authCredentials.replaceFirst("Basic" + " ", "");
//		String usernameAndPassword = null;
//		try {
//			byte[] decodedBytes = Base64.decodeBase64(encodedUserPassword);
//			usernameAndPassword = new String(decodedBytes, "UTF-8");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
//		final String username = tokenizer.nextToken();
//		final String password = tokenizer.nextToken();
//
//		// we have fixed the userid and password as admin
//		// call some UserService/LDAP here
//		boolean authenticationStatus = "admin".equals(username) && "admin".equals(password);
//		return authenticationStatus;
//	}

	public byte[] createNonce(long seed) {
		Random random = new Random();
		random.setSeed(seed);
		byte[] bytes = new byte[256];
		random.nextBytes(bytes);
		return bytes;
	}

	public static HttpURLConnection authenticateRequest(HttpURLConnection con, URL url, String username, String secret)
			throws IOException, NoSuchAlgorithmException {
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Email", username);
		int status = con.getResponseCode();

		if (status == 401) {
			byte[] nonce = Base64.decodeBase64(con.getHeaderField("Nonce"));
			String time = con.getHeaderField("Time");
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			byte[] hash = digest.digest((username + ":" + secret + ":" + new String(nonce) + ":" + time).getBytes());
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Email", username);
			con.setRequestProperty("Authorization", "Digest " + Base64.encodeBase64String(hash));
		}
		return con;
	}
}