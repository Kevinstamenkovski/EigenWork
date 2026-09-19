package dev.eigenworks.instrumentation;

/** One finite engineering sample stored by bounded instrumentation histories. */
public record InstrumentSample(long timestampMicros, double value, boolean valid) {
	public InstrumentSample {
		if (timestampMicros < 0) throw new IllegalArgumentException("Sample timestamp cannot be negative");
		if (!Double.isFinite(value)) throw new IllegalArgumentException("Instrument sample must be finite");
	}
}
