package homeautomation.backend.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import homeautomation.backend.entity.SensorType;
import homeautomation.backend.test.AuthenticationTest;

class SensorRessourceTest {

	static String WRONG_STATUS_CODE = "Wrong status code!";
	static String WRONG_OBJECT = "Response entity does not contain the right Sensor object.";
	static String serverURL = AuthenticationTest.SERVER_BASE_URL + "secure/sensor";
	static Sensor s1 = new Sensor("123456789L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s2 = new Sensor("12345678L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s3 = new Sensor("1234567L", SensorType.TEMPERATURE, "°C");
	//static Sensor s4 = new Sensor("SN180179628Hum", SensorType.HUMIDITY, "%");
	//static Sensor s5 = new Sensor("SN180179628Temp", SensorType.TEMPERATURE, "°C");
	//static Sensor s6 = new Sensor("CPU", SensorType.TEMPERATURE, "°C");
	static Sensor s4 = new Sensor("1L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s5 = new Sensor("2L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s6 = new Sensor("3L", SensorType.TEMPERATURE, "°C");

	static String failMessage = "";
	static boolean failure = false;

	@BeforeAll
	public static void setupInitialData() {
		try {
			postSensor(s4);
			postSensor(s5);
			postSensor(s6);
		} catch (NoSuchAlgorithmException | IOException | JSONException e) {
			System.out.println("Could not create initial Data");
		}
	}
	
	@AfterAll
	public static void deleteInitialData() {
		try {
			deleteSensor(s4.getSerialNumber());
			deleteSensor(s5.getSerialNumber());
			deleteSensor(s6.getSerialNumber());
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
	void test1GetAllSensors() {
		try {
			for (int i = 0; i < 2; i++) {
				URL url = new URL(serverURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
				con.setRequestMethod("GET");
				int status = con.getResponseCode();
				Thread.sleep(1000);
				String response = readResponse(con);
				con.disconnect();
				if (status != 200 && status != 404) {
					failMessage = WRONG_STATUS_CODE;
					failure = true;
				}
				if (!response.contains(Sensor.ConvertToJsonObject(s4).toString())
						|| !response.contains(Sensor.ConvertToJsonObject(s5).toString())
						|| !response.contains(Sensor.ConvertToJsonObject(s6).toString())) {
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
	void test2CreateSensor() {
		try {
			// Post Sensor
			postSensor(s1);

			// Get Sensor and compare
			checkSensor(s1.getSerialNumber(), s1);

			// Cleanup
			deleteSensor(s1.getSerialNumber());

			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void test3UpdateSensor() {
		try {
			URL url = new URL(serverURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("PUT");
			con.setDoOutput(true);

			OutputStream os = con.getOutputStream();
			os.write(Sensor.ConvertToJsonObject(s1).toString().getBytes("UTF-8"));
			os.close();

			int status = con.getResponseCode();
			if (status != 201 && status != 204) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			} else if (status == 201) {
				JSONObject response = new JSONObject(readResponse(con));
				Sensor r = Sensor.ConvertFromJsonObject(response);
				if (!r.equals(s1)) {
					failMessage = WRONG_OBJECT;
					failure = true;
				}
			}

			con.disconnect();

			// Get Sensor and compare
			checkSensor(s1.getSerialNumber(), s1);

			// Cleanup
			deleteSensor(s1.getSerialNumber());
			
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void test4GetAllSensorsFromOneType() {
		try {
			// create Sensors
			postSensor(s1);
			postSensor(s2);
			postSensor(s3);

			URL url = new URL(serverURL + "/type/" + SensorType.ULTRASONIC_RANGE.ordinal());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			if (status != 200 && status != 404) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}

			String response = readResponse(con);
			con.disconnect();

			if (!response.contains(Sensor.ConvertToJsonObject(s1).toString())
					&& !response.contains(Sensor.ConvertToJsonObject(s2).toString())
					&& !response.contains(Sensor.ConvertToJsonObject(s3).toString())) {
				failMessage = "Wrong response!";
				failure = true;
			}

			// cleanup
			deleteSensor(s1.getSerialNumber());
			deleteSensor(s2.getSerialNumber());
			deleteSensor(s3.getSerialNumber());

			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public static String readResponse(HttpURLConnection con) throws UnsupportedEncodingException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	public static int postSensor(Sensor s)
			throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException, JSONException, NoSuchAlgorithmException {
		URL url = new URL(serverURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(Sensor.ConvertToJsonObject(s).toString().getBytes("UTF-8"));
		os.close();

		int status = con.getResponseCode();
		if (status != 201 && status != 409) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		} else if (status == 201) {
			JSONObject response = new JSONObject(readResponse(con));
			Sensor r = Sensor.ConvertFromJsonObject(response);
			if (!r.equals(s)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static int checkSensor(String serialNumber, Sensor s)
			throws MalformedURLException, IOException, ProtocolException, JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		URL url = new URL(serverURL + "/serialNumber/" + serialNumber);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		int status = con.getResponseCode();
		if (status != 200) {
			failMessage = "Sensor should exist in Database!";
			failure = true;
		} else {
			JSONObject response = new JSONObject(readResponse(con));
			Sensor r = Sensor.ConvertFromJsonObject(response);
			if (!r.equals(s)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static int deleteSensor(String serialNumber) throws MalformedURLException, IOException, ProtocolException, NoSuchAlgorithmException {
		URL url;
		HttpURLConnection con;
		int status;
		url = new URL(serverURL + "/" + serialNumber);
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
