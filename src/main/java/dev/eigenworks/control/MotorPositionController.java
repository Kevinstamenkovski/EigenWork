package dev.eigenworks.control;

import dev.eigenworks.mechanical.MotorAssembly;

/** Closed-loop joint-position controller driving the normalized H-bridge command. */
public final class MotorPositionController {
	public static final double SAMPLE_PERIOD_SECONDS = 0.01;
	private final PidController pid = new PidController(2.4, 0.7, 0.16,
			SAMPLE_PERIOD_SECONDS, -1.0, 1.0, 0.04);
	private double targetRadians = Math.PI / 2.0;

	public PidSnapshot update(MotorAssembly assembly) {
		PidSnapshot result = pid.update(targetRadians, assembly.outputAngle());
		assembly.driver().setCommand(result.output());
		return result;
	}
	public double targetRadians() { return targetRadians; }
	public void setTargetRadians(double value) {
		if (!Double.isFinite(value) || value < -Math.PI || value > Math.PI) throw new IllegalArgumentException("Target angle must be finite and within +/- pi radians");
		targetRadians = value;
	}
	public PidSnapshot snapshot() { return pid.snapshot(); }
	public void reset() { pid.reset(); }
}
