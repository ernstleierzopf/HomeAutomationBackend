package homeautomation.backend.entity;

import org.codehaus.jackson.annotate.JsonValue;

public enum SensorType {
	TEMPERATURE, HUMIDITY, SOIL_MOISTURE, ULTRASONIC_RANGE, OPTICAL_PHOTOSENSITIVE_LIGHT, RADIO_RECEIVER;
	
	@JsonValue
	public int getValue() {
		return this.ordinal();
	}
}
