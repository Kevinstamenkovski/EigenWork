package dev.eigenworks.robotics;

/** Rigid planar link; Minecraft scale is one block per metre. */
public record RobotLink(String name, double lengthMeters, double massKilograms) {
	public RobotLink {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Robot link name is required");
		if (!Double.isFinite(lengthMeters) || lengthMeters <= 0) throw new IllegalArgumentException("Link length must be positive and finite");
		if (!Double.isFinite(massKilograms) || massKilograms <= 0) throw new IllegalArgumentException("Link mass must be positive and finite");
	}
}
