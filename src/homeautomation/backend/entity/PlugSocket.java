package homeautomation.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
@XmlRootElement
@Table(name="PLUG_SOCKET")
public class PlugSocket implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public PlugSocket() {
		super();
	}
	
	public PlugSocket(int plugNumber, boolean status, String device) {
		super();
		this.plugNumber = plugNumber;
		this.status = status;
		this.device = device;
	}

	@Id
	@Column(name="PK_PLUG_NUMBER")
	private int plugNumber;
	
	@Column(name="STATUS", nullable=false)
	private boolean status;
	
	@Column(name="DEVICE")
	private String device;
	
	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getPlugNumber() {
		return plugNumber;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public static JSONObject ConvertToJsonObject(PlugSocket plugSocket) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("plugNumber", plugSocket.getPlugNumber());
		jsonObject.put("status", plugSocket.getStatus());
		jsonObject.put("device", plugSocket.getDevice());

		return jsonObject;
	}
	
	public static JSONArray ConvertToJsonArray(List<PlugSocket> plugSocket) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < plugSocket.size(); i++) {
			JSONObject obj = ConvertToJsonObject(plugSocket.get(i));
			jsonArray.put(obj);
		}
		return jsonArray;
	}
	
	public static PlugSocket ConvertFromJsonObject(JSONObject obj) throws JSONException {
		return new PlugSocket(obj.getInt("plugNumber"), (boolean) obj.get("status"), obj.getString("device"));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PlugSocket))
			return false;
		PlugSocket ps = (PlugSocket) obj;
		if (this.getPlugNumber() == ps.getPlugNumber() && this.getStatus() == ps.getStatus()
				&& this.getDevice().equals(ps.getDevice()))
			return true;
		return false;
	}
}
