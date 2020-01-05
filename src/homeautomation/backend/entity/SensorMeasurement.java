package homeautomation.backend.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
@XmlRootElement
@Table(name="SENSOR_MEASUREMENT")
public class SensorMeasurement implements Serializable {
	
	private static final long serialVersionUID = 1L;
	static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public SensorMeasurement() {
		super();
	}

	public SensorMeasurement(Sensor sensor, long value) {
		this(sensor, new Timestamp(System.currentTimeMillis()), value);
	}

	public SensorMeasurement(Sensor sensor, Timestamp time, double value) {
		super();
		this.sensor = sensor;
		this.time = time;
		this.value = value;
	}
	
	public SensorMeasurement(long id, Sensor sensor, Timestamp time, double value) {
		this(sensor, time, value);
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PK_ID")
	private long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FK_SENSOR_SERIAL_NUMBER", nullable=false)
	private Sensor sensor;
	
	@Column(name="TIME", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Timestamp time;
	
	@Column(name="VALUE", nullable=false)
	private double value;

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public long getId() {
		return id;
	}
	
	public static JSONObject ConvertToJsonObject(SensorMeasurement sensorMeasurement) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", sensorMeasurement.getId());
		jsonObject.put("sensor", Sensor.ConvertToJsonObject(sensorMeasurement.getSensor()));
		jsonObject.put("value", sensorMeasurement.getValue());
		jsonObject.put("time", sensorMeasurement.getTime().getTime());

		return jsonObject;
	}
	
	public static JSONArray ConvertToJsonArray(List<SensorMeasurement> sensorMeasurement) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < sensorMeasurement.size(); i++) {
			JSONObject obj = ConvertToJsonObject(sensorMeasurement.get(i));
			jsonArray.put(obj);
		}
		return jsonArray;
	}
	
	public static SensorMeasurement ConvertFromJsonObject(JSONObject obj) throws JSONException, ParseException {
		return new SensorMeasurement(obj.getLong("id"), Sensor.ConvertFromJsonObject((JSONObject) obj.get("sensor")),
				new Timestamp(obj.getLong("time")), obj.getDouble("value"));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SensorMeasurement))
			return false;
		SensorMeasurement sm = (SensorMeasurement) obj;
		if (this.id == sm.id && this.time.equals(sm.time)
				&& this.sensor.equals(sm.sensor) && this.value == sm.value)
			return true;
		return false;
	}
}