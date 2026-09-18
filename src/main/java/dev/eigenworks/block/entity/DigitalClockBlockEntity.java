package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.LogicalClock;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent Minecraft adapter for the pure {@link LogicalClock}. */
public final class DigitalClockBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	private static final long UPDATE_PERIOD_MICROS = 10_000L;
	private static final double[] FREQUENCIES_HZ = {1.0, 2.0, 5.0, 10.0, 20.0};
	private static final DigitalPortSpec OUTPUT = new DigitalPortSpec("output", 1, DigitalPortDirection.OUTPUT);

	private double frequencyHertz = 2.0;
	private double dutyCycle = 0.5;
	private double phaseCycles;
	private boolean enabled = true;
	private boolean levelHigh;
	private String simulationId;
	private LogicalClock clock;
	private DigitalDeviceAddress digitalAddress;
	private DigitalWorldNetwork digitalNetwork;

	public DigitalClockBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIGITAL_CLOCK, pos, state);
	}

	@Override
	public void bindToLevel(ServerLevel level) {
		simulationId = "digital_clock:" + level.dimension().identifier() + ":" + worldPosition.asLong();
		clock = new LogicalClock(simulationId, UPDATE_PERIOD_MICROS, frequencyHertz, dutyCycle, phaseCycles, enabled);
		levelHigh = clock.level();
	}

	@Override
	public void unbindFromLevel() {
		clock = null;
	}

	@Override
	public String simulationId() {
		if (simulationId == null) {
			throw new IllegalStateException("Digital clock is not bound to a server level");
		}
		return simulationId;
	}

	@Override
	public long updatePeriodMicros() {
		return UPDATE_PERIOD_MICROS;
	}

	@Override
	public void simulate(SimulationContext context) {
		if (clock == null) {
			throw new IllegalStateException("Digital clock simulation ran while unloaded");
		}
		boolean previous = levelHigh;
		clock.simulate(context);
		levelHigh = clock.level();
		if (previous != levelHigh) {
			setChanged();
			if (digitalNetwork != null) {
				digitalNetwork.publish(this, OUTPUT.name(), outputWord(),
						context.simulationTimeMicros(), context.deltaMicros());
			}
		}
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
			throw new IllegalStateException("Digital clock is not bound to a server level");
		}
		return digitalAddress;
	}

	@Override
	public List<DigitalPortSpec> outputPorts() {
		return List.of(OUTPUT);
	}

	@Override
	public List<DigitalInputBinding> inputBindings() {
		return List.of();
	}

	@Override
	public DigitalWord outputValue(String port) {
		requireOutputPort(port);
		return outputWord();
	}

	@Override
	public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		throw new IllegalArgumentException("Digital clock has no input ports");
	}

	@Override
	public void disconnectInput(String inputPort) {
		throw new IllegalArgumentException("Digital clock has no input ports");
	}

	@Override
	public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		throw new IllegalArgumentException("Digital clock has no input ports");
	}

	public void toggleEnabled() {
		enabled = !enabled;
		if (clock != null) {
			clock.setEnabled(enabled);
		}
		setChanged();
	}

	public void selectNextFrequency() {
		int currentIndex = 0;
		for (int index = 0; index < FREQUENCIES_HZ.length; index++) {
			if (Double.compare(FREQUENCIES_HZ[index], frequencyHertz) == 0) {
				currentIndex = index;
				break;
			}
		}
		frequencyHertz = FREQUENCIES_HZ[(currentIndex + 1) % FREQUENCIES_HZ.length];
		if (clock != null) {
			clock.setFrequencyHertz(frequencyHertz);
		}
		setChanged();
	}

	public boolean enabled() {
		return enabled;
	}

	public double frequencyHertz() {
		return frequencyHertz;
	}

	public boolean levelHigh() {
		return levelHigh;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		frequencyHertz = validFrequency(input.getDoubleOr("frequency_hz", 2.0));
		dutyCycle = validDutyCycle(input.getDoubleOr("duty_cycle", 0.5));
		phaseCycles = validPhase(input.getDoubleOr("phase_cycles", 0.0));
		enabled = input.getBooleanOr("enabled", true);
		levelHigh = input.getBooleanOr("level_high", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("frequency_hz", frequencyHertz);
		output.putDouble("duty_cycle", dutyCycle);
		output.putDouble("phase_cycles", phaseCycles);
		output.putBoolean("enabled", enabled);
		output.putBoolean("level_high", levelHigh);
	}

	private static double validFrequency(double value) {
		for (double supported : FREQUENCIES_HZ) {
			if (Double.compare(value, supported) == 0) {
				return value;
			}
		}
		return 2.0;
	}

	private static double validDutyCycle(double value) {
		return Double.isFinite(value) && value > 0.0 && value < 1.0 ? value : 0.5;
	}

	private static double validPhase(double value) {
		return Double.isFinite(value) ? value - Math.floor(value) : 0.0;
	}

	private DigitalWord outputWord() {
		return new DigitalWord(1, levelHigh ? 1L : 0L);
	}

	private static void requireOutputPort(String port) {
		if (!OUTPUT.name().equals(port)) {
			throw new IllegalArgumentException("Unknown digital clock output: " + port);
		}
	}
}
