package homeautomation.backend.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Before;
import org.junit.jupiter.api.Test;

class AuthenticationTest {

	//Log4j Ordner Ändern!!
	
	public static final String SERVER_BASE_URL = "http://192.168.0.200:8080/HomeAutomationBackend/rest/";
	//public static final String SERVER_BASE_URL = "http://192.168.43.46:8080/HomeAutomationBackend/rest/";
	//public static final String SERVER_BASE_URL = "http://localhost:8080/HomeAutomationBackend/rest/";
	static String UNAUTHORIZED = "Unauthorized";
	String serverURL = SERVER_BASE_URL + "secure/sensor";
	public static final String USERNAME = "isEPa33h";
	public static final String SECRET = "mFSmoYIeZKm1oJFNOXQLyJVGFLWQrhatYeu6AkYxto2BKMWsrtfI8UmjtvjSrfUk5K+yCTWN8APxHjs9B7qZjrD0Jnn2a1UHtBmHrhJG571x6MUWWzMhUh2ga7PleZWUwoxa+8ssHGR17RfNOQUevAKdnCdTJT79nA5NztLrc35InzpMgt0ijhxeqDcGUqS3k5C/JJMTexx6LRGcfw0ZEEWgqoDRi4Hbzy8=";

	static String authorizationHeader;
	
	String failMessage = "";
	boolean failure = false;

	@Before
	public void init() {
		failMessage = "";
		failure = false;
	}

	@Test
	void test1Authenticate() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			con.disconnect();

			if (status == 401) {
				failMessage = UNAUTHORIZED;
				failure = true;
			}
			if (failure)
				fail(failMessage);
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void test2ReplayAttack() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			int status = con.getResponseCode();
			con.disconnect();

			if (status == 401) {
				failMessage = UNAUTHORIZED;
				failure = true;
			}
			else {
				HttpURLConnection con1 = (HttpURLConnection) url.openConnection();
				con1.setRequestMethod("GET");
				con1.setRequestProperty("Content-Type", "application/json");
				con1.setRequestProperty("Email", AuthenticationTest.USERNAME);
				con1.setRequestProperty("Authorization", authorizationHeader);
				status = con1.getResponseCode();
				if(status != 401 && status != 500)
					fail("Replay attack successful");
			}

			if (failure)
				fail(failMessage);
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void test3DelayedInTheTimeframe() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("GET");
			Thread.sleep(12000);
			int status = con.getResponseCode();
			con.disconnect();

			if (status == 401) {
				failMessage = UNAUTHORIZED;
				failure = true;
			}
			if (failure)
				fail(failMessage);
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void test4Timeout() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("GET");
			Thread.sleep(20000);
			int status = con.getResponseCode();
			con.disconnect();

			if (status != 401) {
				failMessage = "The Request should not be authorized";
				failure = true;
			}
			if (failure)
				fail(failMessage);
		} catch(Exception e) {
			fail(e.getMessage());
		}
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
			byte[] input = new byte[nonce.length + Base64.decodeBase64(secret).length + (username + time).getBytes().length];
			System.arraycopy(nonce, 0, input, 0, nonce.length);
			System.arraycopy(Base64.decodeBase64(secret), 0, input, nonce.length, Base64.decodeBase64(secret).length);
			System.arraycopy((username + time).getBytes(), 0, input, nonce.length + Base64.decodeBase64(secret).length, (username + time).getBytes().length);
			
			byte[] hash = digest.digest(input);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Email", username);
			authorizationHeader = "Digest " + Base64.encodeBase64String(hash);
			con.setRequestProperty("Authorization", authorizationHeader);
		}
		return con;
	}
}
