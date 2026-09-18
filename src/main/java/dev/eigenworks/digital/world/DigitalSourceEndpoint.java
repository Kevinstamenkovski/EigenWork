package dev.eigenworks.digital.world;

/** Persistable reference to a digital output port. */
public record DigitalSourceEndpoint(DigitalDeviceAddress device, String port, int width) {
	public DigitalSourceEndpoint {
		if (device == null) {
			throw new NullPointerException("device");
		}
		if (port == null || port.isBlank()) {
			throw new IllegalArgumentException("Output port cannot be blank");
		}
		if (width < 1 || width > Long.SIZE) {
			throw new IllegalArgumentException("Output width must be between 1 and 64");
		}
	}
}
