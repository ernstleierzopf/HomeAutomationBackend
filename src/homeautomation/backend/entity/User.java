package homeautomation.backend.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
@XmlRootElement
@Table(name="USER")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public User() {
		super();
	}

	public User(String email, String secret, byte[] nonce) {
		super();
		this.email = email;
		this.secret = secret;
		this.nonce = nonce;
		this.activated = false;
	}

	@Id
	@Column(name="PK_EMAIL", length=30)
	private String email;
	
	@Column(name="SECRET", nullable=false, length=256)
	private String secret;
	
	@Column(name="NONCE", length=200)
	private byte[] nonce;

	@Column(name="NONCE_CREATION_DATE")
	private Timestamp nonceCreationDate;
	
	@Column(name="ACTIVATED")
	private boolean activated;
	
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public void setNonce(byte[] nonce) {
		this.nonce = nonce;
	}

	public String getEmail() {
		return email;
	}

	public Timestamp getNonceCreationDate() {
		return nonceCreationDate;
	}

	public void setNonceCreationDate(Timestamp nonceCreationDate) {
		this.nonceCreationDate = nonceCreationDate;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActivated() {
		return activated;
	}
	
	public static JSONObject ConvertToJsonObject(User user) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("email", user.getEmail());
		jsonObject.put("secret", user.getSecret());

		return jsonObject;
	}
	
	public static User ConvertFromJsonObject(JSONObject obj) throws JSONException {
		return new User(obj.getString("email"), obj.getString("secret"), null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof User))
			return false;
		User u = (User) obj;
		if (this.email.equals(u.email) && this.secret.equals(u.secret)
				&& this.nonce.equals(u.nonce) && this.activated == u.activated)
			return true;
		return false;
	}
}
