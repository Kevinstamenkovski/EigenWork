package dev.eigenworks.digital.world;

/** A device input and its optional persisted source. */
public record DigitalInputBinding(DigitalPortSpec input, DigitalSourceEndpoint source) {
	public DigitalInputBinding {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (input.direction() != dev.eigenworks.digital.DigitalPortDirection.INPUT) {
			throw new IllegalArgumentException("Binding port must be an input");
		}
		if (source != null && source.width() != input.width()) {
			throw new IllegalArgumentException("Digital binding width mismatch");
		}
	}

	public boolean connected() {
		return source != null;
	}
}
