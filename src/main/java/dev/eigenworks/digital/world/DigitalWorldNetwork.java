package dev.eigenworks.digital.world;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.signal.DigitalWord;

/**
 * Loaded-device registry and cached adjacency map for block-level digital wiring.
 * Topology is rebuilt only after load, unload, connect, or disconnect operations.
 */
public final class DigitalWorldNetwork {
	private static final int MAX_PROPAGATIONS_PER_CASCADE = 4_096;
	private final Map<DigitalDeviceAddress, WorldDigitalDevice> devices = new LinkedHashMap<>();
	private final Map<UUID, DigitalSourceEndpoint> selections = new LinkedHashMap<>();
	private final ArrayDeque<PendingSignal> pendingSignals = new ArrayDeque<>();
	private Map<DigitalSourceEndpoint, List<InputTarget>> outgoing = Map.of();
	private boolean topologyDirty = true;
	private boolean dispatching;
	private long topologyRevision;
	private long cacheBuildCount;
	private long unstableCascadeCount;

	public void register(WorldDigitalDevice device) {
		Objects.requireNonNull(device, "device");
		WorldDigitalDevice previous = devices.putIfAbsent(device.digitalAddress(), device);
		if (previous != null && previous != device) {
			throw new IllegalArgumentException("Duplicate world digital device: " + device.digitalAddress());
		}
		markDirty();
	}

	public void unregister(WorldDigitalDevice device) {
		Objects.requireNonNull(device, "device");
		if (devices.remove(device.digitalAddress(), device)) {
			selections.entrySet().removeIf(entry -> entry.getValue().device().equals(device.digitalAddress()));
			markDirty();
		}
	}

	public DigitalSourceEndpoint selectFirstOutput(UUID playerId, WorldDigitalDevice device) {
		Objects.requireNonNull(playerId, "playerId");
		DigitalPortSpec output = device.outputPorts().stream()
				.filter(port -> port.direction() == DigitalPortDirection.OUTPUT)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Device has no digital output"));
		DigitalSourceEndpoint endpoint = new DigitalSourceEndpoint(device.digitalAddress(), output.name(), output.width());
		selections.put(playerId, endpoint);
		return endpoint;
	}

	/** Cycles the selected output when a multi-output source is clicked repeatedly. */
	public DigitalSourceEndpoint selectNextOutput(UUID playerId, WorldDigitalDevice device) {
		Objects.requireNonNull(playerId, "playerId");
		List<DigitalPortSpec> outputs = device.outputPorts().stream()
				.filter(port -> port.direction() == DigitalPortDirection.OUTPUT)
				.toList();
		if (outputs.isEmpty()) throw new IllegalArgumentException("Device has no digital output");
		DigitalSourceEndpoint current = selections.get(playerId);
		int currentIndex = -1;
		if (current != null && current.device().equals(device.digitalAddress())) {
			for (int index = 0; index < outputs.size(); index++) if (outputs.get(index).name().equals(current.port())) currentIndex = index;
		}
		DigitalPortSpec output = outputs.get((currentIndex + 1) % outputs.size());
		DigitalSourceEndpoint endpoint = new DigitalSourceEndpoint(device.digitalAddress(), output.name(), output.width());
		selections.put(playerId, endpoint);
		return endpoint;
	}

	public DigitalSourceEndpoint selection(UUID playerId) {
		return selections.get(playerId);
	}

	public String connectSelected(UUID playerId, WorldDigitalDevice target) {
		DigitalSourceEndpoint source = selections.remove(Objects.requireNonNull(playerId, "playerId"));
		if (source == null) {
			throw new IllegalStateException("No digital source selected");
		}
		if (source.device().equals(target.digitalAddress())) {
			throw new IllegalArgumentException("A device cannot be wired to itself");
		}
		if (!source.device().dimension().equals(target.digitalAddress().dimension())) {
			throw new IllegalArgumentException("Digital links cannot cross dimensions");
		}
		DigitalInputBinding input = target.inputBindings().stream()
				.filter(binding -> binding.input().width() == source.width())
				.filter(binding -> !binding.connected())
				.findFirst()
				.orElseGet(() -> target.inputBindings().stream()
						.filter(binding -> binding.input().width() == source.width())
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Target has no compatible digital input")));
		target.connectInput(input.input().name(), source);
		markDirty();
		DigitalWord current = outputValue(source);
		if (current != null) {
			target.acceptInput(input.input().name(), current, 0L, 0L);
		}
		return input.input().name();
	}

	public String disconnectFirst(WorldDigitalDevice target) {
		DigitalInputBinding binding = target.inputBindings().stream()
				.filter(DigitalInputBinding::connected)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Device has no connected digital input"));
		target.disconnectInput(binding.input().name());
		markDirty();
		return binding.input().name();
	}

	public int publish(
			WorldDigitalDevice sourceDevice,
			String outputPort,
			DigitalWord value,
			long timestampMicros,
			long samplePeriodMicros) {
		Objects.requireNonNull(sourceDevice, "sourceDevice");
		Objects.requireNonNull(value, "value");
		DigitalSourceEndpoint source = new DigitalSourceEndpoint(
				sourceDevice.digitalAddress(), outputPort, value.width());
		pendingSignals.addLast(new PendingSignal(source, value, timestampMicros, samplePeriodMicros));
		if (dispatching) {
			return 0;
		}
		dispatching = true;
		int delivered = 0;
		int processed = 0;
		try {
			while (!pendingSignals.isEmpty()) {
				if (processed++ >= MAX_PROPAGATIONS_PER_CASCADE) {
					pendingSignals.clear();
					unstableCascadeCount++;
					break;
				}
				PendingSignal signal = pendingSignals.removeFirst();
				rebuildCacheIfDirty();
				for (InputTarget destination : outgoing.getOrDefault(signal.source(), List.of())) {
					destination.device().acceptInput(
							destination.port(), signal.value(),
							signal.timestampMicros(), signal.samplePeriodMicros());
					delivered++;
				}
			}
		} finally {
			dispatching = false;
		}
		return delivered;
	}

	public void markTopologyDirty() {
		markDirty();
	}

	public int deviceCount() {
		return devices.size();
	}

	public long topologyRevision() {
		return topologyRevision;
	}

	public long cacheBuildCount() {
		return cacheBuildCount;
	}

	/** Number of propagation cascades stopped by the bounded-settling guard. */
	public long unstableCascadeCount() {
		return unstableCascadeCount;
	}

	private DigitalWord outputValue(DigitalSourceEndpoint source) {
		WorldDigitalDevice device = devices.get(source.device());
		return device == null ? null : device.outputValue(source.port());
	}

	private void markDirty() {
		topologyDirty = true;
		topologyRevision++;
	}

	private void rebuildCacheIfDirty() {
		if (!topologyDirty) {
			return;
		}
		Map<DigitalSourceEndpoint, List<InputTarget>> mutable = new LinkedHashMap<>();
		for (WorldDigitalDevice device : devices.values()) {
			for (DigitalInputBinding binding : device.inputBindings()) {
				if (!binding.connected() || !devices.containsKey(binding.source().device())) {
					continue;
				}
				mutable.computeIfAbsent(binding.source(), ignored -> new ArrayList<>())
						.add(new InputTarget(device, binding.input().name()));
			}
		}
		Map<DigitalSourceEndpoint, List<InputTarget>> immutable = new LinkedHashMap<>();
		mutable.forEach((source, targets) -> immutable.put(source, List.copyOf(targets)));
		outgoing = Map.copyOf(immutable);
		topologyDirty = false;
		cacheBuildCount++;
	}

	private record InputTarget(WorldDigitalDevice device, String port) {
	}

	private record PendingSignal(
			DigitalSourceEndpoint source,
			DigitalWord value,
			long timestampMicros,
			long samplePeriodMicros) {
	}
}
