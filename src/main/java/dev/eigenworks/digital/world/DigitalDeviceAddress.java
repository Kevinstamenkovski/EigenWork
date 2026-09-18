package dev.eigenworks.digital.world;

/** Stable address of a loaded or persisted digital block device. */
public record DigitalDeviceAddress(String dimension, long blockPosition) {
	public DigitalDeviceAddress {
		if (dimension == null || dimension.isBlank()) {
			throw new IllegalArgumentException("Digital device dimension cannot be blank");
		}
	}

	public String deviceId() {
		return dimension + ":" + blockPosition;
	}
}
