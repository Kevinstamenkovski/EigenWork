package dev.eigenworks.control;

/** Shared validation helpers for deterministic control-system blocks. */
final class ControlMath {
	private ControlMath() { }

	static double finite(double value, String name) {
		if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
		return value;
	}

	static double positive(double value, String name) {
		finite(value, name);
		if (value <= 0.0) throw new IllegalArgumentException(name + " must be positive");
		return value;
	}
}
