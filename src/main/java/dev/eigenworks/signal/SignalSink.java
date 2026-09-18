package dev.eigenworks.signal;

/** Receives immutable engineering samples after connection latency. */
@FunctionalInterface
public interface SignalSink<T extends SignalValue> {
	void accept(SignalSample<T> sample);
}

