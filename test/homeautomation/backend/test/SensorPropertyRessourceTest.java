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

import homeautomation.backend.entity.Sensor;
import homeautomation.backend.entity.SensorProperty;
import homeautomation.backend.entity.SensorType;

class SensorPropertyRessourceTest {

	static String WRONG_STATUS_CODE = "Wrong status code!";
	static String WRONG_OBJECT = "Response entity does not contain the right SensorProperty object.";
	static String serverURL = AuthenticationTest.SERVER_BASE_URL + "secure/sensorProperty";
	static Sensor s4 = new Sensor("1L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s5 = new Sensor("2L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s6 = new Sensor("3L", SensorType.TEMPERATURE, "°C");
	static SensorProperty sp1 = new SensorProperty(1L, s4, "Empfindlichkeit", "30");
	static SensorProperty sp2 = new SensorProperty(2L, s4, "Messdauer", "1ms");
	static SensorProperty sp3 = new SensorProperty(3L, s6, "Empfindlichkeit", "30");

	static String failMessage = "";
	static boolean failure = false;

	@BeforeAll
	public static void setupInitialData() {
		try {
			SensorRessourceTest.postSensor(s4);
			SensorRessourceTest.postSensor(s5);
			postSensorProperty(sp2);
		} catch (NoSuchAlgorithmException | IOException | JSONException e) {
			System.out.println("Could not create initial Data");
		}
	}

	@AfterAll
	public static void deleteInitialData() {
		try {
			deleteSensorProperty(sp2.getId());
			SensorRessourceTest.deleteSensor(s4.getSerialNumber());
			SensorRessourceTest.deleteSensor(s5.getSerialNumber());
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
	void test1GetSensorPropertiesFromSensor() {
		try {
			URL url = new URL(serverURL + "/" + s4.getSerialNumber());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();

			if (status != 200 && status != 404) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}
			if (status == 200) {
				String response = SensorRessourceTest.readResponse(con);
				if (!response.contains(SensorProperty.ConvertToJsonObject(sp1).toString())
						&& !response.contains(SensorProperty.ConvertToJsonObject(sp2).toString())) {
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
	void test2CreateSensorProperty() {
		try {
			// Post SensorProperty
			int status = postSensorProperty(sp1);
			if (status != 201) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}

			// Get SensorProperty and compare
			checkSensorProperty(sp1.getId(), sp1);

			// Cleanup
			deleteSensorProperty(sp1.getId());

			// Sensor does not exist in the database
			status = postSensorProperty(sp3);
			if (status != 409) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}

			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void test3UpdateSensorProperty() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("PUT");
			con.setDoOutput(true);

			OutputStream os = con.getOutputStream();
			os.write(SensorProperty.ConvertToJsonObject(sp1).toString().getBytes("UTF-8"));
			os.close();

			int status = con.getResponseCode();
			if (status != 201 && status != 204) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			} else if (status == 201) {
				JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
				SensorProperty r = SensorProperty.ConvertFromJsonObject(response);
				if (!r.equals(sp1)) {
					failMessage = WRONG_OBJECT;
					failure = true;
				}
			}

			con.disconnect();

			// Get Sensor and compare
			checkSensorProperty(sp1.getId(), sp1);

			// Cleanup
			deleteSensorProperty(sp1.getId());
			
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void test4DeleteSensor() {
		try {
			int status = SensorRessourceTest.postSensor(s6);
			postSensorProperty(sp3);
			status = SensorRessourceTest.deleteSensor(s6.getSerialNumber());
			if (status != 204) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}
			
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public static int postSensorProperty(SensorProperty sp) throws MalformedURLException, IOException,
			ProtocolException, UnsupportedEncodingException, JSONException, NoSuchAlgorithmException {
		URL url = new URL(serverURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(SensorProperty.ConvertToJsonObject(sp).toString().getBytes("UTF-8"));
		os.close();

		int status = con.getResponseCode();
		if (status != 201 && status != 409) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		} else if (status == 201) {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			SensorProperty r = SensorProperty.ConvertFromJsonObject(response);
			if (!r.equals(sp)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		return status;
	}

	public static int checkSensorProperty(long id, SensorProperty sp) throws MalformedURLException, IOException,
			ProtocolException, JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		URL url = new URL(serverURL + "/id/" + id);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		int status = con.getResponseCode();
		if (status != 200) {
			failMessage = "SensorProperty should exist in Database!";
			failure = true;
		} else {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			SensorProperty r = SensorProperty.ConvertFromJsonObject(response);
			if (!r.equals(sp)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static void deleteSensorProperty(long id)
			throws MalformedURLException, IOException, ProtocolException, NoSuchAlgorithmException {
		URL url;
		HttpURLConnection con;
		int status;
		url = new URL(serverURL + "/" + id);
		con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("DELETE");
		status = con.getResponseCode();
		if (status != 204) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		}
		con.disconnect();
	}
}
