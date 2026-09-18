package dev.eigenworks.simulation;

import java.util.Objects;

/** A bounded diagnostic emitted instead of allowing one malformed device to crash the server. */
public record SimulationFault(String deviceId, String code, String message) {
	public SimulationFault {
		deviceId = requireText(deviceId, "deviceId");
		code = requireText(code, "code");
		message = Objects.requireNonNullElse(message, "");
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " cannot be blank");
		}
		return value;
	}
}

