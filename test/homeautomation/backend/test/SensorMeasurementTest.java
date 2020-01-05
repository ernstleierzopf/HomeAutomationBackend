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
import java.sql.Timestamp;
import java.text.ParseException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import homeautomation.backend.entity.Sensor;
import homeautomation.backend.entity.SensorMeasurement;
import homeautomation.backend.entity.SensorType;

class SensorMeasurementTest {

	static String WRONG_STATUS_CODE = "Wrong status code!";
	static String WRONG_OBJECT = "Response entity does not contain the right SensorMeasurement object.";
	static String serverURL = AuthenticationTest.SERVER_BASE_URL + "secure/sensorMeasurement";
	static Sensor s4 = new Sensor("1L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s5 = new Sensor("2L", SensorType.ULTRASONIC_RANGE, "Meter");
	static Sensor s6 = new Sensor("3L", SensorType.TEMPERATURE, "°C");
	static long time = System.currentTimeMillis();
	static SensorMeasurement sm1 = new SensorMeasurement(1L, s4, new Timestamp(time - 10000), 30);
	static SensorMeasurement sm2 = new SensorMeasurement(2L, s4, new Timestamp(time), 31.5);
	static SensorMeasurement sm3 = new SensorMeasurement(3L, s6, new Timestamp(time), 31);
	static SensorMeasurement sm4 = new SensorMeasurement(4L, s5, new Timestamp(time), 31);

	static String failMessage = "";
	static boolean failure = false;

	@BeforeAll
	public static void setupInitialData() {
		try {
			SensorRessourceTest.postSensor(s4);
			SensorRessourceTest.postSensor(s5);
			postSensorMeasurement(sm2);
			postSensorMeasurement(sm4);
		} catch (NoSuchAlgorithmException | IOException | JSONException | ParseException e) {
			System.out.println("Could not create initial Data");
		}
	}

	@AfterAll
	public static void deleteInitialData() {
		try {
			deleteSensorMeasurement(sm2.getId());
			deleteSensorMeasurement(sm4.getId());
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
	void test1GetAllMeasurements() {
		try {
			URL url = new URL(serverURL);
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
				if (!response.contains(SensorMeasurement.ConvertToJsonObject(sm2).toString())
						|| !response.contains(SensorMeasurement.ConvertToJsonObject(sm4).toString())) {
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
	void test2GetAllMeasurementsFromOneSensor() {
		try {
			URL url = new URL(serverURL + "/sensorSerialNumber/" + s4.getSerialNumber());
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
				if (!response.contains(SensorMeasurement.ConvertToJsonObject(sm2).toString())
						|| response.contains(SensorMeasurement.ConvertToJsonObject(sm4).toString())) {
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
	void test3GetAllMeasurementsInTimestamp() {
		try {
			URL url = new URL(serverURL + "/timeSpan/" + 2000L);
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
				if (!response.contains(SensorMeasurement.ConvertToJsonObject(sm2).toString())
						|| !response.contains(SensorMeasurement.ConvertToJsonObject(sm4).toString())) {
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
	void test4CreateSensorMeasurement() {
		try {
			int status = postSensorMeasurement(sm1);
			if (status != 201) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}
			
			//Get SensorMeasurement and compare
			checkSensorMeasurement(sm1.getId(), sm1);
			
			//Cleanup
			deleteSensorMeasurement(sm1.getId());
			
			// Sensor does not exist in the database
			status = postSensorMeasurement(sm3);
			if (status != 409) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}

			if (failure)
				fail(failMessage);
		} catch (NoSuchAlgorithmException | IOException | JSONException | ParseException e) {
			fail(e.getMessage());
		}
	}


	@Test
	void test5DeleteSensor() {
		try {
			int status = SensorRessourceTest.postSensor(s6);
			status = SensorRessourceTest.deleteSensor(s6.getSerialNumber());
			if (status != 204) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}
			
			status = SensorRessourceTest.postSensor(s6);
			postSensorMeasurement(sm3);
			status = SensorRessourceTest.deleteSensor(s6.getSerialNumber());
			if (status != 409) {
				failMessage = WRONG_STATUS_CODE;
				failure = true;
			}
			
			//Cleanup
			deleteSensorMeasurement(sm3.getId());
			SensorRessourceTest.deleteSensor(s6.getSerialNumber());
			
			if (failure)
				fail(failMessage);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public static int postSensorMeasurement(SensorMeasurement sm) throws MalformedURLException, IOException,
			ProtocolException, UnsupportedEncodingException, JSONException, NoSuchAlgorithmException, ParseException {
		URL url = new URL(serverURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		con.setRequestMethod("POST");
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(SensorMeasurement.ConvertToJsonObject(sm).toString().getBytes("UTF-8"));
		os.close();

		int status = con.getResponseCode();
		if (status != 201 && status != 409) {
			failMessage = WRONG_STATUS_CODE;
			failure = true;
		} else if (status == 201) {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			SensorMeasurement r = SensorMeasurement.ConvertFromJsonObject(response);
			if (!r.equals(sm)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		return status;
	}

	public static int checkSensorMeasurement(long id, SensorMeasurement sm) throws MalformedURLException, IOException,
			ProtocolException, JSONException, UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
		URL url = new URL(serverURL + "/id/" + id);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con = AuthenticationTest.authenticateRequest(con, url, AuthenticationTest.USERNAME, AuthenticationTest.SECRET);
		int status = con.getResponseCode();
		if (status != 200) {
			failMessage = "SensorMeasurement should exist in Database!";
			failure = true;
		} else {
			JSONObject response = new JSONObject(SensorRessourceTest.readResponse(con));
			SensorMeasurement r = SensorMeasurement.ConvertFromJsonObject(response);
			if (!r.equals(sm)) {
				failMessage = WRONG_OBJECT;
				failure = true;
			}
		}
		con.disconnect();
		return status;
	}

	public static int deleteSensorMeasurement(Long id)
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
		return status;
	}

}
