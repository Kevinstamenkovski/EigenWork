package dev.eigenworks.digital;

/** Stable endpoint in an explicit digital network topology. */
public record DigitalPort(String deviceId, String portName, int width, DigitalPortDirection direction) {
	public DigitalPort {
		if (deviceId == null || deviceId.isBlank()) {
			throw new IllegalArgumentException("Digital port device id cannot be blank");
		}
		if (portName == null || portName.isBlank()) {
			throw new IllegalArgumentException("Digital port name cannot be blank");
		}
		if (width < 1 || width > Long.SIZE) {
			throw new IllegalArgumentException("Digital port width must be between 1 and 64");
		}
		if (direction == null) {
			throw new NullPointerException("direction");
		}
	}
}

