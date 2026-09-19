package dev.eigenworks.control;

/** Constant scalar gain. */
public record GainBlock(double gain) {
	public GainBlock { ControlMath.finite(gain, "Gain"); }
	public double apply(double input) { return ControlMath.finite(gain * ControlMath.finite(input, "Input"), "Gain output"); }
}
