package dev.eigenworks.control;

import java.util.Arrays;

/** Fixed whole-sample transport delay with a deterministic circular buffer. */
public final class DelayBlock {
	private final double[] samples;
	private int next;

	public DelayBlock(int delaySamples, double initialValue) {
		if (delaySamples < 1) throw new IllegalArgumentException("Delay must be at least one sample");
		ControlMath.finite(initialValue, "Initial value");
		samples = new double[delaySamples];
		Arrays.fill(samples, initialValue);
	}

	public double update(double input) {
		input = ControlMath.finite(input, "Input");
		double output = samples[next];
		samples[next] = input;
		next = (next + 1) % samples.length;
		return output;
	}
}
