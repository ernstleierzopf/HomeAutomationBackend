package homeautomation.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
@XmlRootElement
@Table(name="SENSOR")
public class Sensor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Sensor() {
		super();
	}

	public Sensor(String serialNumber, int type, String unitOfMeasurement) {
		super();
		setSerialNumber(serialNumber);
		setType(SensorType.values()[type]);
		setUnitOfMeasurement(unitOfMeasurement);
	}
	
	public Sensor(String serialNumber, SensorType type, String unitOfMeasurement) {
		super();
		setSerialNumber(serialNumber);
		setType(type);
		setUnitOfMeasurement(unitOfMeasurement);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="PK_SERIAL_NUMBER", length=30)
	private String serialNumber; //vielleicht string besser
	
	@Column(name="TYPE", nullable=false)
	private SensorType type; //enum erstellen, Welche Art von Sensor es ist
	
	@Column(name="UNIT_OF_MEASUREMENT", nullable=false)
	private String unitOfMeasurement;
		
	public SensorType getType() {
		return type;
	}

	public void setType(SensorType type) {
		if(type.ordinal() >= 0 && type.ordinal() < SensorType.values().length)
			this.type = type;
		else
			throw new IndexOutOfBoundsException("Sensor type must be between 0 and " + (SensorType.values().length-1));
	}

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public static JSONObject ConvertToJsonObject(Sensor sensor) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("serialNumber", sensor.getSerialNumber());
		jsonObject.put("type", sensor.getType().ordinal());
		jsonObject.put("unitOfMeasurement", sensor.getUnitOfMeasurement());

		return jsonObject;
	}
	
	public static JSONArray ConvertToJsonArray(List<Sensor> sensors) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < sensors.size(); i++) {
			JSONObject obj = ConvertToJsonObject(sensors.get(i));
			jsonArray.put(obj);
		}
		return jsonArray;
	}
	
	public static Sensor ConvertFromJsonObject(JSONObject obj) throws JSONException {
		return new Sensor(obj.getString("serialNumber"), obj.getInt("type"), obj.getString("unitOfMeasurement"));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Sensor))
			return false;
		Sensor s = (Sensor) obj;
		if (this.serialNumber.equals(s.getSerialNumber()) && this.type.equals(s.getType())
				&& this.unitOfMeasurement.equals(s.getUnitOfMeasurement()))
			return true;
		return false;
	}

}
