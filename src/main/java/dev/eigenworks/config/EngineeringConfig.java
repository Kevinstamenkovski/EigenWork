package dev.eigenworks.config;

/**
 * Validated defaults for the engineering simulation. Persistence and a config
 * screen will be added when the scheduler is connected to world state.
 */
public record EngineeringConfig(
		int simulationStepMicros,
		int maximumDeviceUpdatesPerTick,
		int visualUpdateTicks,
		boolean sensorNoiseEnabled,
		boolean destructiveFailuresEnabled) {

	public static final EngineeringConfig DEFAULT = new EngineeringConfig(1_000, 20_000, 2, true, false);

	public EngineeringConfig {
		if (simulationStepMicros <= 0 || 50_000 % simulationStepMicros != 0) {
			throw new IllegalArgumentException("Simulation step must be a positive divisor of 50000 us");
		}
		if (maximumDeviceUpdatesPerTick <= 0) {
			throw new IllegalArgumentException("Maximum device updates must be positive");
		}
		if (visualUpdateTicks <= 0) {
			throw new IllegalArgumentException("Visual update period must be positive");
		}
	}

	public static void validateDefaults() {
		// Accessing DEFAULT invokes all compact-constructor validation.
		DEFAULT.hashCode();
	}
}

