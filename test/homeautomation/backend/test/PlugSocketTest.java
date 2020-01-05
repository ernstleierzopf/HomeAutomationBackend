package homeautomation.backend.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import homeautomation.backend.entity.PlugSocket;

class PlugSocketTest {

	static String WRONG_STATUS_CODE = "Wrong status code!";
	static String WRONG_OBJECT = "Response entity does not contain the right PlugSocket object.";
	static String serverURL = AuthenticationTest.SERVER_BASE_URL + "secure/plugSocket";
	//static String serverURL = "http://localhost:8080/HomeAutomationBackend/rest/" + "secure/plugSocket";
//	static PlugSocket ps1 = new PlugSocket(1, false, "Pumpe");
//	static PlugSocket ps2 = new PlugSocket(2, false, "Ladegerät");
//	static PlugSocket ps3 = new PlugSocket(3, false, "Pumpe 2");
//	static PlugSocket ps4 = new PlugSocket(4, false, "Kompressor");
	static PlugSocket ps1 = new PlugSocket(5, false, "Pumpe");
	static PlugSocket ps2 = new PlugSocket(6, false, "Ladegerät");
	static PlugSocket ps3 = new PlugSocket(7, false, "Pumpe 2");
	static PlugSocket ps4 = new PlugSocket(8, false, "Kompressor");


	static String failMessage = "";
	static boolean failure = false;

	@BeforeAll
	public static void setupInitialData() {
		try {
			postPlugSocket(ps2);
			postPlugSocket(ps3);
			postPlugSocket(ps4);
		} catch (NoSuchAlgorithmException | IOException | JSONException e) {
			System.out.println("Could not create initial Data");
		}
	}

	@AfterAll
	public static void deleteInitialData() {
		try {
			deletePlugSocket(ps2.getPlugNumber());
			deletePlugSocket(ps3.getPlugNumber());
			deletePlugSocket(ps4.getPlugNumber());
		} catch (NoSuchAlgorithmException | IOException e) {
			System.out.println("Could not cleanup the database");
		}
	}

	@BeforeEach
	public void init() {
		failMessage = "";
		failure = false;
	}
	
	@Test
	void test1GetAllPlugSockets() {
		try {
			for (int i = 0; i < 2; i++) {
				URL url = new URL(serverURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
				con.setRequestMethod("GET");
				int status = con.getResponseCode();
				Thread.sleep(1000);
				String response = SensorRessourceTest.readResponse(con);
				con.disconnect();
				if (status != 200 && status != 404) {
					failMessage = WRONG_STATUS_CODE;
					failure = true;
				}
				if (!response.contains(PlugSocket.ConvertToJsonObject(ps2).toString())
						|| !response.contains(PlugSocket.ConvertToJsonObject(ps3).toString())) {
					failMessage = "Wrong response!";
					failure = true;
				}
			}
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	
	@Test
	void test2CreatePlugSocket() {
		try {
			// Post Sensor
			postPlugSocket(ps1);

			// Get Sensor and compare
			checkPlugSocket(ps1.getPlugNumber(), ps1);

			// Cleanup
			deletePlugSocket(ps1.getPlugNumber());

			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void test3UpdatePlugSocket() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("PUT");
			con.setDoOutput(true);

			OutputStream os = con.getOutputStream();
			os.write(PlugSocket.ConvertToJsonObject(ps1).toString().getBytes());
			os.close();

			int status = con.getResponseCode();
			if (status != 201 && status != 204) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			} else if (status == 201) {
				JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
				PlugSocket r = PlugSocket.ConvertFromJsonObject(response);
				if (!r.equals(ps1)) {
					failMessage = WRONG_OBJECT;
					failure = true;
				}
			}

			con.disconnect();

			// Get Sensor and compare
			checkPlugSocket(ps1.getPlugNumber(), ps1);

			// Cleanup
			deletePlugSocket(ps1.getPlugNumber());
			
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public static int postPlugSocket(PlugSocket ps)
			throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException, JSONException, NoSuchAlgorithmException {
		URL url = new URL(serverURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(PlugSocket.ConvertToJsonObject(ps).toString().getBytes());
		os.close();

		int status = con.getResponseCode();
		System.out.println(status);
		if (status != 201 && status != 409) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		} else if (status == 201) {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			PlugSocket r = PlugSocket.ConvertFromJsonObject(response);
			if (!r.equals(ps)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static int checkPlugSocket(int plugNumber, PlugSocket ps)
			throws MalformedURLException, IOException, ProtocolException, JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		URL url = new URL(serverURL + "/plugNumber/" + plugNumber);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		int status = con.getResponseCode();
		System.out.println(status);
		if (status != 200) {
			failMessage = "PlugSocket should exist in Database!";
			failure = true;
		} else {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			PlugSocket r = PlugSocket.ConvertFromJsonObject(response);
			if (!r.equals(ps)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static int deletePlugSocket(int plugNumber) throws MalformedURLException, IOException, ProtocolException, NoSuchAlgorithmException {
		URL url;
		HttpURLConnection con;
		int status;
		url = new URL(serverURL + "/" + plugNumber);
		con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("DELETE");
		status = con.getResponseCode();
		if (status != 204) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		}
		con.disconnect();
		return status;
	}

}
