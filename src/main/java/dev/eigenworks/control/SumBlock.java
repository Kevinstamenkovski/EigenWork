package dev.eigenworks.control;

import java.util.Arrays;

/** Weighted summing junction, including subtracting inputs through negative weights. */
public final class SumBlock {
	private final double[] weights;

	public SumBlock(double... weights) {
		if (weights.length == 0) throw new IllegalArgumentException("A sum block requires at least one input");
		this.weights = Arrays.copyOf(weights, weights.length);
		for (double weight : this.weights) ControlMath.finite(weight, "Weight");
	}

	public double apply(double... inputs) {
		if (inputs.length != weights.length) throw new IllegalArgumentException("Input count does not match weight count");
		double result = 0.0;
		for (int index = 0; index < inputs.length; index++) result += weights[index] * ControlMath.finite(inputs[index], "Input");
		return ControlMath.finite(result, "Sum output");
	}
}
