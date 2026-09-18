package dev.eigenworks.signal;

import java.util.Objects;

/** Immutable, timestamped value transported by an engineering connection. */
public record SignalSample<T extends SignalValue>(
		T value,
		long timestampMicros,
		String source,
		EngineeringUnit unit,
		long samplePeriodMicros,
		SignalValidity validity,
		boolean saturated,
		double noiseStandardDeviation) {

	public SignalSample {
		Objects.requireNonNull(value, "value");
		if (timestampMicros < 0) {
			throw new IllegalArgumentException("Signal timestamp cannot be negative");
		}
		if (source == null || source.isBlank()) {
			throw new IllegalArgumentException("Signal source cannot be blank");
		}
		Objects.requireNonNull(unit, "unit");
		if (samplePeriodMicros <= 0) {
			throw new IllegalArgumentException("Signal sample period must be positive");
		}
		Objects.requireNonNull(validity, "validity");
		if (!Double.isFinite(noiseStandardDeviation) || noiseStandardDeviation < 0.0) {
			throw new IllegalArgumentException("Noise standard deviation must be finite and non-negative");
		}
	}
}

