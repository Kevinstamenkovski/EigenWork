package dev.eigenworks.signal;

/** Finite floating-point engineering value. */
public record ScalarSignal(double value) implements SignalValue {
	public ScalarSignal {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Scalar signal must be finite");
		}
	}
}

