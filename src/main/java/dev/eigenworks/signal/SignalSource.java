package dev.eigenworks.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Fan-out source that owns explicit connections and never discovers the world by scanning. */
public final class SignalSource<T extends SignalValue> {
	private final List<SignalConnection<T>> connections = new ArrayList<>();
	private SignalSample<T> lastSample;

	public void connect(SignalConnection<T> connection) {
		connections.add(Objects.requireNonNull(connection, "connection"));
	}

	public boolean disconnect(SignalConnection<T> connection) {
		return connections.remove(connection);
	}

	public void publish(SignalSample<T> sample) {
		Objects.requireNonNull(sample, "sample");
		if (lastSample != null && sample.timestampMicros() < lastSample.timestampMicros()) {
			throw new IllegalArgumentException("Signal source cannot publish backwards in time");
		}
		lastSample = sample;
		for (SignalConnection<T> connection : List.copyOf(connections)) {
			connection.transmit(sample);
		}
	}

	public int deliverThrough(long simulationTimeMicros) {
		int delivered = 0;
		for (SignalConnection<T> connection : List.copyOf(connections)) {
			delivered = Math.addExact(delivered, connection.deliverThrough(simulationTimeMicros));
		}
		return delivered;
	}

	public SignalSample<T> lastSample() {
		return lastSample;
	}

	public int connectionCount() {
		return connections.size();
	}
}

