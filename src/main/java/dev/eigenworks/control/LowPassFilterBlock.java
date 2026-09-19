package dev.eigenworks.control;

/** Stable first-order low-pass filter using the exact RC discretization coefficient. */
public final class LowPassFilterBlock {
	private final double timeConstant;
	private boolean initialized;
	private double output;

	public LowPassFilterBlock(double timeConstant) { this.timeConstant = ControlMath.positive(timeConstant, "Time constant"); }
	public double update(double input, double deltaSeconds) {
		input = ControlMath.finite(input, "Input");
		deltaSeconds = ControlMath.positive(deltaSeconds, "Timestep");
		if (!initialized) { initialized = true; output = input; return output; }
		double alpha = 1.0 - Math.exp(-deltaSeconds / timeConstant);
		output += alpha * (input - output);
		return ControlMath.finite(output, "Filter output");
	}
	public double output() { return output; }
	public void reset(double value) { output = ControlMath.finite(value, "Reset value"); initialized = true; }
}
