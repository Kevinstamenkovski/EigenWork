package dev.eigenworks.simulation;

import java.util.IdentityHashMap;
import java.util.Map;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.config.EngineeringConfig;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.networking.world.CanWorldNetwork;
import dev.eigenworks.networking.world.WorldCanElement;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/** Owns one central scheduler for each running logical server. */
public final class EngineeringSimulation {
	private static final Map<MinecraftServer, EngineeringScheduler> SCHEDULERS = new IdentityHashMap<>();
	private static final Map<MinecraftServer, DigitalWorldNetwork> DIGITAL_NETWORKS = new IdentityHashMap<>();
	private static final Map<MinecraftServer, CanWorldNetwork> CAN_NETWORKS = new IdentityHashMap<>();

	private EngineeringSimulation() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			SCHEDULERS.put(server, createScheduler());
			DIGITAL_NETWORKS.put(server, new DigitalWorldNetwork());
			CAN_NETWORKS.put(server, new CanWorldNetwork());
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			SCHEDULERS.remove(server);
			DIGITAL_NETWORKS.remove(server);
			CAN_NETWORKS.remove(server);
		});
		ServerTickEvents.END_SERVER_TICK.register(EngineeringSimulation::tick);
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof LoadedSimulationDevice device) {
				device.bindToLevel(level);
				scheduler(level.getServer()).register(device);
			}
			if (blockEntity instanceof WorldDigitalDevice device) {
				DigitalWorldNetwork network = digitalNetwork(level.getServer());
				device.bindDigitalNetwork(level, network);
				network.register(device);
			}
			if (blockEntity instanceof WorldCanElement element) {
				CanWorldNetwork network = canNetwork(level.getServer());
				element.bindCanNetwork(level, network);
				network.register(element);
			}
		});
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof LoadedSimulationDevice device) {
				EngineeringScheduler scheduler = SCHEDULERS.get(level.getServer());
				if (scheduler != null) {
					scheduler.unregister(device.simulationId());
				}
				device.unbindFromLevel();
			}
			if (blockEntity instanceof WorldDigitalDevice device) {
				DigitalWorldNetwork network = DIGITAL_NETWORKS.get(level.getServer());
				if (network != null) {
					network.unregister(device);
				}
				device.unbindDigitalNetwork();
			}
			if (blockEntity instanceof WorldCanElement element) {
				CanWorldNetwork network = CAN_NETWORKS.get(level.getServer());
				if (network != null) network.unregister(element);
				element.unbindCanNetwork();
			}
		});
	}

	public static EngineeringScheduler scheduler(MinecraftServer server) {
		EngineeringScheduler scheduler = SCHEDULERS.get(server);
		if (scheduler == null) {
			throw new IllegalStateException("Engineering scheduler is unavailable before server start or after stop");
		}
		return scheduler;
	}

	public static DigitalWorldNetwork digitalNetwork(MinecraftServer server) {
		DigitalWorldNetwork network = DIGITAL_NETWORKS.get(server);
		if (network == null) {
			throw new IllegalStateException("Digital network is unavailable before server start or after stop");
		}
		return network;
	}

	public static CanWorldNetwork canNetwork(MinecraftServer server) {
		CanWorldNetwork network = CAN_NETWORKS.get(server);
		if (network == null) throw new IllegalStateException("CAN network is unavailable before server start or after stop");
		return network;
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
		CanWorldNetwork canNetwork = CAN_NETWORKS.get(server);
		if (canNetwork != null) canNetwork.advance(50_000);
	}
}
