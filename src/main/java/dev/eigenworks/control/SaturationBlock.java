package dev.eigenworks.control;

/** Inclusive lower/upper output clamp. */
public record SaturationBlock(double minimum, double maximum) {
	public SaturationBlock {
		ControlMath.finite(minimum, "Minimum");
		ControlMath.finite(maximum, "Maximum");
		if (minimum >= maximum) throw new IllegalArgumentException("Minimum must be below maximum");
	}
	public double apply(double input) { return Math.clamp(ControlMath.finite(input, "Input"), minimum, maximum); }
}
