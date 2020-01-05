package homeautomation.backend.entity;

import java.io.Serializable;
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
@Table(name = "SENSOR_PROPERTY")
public class SensorProperty implements Serializable {

	private static final long serialVersionUID = 1L;

	public SensorProperty() {
		super();
	}

	public SensorProperty(Sensor sensor, String description, String value) {
		super();
		this.sensor = sensor;
		this.description = description;
		this.value = value;
	}

	public SensorProperty(long id, Sensor sensor, String description, String value) {
		this(sensor, description, value);
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PK_ID")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FK_SENSOR_SERIAL_NUMBER", nullable = false)
	private Sensor sensor;

	@Column(name = "DESCRIPTION", nullable = false)
	private String description;

	@Column(name = "VALUE", nullable = false)
	private String value;

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public static JSONObject ConvertToJsonObject(SensorProperty sensorProperty) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sensor", Sensor.ConvertToJsonObject(sensorProperty.getSensor()));
		jsonObject.put("description", sensorProperty.getDescription());
		jsonObject.put("value", sensorProperty.getValue());
		jsonObject.put("id", sensorProperty.getId());

		return jsonObject;
	}

	public static JSONArray ConvertToJsonArray(List<SensorProperty> sensorProperty) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < sensorProperty.size(); i++) {
			JSONObject obj = ConvertToJsonObject(sensorProperty.get(i));
			jsonArray.put(obj);
		}
		return jsonArray;
	}

	public static SensorProperty ConvertFromJsonObject(JSONObject obj) throws JSONException {
		return new SensorProperty(obj.getLong("id"), Sensor.ConvertFromJsonObject((JSONObject) obj.get("sensor")),
				obj.getString("description"), obj.getString("value"));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SensorProperty))
			return false;
		SensorProperty sp = (SensorProperty) obj;
		if (this.id == sp.id && this.description.equals(sp.description)
				&& this.sensor.equals(sp.sensor) && this.value.equals(sp.value))
			return true;
		return false;
	}
}
