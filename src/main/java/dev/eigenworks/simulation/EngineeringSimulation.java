package dev.eigenworks.simulation;

import java.util.IdentityHashMap;
import java.util.Map;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.config.EngineeringConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/** Owns one central scheduler for each running logical server. */
public final class EngineeringSimulation {
	private static final Map<MinecraftServer, EngineeringScheduler> SCHEDULERS = new IdentityHashMap<>();

	private EngineeringSimulation() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SCHEDULERS.put(server, createScheduler()));
		ServerLifecycleEvents.SERVER_STOPPED.register(SCHEDULERS::remove);
		ServerTickEvents.END_SERVER_TICK.register(EngineeringSimulation::tick);
	}

	public static EngineeringScheduler scheduler(MinecraftServer server) {
		EngineeringScheduler scheduler = SCHEDULERS.get(server);
		if (scheduler == null) {
			throw new IllegalStateException("Engineering scheduler is unavailable before server start or after stop");
		}
		return scheduler;
	}

	private static EngineeringScheduler createScheduler() {
		EngineeringConfig config = EngineeringConfig.DEFAULT;
		return new EngineeringScheduler(config.simulationStepMicros(), config.maximumDeviceUpdatesPerTick());
	}

	private static void tick(MinecraftServer server) {
		EngineeringScheduler scheduler = SCHEDULERS.get(server);
		if (scheduler == null) {
			return;
		}
		SchedulerReport report = scheduler.tick();
		if (report.overloaded()) {
			EigenWorks.LOGGER.warn("SIMULATION UPDATE BUDGET EXCEEDED: {} updates skipped", report.skippedUpdates());
		}
		for (SimulationFault fault : report.faults()) {
			EigenWorks.LOGGER.error("{} [{}]: {}", fault.code(), fault.deviceId(), fault.message());
		}
	}
}

