package dev.eigenworks.block.entity;

import dev.eigenworks.digital.ClockEdge;
import dev.eigenworks.digital.ClockEdgeEvent;
import dev.eigenworks.digital.DigitalCounter;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.EngineeringScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent Minecraft adapter for an event-driven eight-bit counter. */
public final class DigitalCounterBlockEntity extends BlockEntity {
	private static final int WIDTH = 8;
	private final DigitalCounter counter = new DigitalCounter("digital_counter", WIDTH, ClockEdge.RISING);

	public DigitalCounterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIGITAL_COUNTER, pos, state);
	}

	public void pulse() {
		counter.onClockEdge(risingEdge());
		setChanged();
	}

	public void reset() {
		counter.requestReset();
		counter.onClockEdge(risingEdge());
		setChanged();
	}

	public long value() {
		return counter.value().value();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		counter.restoreValue(new DigitalWord(WIDTH, input.getLongOr("value", 0L)));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("value", value());
	}

	private ClockEdgeEvent risingEdge() {
		long gameTicks = level == null ? 0L : level.getGameTime();
		return new ClockEdgeEvent(
				"digital_counter:" + worldPosition.asLong(),
				ClockEdge.RISING,
				gameTicks * EngineeringScheduler.MINECRAFT_TICK_MICROS,
				EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}
}
