package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.ClockEdge;
import dev.eigenworks.digital.ClockEdgeEvent;
import dev.eigenworks.digital.DigitalCounter;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.EngineeringScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent Minecraft adapter for an event-driven eight-bit counter. */
public final class DigitalCounterBlockEntity extends BlockEntity implements WorldDigitalDevice {
	private static final int WIDTH = 8;
	private static final DigitalPortSpec CLOCK_INPUT = new DigitalPortSpec("clock", 1, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec OUTPUT = new DigitalPortSpec("output", WIDTH, DigitalPortDirection.OUTPUT);
	private final DigitalCounter counter = new DigitalCounter("digital_counter", WIDTH, ClockEdge.RISING);
	private DigitalSourceEndpoint clockSource;
	private boolean clockHigh;
	private DigitalDeviceAddress digitalAddress;
	private DigitalWorldNetwork digitalNetwork;

	public DigitalCounterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIGITAL_COUNTER, pos, state);
	}

	public void pulse() {
		counter.onClockEdge(risingEdge());
		setChanged();
		publishOutput(EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}

	public void reset() {
		counter.requestReset();
		counter.onClockEdge(risingEdge());
		setChanged();
		publishOutput(EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}

	public long value() {
		return counter.value().value();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		counter.restoreValue(new DigitalWord(WIDTH, input.getLongOr("value", 0L)));
		clockHigh = input.getBooleanOr("clock_high", false);
		clockSource = DigitalLinkStorage.read(input, "clock_source");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("value", value());
		output.putBoolean("clock_high", clockHigh);
		DigitalLinkStorage.write(output, "clock_source", clockSource);
	}

	@Override
	public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		digitalAddress = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong());
		digitalNetwork = network;
	}

	@Override
	public void unbindDigitalNetwork() {
		digitalNetwork = null;
	}

	@Override
	public DigitalDeviceAddress digitalAddress() {
		if (digitalAddress == null) {
			throw new IllegalStateException("Digital counter is not bound to a server level");
		}
		return digitalAddress;
	}

	@Override
	public List<DigitalPortSpec> outputPorts() {
		return List.of(OUTPUT);
	}

	@Override
	public List<DigitalInputBinding> inputBindings() {
		return List.of(new DigitalInputBinding(CLOCK_INPUT, clockSource));
	}

	@Override
	public DigitalWord outputValue(String port) {
		if (!OUTPUT.name().equals(port)) {
			throw new IllegalArgumentException("Unknown digital counter output: " + port);
		}
		return counter.value();
	}

	@Override
	public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		requireClockPort(inputPort);
		if (source.width() != CLOCK_INPUT.width()) {
			throw new IllegalArgumentException("Counter clock input requires one bit");
		}
		clockSource = source;
		setChanged();
	}

	@Override
	public void disconnectInput(String inputPort) {
		requireClockPort(inputPort);
		clockSource = null;
		clockHigh = false;
		setChanged();
	}

	@Override
	public void acceptInput(String inputPort, DigitalWord input, long timestampMicros, long samplePeriodMicros) {
		requireClockPort(inputPort);
		boolean nextHigh = input.value() != 0L;
		if (timestampMicros == 0L && samplePeriodMicros == 0L) {
			clockHigh = nextHigh;
			return;
		}
		if (nextHigh && !clockHigh) {
			counter.onClockEdge(new ClockEdgeEvent(
					"world_clock", ClockEdge.RISING, timestampMicros, Math.max(1L, samplePeriodMicros)));
			setChanged();
			publishOutput(samplePeriodMicros);
		}
		clockHigh = nextHigh;
	}

	private ClockEdgeEvent risingEdge() {
		long gameTicks = level == null ? 0L : level.getGameTime();
		return new ClockEdgeEvent(
				"digital_counter:" + worldPosition.asLong(),
				ClockEdge.RISING,
				gameTicks * EngineeringScheduler.MINECRAFT_TICK_MICROS,
				EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}

	private void publishOutput(long samplePeriodMicros) {
		if (digitalNetwork == null) {
			return;
		}
		long timestamp = level == null ? 0L : level.getGameTime() * EngineeringScheduler.MINECRAFT_TICK_MICROS;
		digitalNetwork.publish(this, OUTPUT.name(), counter.value(), timestamp, Math.max(1L, samplePeriodMicros));
	}

	private static void requireClockPort(String port) {
		if (!CLOCK_INPUT.name().equals(port)) {
			throw new IllegalArgumentException("Unknown digital counter input: " + port);
		}
	}
}
