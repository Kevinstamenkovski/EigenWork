package dev.eigenworks.simulation;

import net.minecraft.server.level.ServerLevel;

/** A scheduled block entity whose identity becomes available when its chunk loads. */
public interface LoadedSimulationDevice extends SimulationDevice {
	/** Initializes transient server-level state before scheduler registration. */
	void bindToLevel(ServerLevel level);

	/** Releases transient state after scheduler removal. */
	void unbindFromLevel();
}
