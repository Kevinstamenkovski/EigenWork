package dev.eigenworks.electrical;

/** Backward-Euler series-RC transient model, stable for positive timesteps. */
public final class RcTransient {
	private final double resistance, capacitance;
	private double capacitorVoltage;
	public RcTransient(double resistance, double capacitance, double initialVoltage) {
		if (!finitePositive(resistance) || !finitePositive(capacitance) || !Double.isFinite(initialVoltage)) throw new IllegalArgumentException("RC parameters must be finite and positive");
		this.resistance = resistance; this.capacitance = capacitance; capacitorVoltage = initialVoltage;
	}
	public double step(double sourceVoltage, double timestepSeconds) {
		if (!Double.isFinite(sourceVoltage) || !finitePositive(timestepSeconds)) throw new IllegalArgumentException("RC input and timestep must be valid");
		double alpha = timestepSeconds / (resistance * capacitance);
		capacitorVoltage = (capacitorVoltage + alpha * sourceVoltage) / (1 + alpha);
		if (!Double.isFinite(capacitorVoltage)) throw new ArithmeticException("RC NUMERICAL INSTABILITY");
		return capacitorVoltage;
	}
	public double voltage() { return capacitorVoltage; }
	public double current(double sourceVoltage) { if (!Double.isFinite(sourceVoltage)) throw new IllegalArgumentException("Voltage must be finite"); return (sourceVoltage - capacitorVoltage) / resistance; }
	private static boolean finitePositive(double value) { return Double.isFinite(value) && value > 0; }
}
