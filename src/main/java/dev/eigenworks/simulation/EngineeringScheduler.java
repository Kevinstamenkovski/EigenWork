package dev.eigenworks.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic fixed-step scheduler. Devices run in registration order and
 * topology changes made during a tick become visible on the next tick.
 */
public final class EngineeringScheduler {
	public static final long MINECRAFT_TICK_MICROS = 50_000L;

	private final long baseStepMicros;
	private final int maximumUpdatesPerTick;
	private final Map<String, SimulationDevice> devices = new LinkedHashMap<>();
	private long simulationTimeMicros;

	public EngineeringScheduler(long baseStepMicros, int maximumUpdatesPerTick) {
		if (baseStepMicros <= 0 || MINECRAFT_TICK_MICROS % baseStepMicros != 0) {
			throw new IllegalArgumentException("Base step must be a positive divisor of 50000 us");
		}
		if (maximumUpdatesPerTick <= 0) {
			throw new IllegalArgumentException("Maximum updates per tick must be positive");
		}
		this.baseStepMicros = baseStepMicros;
		this.maximumUpdatesPerTick = maximumUpdatesPerTick;
	}

	public void register(SimulationDevice device) {
		Objects.requireNonNull(device, "device");
		String id = requireId(device.simulationId());
		validatePeriod(device.updatePeriodMicros());
		if (devices.putIfAbsent(id, device) != null) {
			throw new IllegalArgumentException("Duplicate simulation device id: " + id);
		}
	}

	public boolean unregister(String deviceId) {
		return devices.remove(requireId(deviceId)) != null;
	}

	public SchedulerReport tick() {
		long startTime = simulationTimeMicros;
		long endTime = Math.addExact(startTime, MINECRAFT_TICK_MICROS);
		int executed = 0;
		int skipped = 0;
		List<SimulationFault> faults = new ArrayList<>();
		List<SimulationDevice> tickDevices = List.copyOf(devices.values());

		for (long stepTime = Math.addExact(startTime, baseStepMicros);
				stepTime <= endTime;
				stepTime += baseStepMicros) {
			for (SimulationDevice device : tickDevices) {
				long period = device.updatePeriodMicros();
				if (stepTime % period != 0) {
					continue;
				}
				if (executed >= maximumUpdatesPerTick) {
					skipped++;
					continue;
				}
				try {
					device.simulate(new SimulationContext(stepTime, period));
				} catch (RuntimeException exception) {
					faults.add(new SimulationFault(
							device.simulationId(),
							"DEVICE_UPDATE_FAILED",
							exception.getClass().getSimpleName() + ": " + Objects.toString(exception.getMessage(), "")));
				}
				executed++;
			}
		}

		simulationTimeMicros = endTime;
		return new SchedulerReport(startTime, endTime, executed, skipped, faults);
	}

	public long simulationTimeMicros() {
		return simulationTimeMicros;
	}

	public int registeredDeviceCount() {
		return devices.size();
	}

	private void validatePeriod(long periodMicros) {
		if (periodMicros <= 0 || periodMicros % baseStepMicros != 0) {
			throw new IllegalArgumentException("Device period must be a positive multiple of the base step");
		}
	}

	private static String requireId(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Simulation device id cannot be blank");
		}
		return value;
	}
}

