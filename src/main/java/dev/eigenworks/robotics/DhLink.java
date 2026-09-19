package dev.eigenworks.robotics;

/** Standard revolute Denavit-Hartenberg link parameters in SI units and radians. */
public record DhLink(double aMeters, double alphaRadians, double dMeters, double thetaOffsetRadians) {
	public DhLink {
		if (!Double.isFinite(aMeters) || aMeters < 0 || aMeters > 64) throw new IllegalArgumentException("DH link length must be finite and in [0,64] m");
		if (!Double.isFinite(alphaRadians) || !Double.isFinite(dMeters) || !Double.isFinite(thetaOffsetRadians))
			throw new IllegalArgumentException("DH parameters must be finite");
		if (Math.abs(dMeters) > 64) throw new IllegalArgumentException("DH offset must be within 64 m");
	}
}
