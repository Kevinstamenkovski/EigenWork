package dev.eigenworks.block;

import dev.eigenworks.digital.DigitalDiagnosticResult;
import dev.eigenworks.digital.DigitalDiagnostics;
import dev.eigenworks.simulation.EngineeringScheduler;
import dev.eigenworks.simulation.EngineeringSimulation;
import dev.eigenworks.simulation.SimulationContext;
import dev.eigenworks.simulation.SimulationDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** First gameplay adapter for the pure engineering simulation core. */
public final class EngineeringTestBenchBlock extends Block {
	public EngineeringTestBenchBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		EngineeringScheduler scheduler = EngineeringSimulation.scheduler(level.getServer());
		String deviceId = "test_bench:" + player.getUUID() + ":" + pos.asLong();
		scheduler.unregister(deviceId);
		scheduler.register(new BenchDiagnostic(deviceId, scheduler, player));
		player.sendSystemMessage(Component.translatable("message.eigenworks.diagnostic_queued"));
		return InteractionResult.SUCCESS_SERVER;
	}

	private static final class BenchDiagnostic implements SimulationDevice {
		private final String id;
		private final EngineeringScheduler scheduler;
		private final Player player;

		private BenchDiagnostic(String id, EngineeringScheduler scheduler, Player player) {
			this.id = id;
			this.scheduler = scheduler;
			this.player = player;
		}

		@Override
		public String simulationId() {
			return id;
		}

		@Override
		public long updatePeriodMicros() {
			return EngineeringScheduler.MINECRAFT_TICK_MICROS;
		}

		@Override
		public void simulate(SimulationContext context) {
			DigitalDiagnosticResult result = DigitalDiagnostics.run(
					context.simulationTimeMicros(), context.deltaMicros());
			player.sendSystemMessage(Component.translatable(
					"message.eigenworks.diagnostic_result",
					result.gateResult().value(), result.counterResult().value()));
			scheduler.unregister(id);
		}
	}
}
