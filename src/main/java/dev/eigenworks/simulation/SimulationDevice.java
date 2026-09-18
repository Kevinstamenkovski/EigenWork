package dev.eigenworks.simulation;

/** A server-authoritative engineering object updated by the central scheduler. */
public interface SimulationDevice {
	/** Stable identity used for diagnostics and duplicate-registration protection. */
	String simulationId();

	/** Requested update period in microseconds. It must be a multiple of the scheduler base step. */
	long updatePeriodMicros();

	/** Advances this device by one requested period. */
	void simulate(SimulationContext context);
}

