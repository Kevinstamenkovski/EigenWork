package dev.eigenworks.config;

/** Immutable definition for one finite, bounded, step-editable engineering parameter. */
public record EngineeringParameter(String key, String label, double minimum, double maximum,
		double step, double defaultValue, String unit) {
	public EngineeringParameter {
		if (key == null || key.isBlank() || label == null || label.isBlank() || unit == null) throw new IllegalArgumentException("Parameter metadata is required");
		if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum >= maximum) throw new IllegalArgumentException("Parameter range is invalid");
		if (!Double.isFinite(step) || step <= 0 || !Double.isFinite(defaultValue) || defaultValue < minimum || defaultValue > maximum) throw new IllegalArgumentException("Parameter step/default is invalid");
	}
	public double requireValid(double value) {
		if (!Double.isFinite(value) || value < minimum || value > maximum) throw new IllegalArgumentException(label + " must be within [" + minimum + ", " + maximum + "] " + unit);
		return value;
	}
}
