package dev.eigenworks.electrical;

/** Backward-Euler series-RL transient model. */
public final class RlTransient {
	private final double resistance, inductance;
	private double current;
	public RlTransient(double resistance, double inductance, double initialCurrent) {
		if (!finitePositive(resistance) || !finitePositive(inductance) || !Double.isFinite(initialCurrent)) throw new IllegalArgumentException("RL parameters must be finite and positive");
		this.resistance = resistance; this.inductance = inductance; current = initialCurrent;
	}
	public double step(double sourceVoltage, double timestepSeconds) {
		if (!Double.isFinite(sourceVoltage) || !finitePositive(timestepSeconds)) throw new IllegalArgumentException("RL input and timestep must be valid");
		current = (current + timestepSeconds * sourceVoltage / inductance) / (1 + timestepSeconds * resistance / inductance);
		if (!Double.isFinite(current)) throw new ArithmeticException("RL NUMERICAL INSTABILITY");
		return current;
	}
	public double current() { return current; }
	private static boolean finitePositive(double value) { return Double.isFinite(value) && value > 0; }
}
