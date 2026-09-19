package dev.eigenworks.robotics;

/** Inverse-kinematics result with explicit failure diagnostics. */
public record IkResult(boolean converged, double joint1, double joint2, int iterations, String diagnostic) {
	public IkResult {
		if (!Double.isFinite(joint1) || !Double.isFinite(joint2) || iterations < 0 || diagnostic == null) throw new IllegalArgumentException("Invalid IK result");
	}
}
