package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.ClockEdge;
import dev.eigenworks.digital.ClockEdgeEvent;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.DigitalRegister;
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

/** Persistent edge-triggered eight-bit register with data and clock inputs. */
public final class DigitalRegisterBlockEntity extends BlockEntity implements WorldDigitalDevice {
	private static final int WIDTH = 8;
	private static final DigitalPortSpec DATA_INPUT = new DigitalPortSpec("data", WIDTH, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec CLOCK_INPUT = new DigitalPortSpec("clock", 1, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec OUTPUT = new DigitalPortSpec("output", WIDTH, DigitalPortDirection.OUTPUT);

	private final DigitalRegister register = new DigitalRegister("digital_register", WIDTH, ClockEdge.RISING);
	private DigitalWord data = new DigitalWord(WIDTH, 0L);
	private boolean clockHigh;
	private DigitalSourceEndpoint dataSource;
	private DigitalSourceEndpoint clockSource;
	private DigitalDeviceAddress digitalAddress;
	private DigitalWorldNetwork digitalNetwork;

	public DigitalRegisterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIGITAL_REGISTER, pos, state);
	}

	public void pulse() {
		capture(0L, EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}

	public void reset() {
		register.requestReset();
		capture(0L, EngineeringScheduler.MINECRAFT_TICK_MICROS);
	}

	public long value() {
		return register.value().value();
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
			throw new IllegalStateException("Digital register is not bound to a server level");
		}
		return digitalAddress;
	}

	@Override
	public List<DigitalPortSpec> outputPorts() {
		return List.of(OUTPUT);
	}

	@Override
	public List<DigitalInputBinding> inputBindings() {
		return List.of(
				new DigitalInputBinding(DATA_INPUT, dataSource),
				new DigitalInputBinding(CLOCK_INPUT, clockSource));
	}

	@Override
	public DigitalWord outputValue(String port) {
		if (!OUTPUT.name().equals(port)) {
			throw new IllegalArgumentException("Unknown digital register output: " + port);
		}
		return register.value();
	}

	@Override
	public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		if (DATA_INPUT.name().equals(inputPort) && source.width() == DATA_INPUT.width()) {
			dataSource = source;
		} else if (CLOCK_INPUT.name().equals(inputPort) && source.width() == CLOCK_INPUT.width()) {
			clockSource = source;
		} else {
			throw new IllegalArgumentException("Unknown or incompatible digital register input: " + inputPort);
		}
		setChanged();
	}

	@Override
	public void disconnectInput(String inputPort) {
		if (DATA_INPUT.name().equals(inputPort)) {
			dataSource = null;
		} else if (CLOCK_INPUT.name().equals(inputPort)) {
			clockSource = null;
			clockHigh = false;
		} else {
			throw new IllegalArgumentException("Unknown digital register input: " + inputPort);
		}
		setChanged();
	}

	@Override
	public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		if (DATA_INPUT.name().equals(inputPort)) {
			if (value.width() != WIDTH) {
				throw new IllegalArgumentException("Register data input requires eight bits");
			}
			data = value;
			register.setInput(data);
			setChanged();
			return;
		}
		if (!CLOCK_INPUT.name().equals(inputPort) || value.width() != 1) {
			throw new IllegalArgumentException("Unknown or incompatible digital register input: " + inputPort);
		}
		boolean nextHigh = value.value() != 0L;
		if (timestampMicros == 0L && samplePeriodMicros == 0L) {
			clockHigh = nextHigh;
			return;
		}
		if (nextHigh && !clockHigh) {
			capture(timestampMicros, Math.max(1L, samplePeriodMicros));
		}
		clockHigh = nextHigh;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		data = new DigitalWord(WIDTH, input.getLongOr("data", 0L));
		register.restoreState(data, new DigitalWord(WIDTH, input.getLongOr("value", 0L)));
		clockHigh = input.getBooleanOr("clock_high", false);
		dataSource = DigitalLinkStorage.read(input, "data_source");
		clockSource = DigitalLinkStorage.read(input, "clock_source");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("data", data.value());
		output.putLong("value", register.value().value());
		output.putBoolean("clock_high", clockHigh);
		DigitalLinkStorage.write(output, "data_source", dataSource);
		DigitalLinkStorage.write(output, "clock_source", clockSource);
	}

	private void capture(long timestampMicros, long samplePeriodMicros) {
		register.onClockEdge(new ClockEdgeEvent(
				"world_clock", ClockEdge.RISING, timestampMicros, samplePeriodMicros));
		setChanged();
		if (digitalNetwork != null) {
			digitalNetwork.publish(this, OUTPUT.name(), register.value(), timestampMicros, samplePeriodMicros);
		}
	}
}
