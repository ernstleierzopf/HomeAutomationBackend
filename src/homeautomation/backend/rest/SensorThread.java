package homeautomation.backend.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.log4j.Logger;
import homeautomation.backend.entity.Sensor;
import homeautomation.backend.entity.SensorMeasurement;

public class SensorThread implements Runnable {
	private static final String PYTHON_PATH = "/usr/bin/python";
	private static final String ADAFRUIT_PATH = "/opt/tomcat/latest/webapps/HomeAutomationBackend/WEB-INF/classes/homeautomation/backend/rest/AdafruitDHT.py";
	private static String serialNumberHum = "DHT22Hum";
	private static String serialNumberTemp = "DHT22Temp";
	private static String serialNumberCPU = "CPU Temp";
	private static Sensor sensorHum = null;
	private static Sensor sensorTemp = null;
	private static Sensor sensorCPU = null;

	@Override
	public void run() {
		try {
			EntityManager em = null;
			if (AuthenticationService.EMF == null)
				AuthenticationService.EMF = Persistence.createEntityManagerFactory("HomeAutomationPU");
			if (em == null)
				em = AuthenticationService.EMF.createEntityManager();

			TypedQuery<Sensor> query = em.createQuery("SELECT u FROM Sensor u where u.serialNumber = :serialNumber",
					Sensor.class);
			query.setParameter("serialNumber", serialNumberHum);
			List<Sensor> sensors = query.getResultList();
			if (sensors.size() > 0) {
				sensorHum = sensors.get(0);
			}

			query = em.createQuery("SELECT u FROM Sensor u where u.serialNumber = :serialNumber", Sensor.class);
			query.setParameter("serialNumber", serialNumberTemp);
			sensors = query.getResultList();
			if (sensors.size() > 0) {
				sensorTemp = sensors.get(0);
			}

			query = em.createQuery("SELECT u FROM Sensor u where u.serialNumber = :serialNumber", Sensor.class);
			query.setParameter("serialNumber", serialNumberCPU);
			sensors = query.getResultList();
			if (sensors.size() > 0) {
				sensorCPU = sensors.get(0);
			}

			while (true) {

				StringBuilder sb = new StringBuilder();
				ProcessBuilder pb = new ProcessBuilder(PYTHON_PATH, ADAFRUIT_PATH, "h");
				Process p = pb.start();
				p.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				SensorMeasurement smHum = new SensorMeasurement(sensorHum, new Timestamp(System.currentTimeMillis()),
						Double.parseDouble(sb.toString()));
				em.getTransaction().begin();
				em.persist(smHum);
				em.getTransaction().commit();

				sb = new StringBuilder();
				pb = new ProcessBuilder(PYTHON_PATH, ADAFRUIT_PATH, "t");
				p = pb.start();
				p.waitFor();
				reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				SensorMeasurement smTemp = new SensorMeasurement(sensorTemp, new Timestamp(System.currentTimeMillis()),
						Double.parseDouble(sb.toString()));
				em.getTransaction().begin();
				em.persist(smTemp);
				em.getTransaction().commit();

				sb = new StringBuilder();
				pb = new ProcessBuilder("/bin/sh", "-c", "/opt/vc/bin/vcgencmd measure_temp| /bin/sed s/[^0-9.]//g");
				p = pb.start();
				p.waitFor();
				reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				SensorMeasurement smCPU = new SensorMeasurement(sensorCPU, new Timestamp(System.currentTimeMillis()),
						Double.parseDouble(sb.toString()));
				em.getTransaction().begin();
				em.persist(smCPU);
				em.getTransaction().commit();

				Thread.sleep(1800000);
			}
		} catch (InterruptedException |

				IOException e) {
			Logger.getLogger(RestAuthenticationFilter.LOGGER)
					.info("Exception at" + this.getClass() + ": " + e.getMessage());
		}
	}

}
