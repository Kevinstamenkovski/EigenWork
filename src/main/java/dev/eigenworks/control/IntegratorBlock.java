package dev.eigenworks.control;

/** Forward-Euler integrator with an explicit state clamp. */
public final class IntegratorBlock {
	private final SaturationBlock limits;
	private double state;

	public IntegratorBlock(double minimum, double maximum) { limits = new SaturationBlock(minimum, maximum); }
	public double update(double input, double deltaSeconds) {
		state = limits.apply(state + ControlMath.finite(input, "Input") * ControlMath.positive(deltaSeconds, "Timestep"));
		return state;
	}
	public double state() { return state; }
	public void reset(double value) { state = limits.apply(value); }
}
