package dev.eigenworks.digital.world;

import dev.eigenworks.digital.DigitalPortDirection;

/** Named, width-checked port exposed by a world digital device. */
public record DigitalPortSpec(String name, int width, DigitalPortDirection direction) {
	public DigitalPortSpec {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Port name cannot be blank");
		}
		if (width < 1 || width > Long.SIZE) {
			throw new IllegalArgumentException("Port width must be between 1 and 64");
		}
		if (direction == null) {
			throw new NullPointerException("direction");
		}
	}
}
