package dev.eigenworks.digital;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import dev.eigenworks.signal.DigitalWord;

/**
 * Explicit digital wiring graph. Its adjacency cache is rebuilt only after a
 * topology mutation, never by scanning blocks during simulation.
 */
public final class DigitalNetwork {
	private final Set<DigitalPort> ports = new LinkedHashSet<>();
	private final Set<DigitalConnection> connections = new LinkedHashSet<>();
	private Map<DigitalPort, List<DigitalPort>> cachedOutgoing = Map.of();
	private boolean topologyDirty = true;
	private long topologyRevision;
	private long cacheBuildCount;

	public void addPort(DigitalPort port) {
		Objects.requireNonNull(port, "port");
		if (!ports.add(port)) {
			throw new IllegalArgumentException("Digital port is already registered: " + port);
		}
		markDirty();
	}

	public boolean removePort(DigitalPort port) {
		Objects.requireNonNull(port, "port");
		if (!ports.remove(port)) {
			return false;
		}
		connections.removeIf(connection -> connection.output().equals(port) || connection.input().equals(port));
		markDirty();
		return true;
	}

	public void connect(DigitalPort output, DigitalPort input) {
		requireRegistered(output);
		requireRegistered(input);
		if (output.direction() != DigitalPortDirection.OUTPUT || input.direction() != DigitalPortDirection.INPUT) {
			throw new IllegalArgumentException("Digital wires must connect an output to an input");
		}
		if (output.width() != input.width()) {
			throw new IllegalArgumentException("Digital wire width mismatch");
		}
		if (connections.stream().anyMatch(connection -> connection.input().equals(input))) {
			throw new IllegalArgumentException("Digital input already has a driver: " + input);
		}
		if (!connections.add(new DigitalConnection(output, input))) {
			throw new IllegalArgumentException("Digital connection already exists");
		}
		markDirty();
	}

	public boolean disconnect(DigitalPort output, DigitalPort input) {
		boolean removed = connections.remove(new DigitalConnection(output, input));
		if (removed) {
			markDirty();
		}
		return removed;
	}

	public List<DigitalPort> connectionsFrom(DigitalPort output) {
		requireRegistered(output);
		rebuildCacheIfDirty();
		return cachedOutgoing.getOrDefault(output, List.of());
	}

	public int propagate(
			DigitalPort output,
			DigitalWord value,
			BiConsumer<DigitalPort, DigitalWord> receiver) {
		Objects.requireNonNull(value, "value");
		Objects.requireNonNull(receiver, "receiver");
		if (output.width() != value.width()) {
			throw new IllegalArgumentException("Published word width does not match output port");
		}
		List<DigitalPort> destinations = connectionsFrom(output);
		for (DigitalPort destination : destinations) {
			receiver.accept(destination, value);
		}
		return destinations.size();
	}

	public long topologyRevision() {
		return topologyRevision;
	}

	public long cacheBuildCount() {
		return cacheBuildCount;
	}

	public int portCount() {
		return ports.size();
	}

	public int connectionCount() {
		return connections.size();
	}

	private void requireRegistered(DigitalPort port) {
		Objects.requireNonNull(port, "port");
		if (!ports.contains(port)) {
			throw new IllegalArgumentException("Digital port is not registered: " + port);
		}
	}

	private void markDirty() {
		topologyDirty = true;
		topologyRevision++;
	}

	private void rebuildCacheIfDirty() {
		if (!topologyDirty) {
			return;
		}
		Map<DigitalPort, List<DigitalPort>> mutable = new LinkedHashMap<>();
		for (DigitalConnection connection : connections) {
			mutable.computeIfAbsent(connection.output(), ignored -> new ArrayList<>()).add(connection.input());
		}
		Map<DigitalPort, List<DigitalPort>> immutable = new HashMap<>();
		mutable.forEach((port, destinations) -> immutable.put(port, List.copyOf(destinations)));
		cachedOutgoing = Map.copyOf(immutable);
		topologyDirty = false;
		cacheBuildCount++;
	}
}

