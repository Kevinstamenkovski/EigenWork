package dev.eigenworks.control;

/** Observable terms from one discrete PID update. */
public record PidSnapshot(double reference, double measurement, double error,
		double proportional, double integral, double derivative, double output, boolean saturated) {
	public static PidSnapshot zero() { return new PidSnapshot(0, 0, 0, 0, 0, 0, 0, false); }
}
