package dev.eigenworks.control;

/** First-order filtered finite-difference derivative. */
public final class DerivativeBlock {
	private final double filterTimeConstant;
	private boolean initialized;
	private double previousInput;
	private double output;

	public DerivativeBlock(double filterTimeConstant) {
		this.filterTimeConstant = ControlMath.finite(filterTimeConstant, "Filter time constant");
		if (filterTimeConstant < 0.0) throw new IllegalArgumentException("Filter time constant cannot be negative");
	}

	public double update(double input, double deltaSeconds) {
		input = ControlMath.finite(input, "Input");
		deltaSeconds = ControlMath.positive(deltaSeconds, "Timestep");
		if (!initialized) {
			initialized = true;
			previousInput = input;
			return 0.0;
		}
		double raw = (input - previousInput) / deltaSeconds;
		double alpha = filterTimeConstant / (filterTimeConstant + deltaSeconds);
		output = alpha * output + (1.0 - alpha) * raw;
		previousInput = input;
		return ControlMath.finite(output, "Derivative output");
	}

	public void reset() { initialized = false; previousInput = 0.0; output = 0.0; }
}
