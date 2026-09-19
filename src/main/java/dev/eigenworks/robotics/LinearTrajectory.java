package dev.eigenworks.robotics;

/** Constant-velocity point-to-point joint interpolation. */
public record LinearTrajectory(double start, double end, double durationSeconds) {
	public LinearTrajectory {
		if (!Double.isFinite(start) || !Double.isFinite(end) || !Double.isFinite(durationSeconds) || durationSeconds <= 0) throw new IllegalArgumentException("Linear trajectory parameters are invalid");
	}
	public double position(double timeSeconds) { return start + (end - start) * Math.clamp(timeSeconds / durationSeconds, 0, 1); }
	public double velocity(double timeSeconds) { return timeSeconds > 0 && timeSeconds < durationSeconds ? (end - start) / durationSeconds : 0; }
}
