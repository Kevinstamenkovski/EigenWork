package dev.eigenworks.control;

/**
 * Protected discrete PID controller. Derivative-on-measurement avoids setpoint kick,
 * a first-order derivative filter attenuates noise, and conditional integration
 * prevents windup while the output is saturated.
 */
public final class PidController {
	private final double kp;
	private final double ki;
	private final double kd;
	private final double samplePeriod;
	private final double derivativeFilterTime;
	private final SaturationBlock outputLimits;
	private boolean initialized;
	private double previousMeasurement;
	private double filteredMeasurementDerivative;
	private double integral;
	private PidSnapshot snapshot = PidSnapshot.zero();

	public PidController(double kp, double ki, double kd, double samplePeriod,
			double outputMinimum, double outputMaximum, double derivativeFilterTime) {
		this.kp = ControlMath.finite(kp, "Kp");
		this.ki = ControlMath.finite(ki, "Ki");
		this.kd = ControlMath.finite(kd, "Kd");
		this.samplePeriod = ControlMath.positive(samplePeriod, "Sample period");
		this.derivativeFilterTime = ControlMath.finite(derivativeFilterTime, "Derivative filter time");
		if (derivativeFilterTime < 0.0) throw new IllegalArgumentException("Derivative filter time cannot be negative");
		outputLimits = new SaturationBlock(outputMinimum, outputMaximum);
	}

	public PidSnapshot update(double reference, double measurement) {
		reference = ControlMath.finite(reference, "Reference");
		measurement = ControlMath.finite(measurement, "Measurement");
		double error = reference - measurement;
		double measurementDerivative = initialized ? (measurement - previousMeasurement) / samplePeriod : 0.0;
		double alpha = derivativeFilterTime / (derivativeFilterTime + samplePeriod);
		filteredMeasurementDerivative = alpha * filteredMeasurementDerivative + (1.0 - alpha) * measurementDerivative;
		previousMeasurement = measurement;
		initialized = true;

		double proportional = kp * error;
		double derivative = -kd * filteredMeasurementDerivative;
		double candidateIntegral = integral + ki * error * samplePeriod;
		double candidate = proportional + candidateIntegral + derivative;
		double limitedCandidate = outputLimits.apply(candidate);
		boolean drivesFurtherIntoSaturation = candidate != limitedCandidate
				&& ((candidate > limitedCandidate && error > 0.0) || (candidate < limitedCandidate && error < 0.0));
		if (!drivesFurtherIntoSaturation) integral = candidateIntegral;
		double unconstrained = proportional + integral + derivative;
		double output = outputLimits.apply(unconstrained);
		snapshot = new PidSnapshot(reference, measurement, error, proportional, integral, derivative,
				output, output != unconstrained);
		return snapshot;
	}

	public PidSnapshot snapshot() { return snapshot; }
	public double samplePeriod() { return samplePeriod; }
	public void reset() {
		initialized = false;
		previousMeasurement = 0.0;
		filteredMeasurementDerivative = 0.0;
		integral = 0.0;
		snapshot = PidSnapshot.zero();
	}
}
