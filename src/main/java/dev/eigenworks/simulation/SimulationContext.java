package dev.eigenworks.simulation;

/** Immutable timing information supplied to a simulation update. */
public record SimulationContext(long simulationTimeMicros, long deltaMicros) {
	public SimulationContext {
		if (simulationTimeMicros < 0) {
			throw new IllegalArgumentException("Simulation time cannot be negative");
		}
		if (deltaMicros <= 0) {
			throw new IllegalArgumentException("Simulation delta must be positive");
		}
	}

	public double simulationTimeSeconds() {
		return simulationTimeMicros / 1_000_000.0;
	}

	public double deltaSeconds() {
		return deltaMicros / 1_000_000.0;
	}
}

