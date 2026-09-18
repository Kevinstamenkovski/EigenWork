package dev.eigenworks.simulation;

import java.util.List;

/** Result of one 50 ms Minecraft-tick simulation advance. */
public record SchedulerReport(
		long startTimeMicros,
		long endTimeMicros,
		int executedUpdates,
		int skippedUpdates,
		List<SimulationFault> faults) {

	public SchedulerReport {
		faults = List.copyOf(faults);
	}

	public boolean overloaded() {
		return skippedUpdates > 0;
	}
}

