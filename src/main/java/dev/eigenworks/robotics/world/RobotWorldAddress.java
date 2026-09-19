package dev.eigenworks.robotics.world;

/** Stable loaded-world address without retaining a level reference. */
public record RobotWorldAddress(String dimension, long packedPosition) {
	public RobotWorldAddress {
		if (dimension == null || dimension.isBlank()) throw new IllegalArgumentException("Robot dimension is required");
	}
}
