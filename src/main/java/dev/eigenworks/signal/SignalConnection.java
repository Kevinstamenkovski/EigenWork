package dev.eigenworks.signal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Ordered point-to-point signal connection with deterministic simulated latency. */
public final class SignalConnection<T extends SignalValue> {
	private final long latencyMicros;
	private final SignalSink<T> sink;
	private final Deque<PendingSample<T>> pending = new ArrayDeque<>();
	private long lastTimestampMicros = -1L;

	public SignalConnection(long latencyMicros, SignalSink<T> sink) {
		if (latencyMicros < 0) {
			throw new IllegalArgumentException("Signal latency cannot be negative");
		}
		this.latencyMicros = latencyMicros;
		this.sink = Objects.requireNonNull(sink, "sink");
	}

	public void transmit(SignalSample<T> sample) {
		Objects.requireNonNull(sample, "sample");
		if (sample.timestampMicros() < lastTimestampMicros) {
			throw new IllegalArgumentException("Signal samples must be transmitted in timestamp order");
		}
		long deliveryTime = Math.addExact(sample.timestampMicros(), latencyMicros);
		pending.addLast(new PendingSample<>(deliveryTime, sample));
		lastTimestampMicros = sample.timestampMicros();
	}

	public int deliverThrough(long simulationTimeMicros) {
		if (simulationTimeMicros < 0) {
			throw new IllegalArgumentException("Delivery time cannot be negative");
		}
		int delivered = 0;
		while (!pending.isEmpty() && pending.peekFirst().deliveryTimeMicros() <= simulationTimeMicros) {
			sink.accept(pending.removeFirst().sample());
			delivered++;
		}
		return delivered;
	}

	public int pendingCount() {
		return pending.size();
	}

	public long latencyMicros() {
		return latencyMicros;
	}

	private record PendingSample<T extends SignalValue>(long deliveryTimeMicros, SignalSample<T> sample) {
	}
}

