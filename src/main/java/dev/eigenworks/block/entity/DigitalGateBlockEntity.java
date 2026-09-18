package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.DigitalGateOperation;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent configurable eight-bit combinational gate. */
public final class DigitalGateBlockEntity extends BlockEntity implements WorldDigitalDevice {
	private static final int WIDTH = 8;
	private static final DigitalPortSpec INPUT_A = new DigitalPortSpec("a", WIDTH, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec INPUT_B = new DigitalPortSpec("b", WIDTH, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec OUTPUT = new DigitalPortSpec("output", WIDTH, DigitalPortDirection.OUTPUT);

	private DigitalGateOperation operation = DigitalGateOperation.AND;
	private DigitalWord inputA = new DigitalWord(WIDTH, 0L);
	private DigitalWord inputB = new DigitalWord(WIDTH, 0xFFL);
	private DigitalWord output = operation.apply(inputA, inputB);
	private DigitalSourceEndpoint sourceA;
	private DigitalSourceEndpoint sourceB;
	private DigitalDeviceAddress digitalAddress;
	private DigitalWorldNetwork digitalNetwork;

	public DigitalGateBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIGITAL_GATE, pos, state);
	}

	public void toggleManualInputA() {
		inputA = new DigitalWord(WIDTH, inputA.value() == 0L ? 0xFFL : 0L);
		recompute(0L, 1L);
		setChanged();
	}

	public void selectNextOperation() {
		operation = operation.next();
		recompute(0L, 1L);
		setChanged();
	}

	public DigitalGateOperation operation() {
		return operation;
	}

	public long outputValue() {
		return output.value();
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
			throw new IllegalStateException("Digital gate is not bound to a server level");
		}
		return digitalAddress;
	}

	@Override
	public List<DigitalPortSpec> outputPorts() {
		return List.of(OUTPUT);
	}

	@Override
	public List<DigitalInputBinding> inputBindings() {
		return List.of(new DigitalInputBinding(INPUT_A, sourceA), new DigitalInputBinding(INPUT_B, sourceB));
	}

	@Override
	public DigitalWord outputValue(String port) {
		requirePort(port, OUTPUT);
		return output;
	}

	@Override
	public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		if (source.width() != WIDTH) {
			throw new IllegalArgumentException("Digital gate inputs require eight bits");
		}
		if (INPUT_A.name().equals(inputPort)) {
			sourceA = source;
		} else if (INPUT_B.name().equals(inputPort)) {
			sourceB = source;
		} else {
			throw new IllegalArgumentException("Unknown digital gate input: " + inputPort);
		}
		setChanged();
	}

	@Override
	public void disconnectInput(String inputPort) {
		if (INPUT_A.name().equals(inputPort)) {
			sourceA = null;
		} else if (INPUT_B.name().equals(inputPort)) {
			sourceB = null;
		} else {
			throw new IllegalArgumentException("Unknown digital gate input: " + inputPort);
		}
		setChanged();
	}

	@Override
	public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		if (value.width() != WIDTH) {
			throw new IllegalArgumentException("Digital gate inputs require eight bits");
		}
		if (INPUT_A.name().equals(inputPort)) {
			inputA = value;
		} else if (INPUT_B.name().equals(inputPort)) {
			inputB = value;
		} else {
			throw new IllegalArgumentException("Unknown digital gate input: " + inputPort);
		}
		recompute(timestampMicros, Math.max(1L, samplePeriodMicros));
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		try {
			operation = DigitalGateOperation.valueOf(input.getStringOr("operation", "AND"));
		} catch (IllegalArgumentException ignored) {
			operation = DigitalGateOperation.AND;
		}
		inputA = new DigitalWord(WIDTH, input.getLongOr("input_a", 0L));
		inputB = new DigitalWord(WIDTH, input.getLongOr("input_b", 0xFFL));
		output = operation.apply(inputA, inputB);
		sourceA = DigitalLinkStorage.read(input, "source_a");
		sourceB = DigitalLinkStorage.read(input, "source_b");
	}

	@Override
	protected void saveAdditional(ValueOutput outputData) {
		super.saveAdditional(outputData);
		outputData.putString("operation", operation.name());
		outputData.putLong("input_a", inputA.value());
		outputData.putLong("input_b", inputB.value());
		outputData.putLong("output", output.value());
		DigitalLinkStorage.write(outputData, "source_a", sourceA);
		DigitalLinkStorage.write(outputData, "source_b", sourceB);
	}

	private void recompute(long timestampMicros, long samplePeriodMicros) {
		DigitalWord next = operation.apply(inputA, inputB);
		if (next.equals(output)) {
			return;
		}
		output = next;
		if (digitalNetwork != null) {
			digitalNetwork.publish(this, OUTPUT.name(), output, timestampMicros, samplePeriodMicros);
		}
	}

	private static void requirePort(String actual, DigitalPortSpec expected) {
		if (!expected.name().equals(actual)) {
			throw new IllegalArgumentException("Unknown digital gate port: " + actual);
		}
	}
}
