package dev.eigenworks.networking.world;

import java.util.Objects;

/** Stable dimension and block-position address for a loaded CAN element. */
public record CanWorldAddress(String dimension, long packedPosition) {
	public CanWorldAddress {
		if (Objects.requireNonNull(dimension, "dimension").isBlank()) throw new IllegalArgumentException("CAN dimension is required");
	}
}
