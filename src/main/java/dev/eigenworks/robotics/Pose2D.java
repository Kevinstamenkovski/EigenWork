package dev.eigenworks.robotics;

/** Planar Cartesian pose in metres and radians. */
public record Pose2D(double xMeters, double yMeters, double angleRadians) {
	public Pose2D { if (!Double.isFinite(xMeters) || !Double.isFinite(yMeters) || !Double.isFinite(angleRadians)) throw new IllegalArgumentException("Pose must be finite"); }
}
