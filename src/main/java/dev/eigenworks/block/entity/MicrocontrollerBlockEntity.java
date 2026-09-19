package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.computer.assembly.AssemblyDiagnostic;
import dev.eigenworks.computer.assembly.AssemblyResult;
import dev.eigenworks.computer.cpu.AluFlags;
import dev.eigenworks.computer.cpu.CpuStatus;
import dev.eigenworks.computer.cpu.Eigen8Cpu;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.embedded.EigenMicrocontroller;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent, server-authoritative Minecraft adapter for an Eigen-MCU. */
public final class MicrocontrollerBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	public static final int MAX_SOURCE_CHARS = 32_000;
	private static final long UPDATE_PERIOD_MICROS = 1_000L;
	private static final DigitalPortSpec GPIO_INPUT = new DigitalPortSpec("gpio_in", 8, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec GPIO_OUTPUT = new DigitalPortSpec("gpio_out", 8, DigitalPortDirection.OUTPUT);
	private static final DigitalPortSpec PWM_OUTPUT = new DigitalPortSpec("pwm0", 1, DigitalPortDirection.OUTPUT);

	private final EigenMicrocontroller mcu = new EigenMicrocontroller();
	private String lastDiagnostic = "No program loaded";
	private String simulationId;
	private DigitalSourceEndpoint gpioSource;
	private DigitalDeviceAddress digitalAddress;
	private DigitalWorldNetwork digitalNetwork;
	private int publishedGpio;
	private boolean pwmHigh;

	public MicrocontrollerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MICROCONTROLLER, pos, state);
	}

	@Override
	public void bindToLevel(ServerLevel level) {
		simulationId = "microcontroller:" + level.dimension().identifier() + ":" + worldPosition.asLong();
	}

	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() {
		if (simulationId == null) throw new IllegalStateException("Microcontroller is not bound to a server level");
		return simulationId;
	}
	@Override public long updatePeriodMicros() { return UPDATE_PERIOD_MICROS; }

	@Override
	public void simulate(SimulationContext context) {
		long deadlinesBefore = mcu.missedDeadlines();
		mcu.simulate(context.simulationTimeMicros(), context.deltaMicros());
		if (mcu.missedDeadlines() > deadlinesBefore) {
			lastDiagnostic = "CONTROL DEADLINE MISSED";
		}
		int nextGpio = mcu.peripherals().gpio().outputLatch() & mcu.peripherals().gpio().directionMask();
		boolean nextPwm = mcu.peripherals().pwm().levelAt(context.simulationTimeMicros());
		if (nextGpio != publishedGpio) {
			publishedGpio = nextGpio;
			publish(GPIO_OUTPUT, new DigitalWord(8, publishedGpio), context);
		}
		if (nextPwm != pwmHigh) {
			pwmHigh = nextPwm;
			publish(PWM_OUTPUT, new DigitalWord(1, pwmHigh ? 1 : 0), context);
		}
		if (mcu.cpu().status() == CpuStatus.RUNNING || mcu.peripherals().adc().busy()) {
			setChanged();
		}
	}

	public AssemblyResult assembleAndLoad(String source) {
		if (source == null || source.length() > MAX_SOURCE_CHARS) {
			AssemblyDiagnostic diagnostic = new AssemblyDiagnostic(1, 1,
					"Program source must contain at most " + MAX_SOURCE_CHARS + " characters", "");
			lastDiagnostic = diagnostic.displayMessage();
			return new AssemblyResult(null, List.of(diagnostic));
		}
		AssemblyResult result = mcu.assembleAndProgram(source);
		lastDiagnostic = result.successful()
				? "PROGRAMMED " + result.program().bytecode().length + " FLASH BYTES"
				: result.diagnostics().getFirst().displayMessage();
		setChanged();
		return result;
	}

	public void toggleRunPause() {
		if (mcu.cpu().status() == CpuStatus.RUNNING) mcu.pause(); else mcu.run();
		setChanged();
	}
	public void resetMcu() { mcu.reset(); setChanged(); }
	public EigenMicrocontroller mcu() { return mcu; }
	public String lastDiagnostic() { return lastDiagnostic; }
	public int gpioOutput() { return publishedGpio; }
	public boolean pwmHigh() { return pwmHigh; }

	@Override
	public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		digitalAddress = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong());
		digitalNetwork = network;
	}
	@Override public void unbindDigitalNetwork() { digitalNetwork = null; }
	@Override public DigitalDeviceAddress digitalAddress() {
		if (digitalAddress == null) throw new IllegalStateException("Microcontroller is not bound to a digital network");
		return digitalAddress;
	}
	@Override public List<DigitalPortSpec> outputPorts() { return List.of(GPIO_OUTPUT, PWM_OUTPUT); }
	@Override public List<DigitalInputBinding> inputBindings() { return List.of(new DigitalInputBinding(GPIO_INPUT, gpioSource)); }
	@Override public DigitalWord outputValue(String port) {
		if (GPIO_OUTPUT.name().equals(port)) return new DigitalWord(8, publishedGpio);
		if (PWM_OUTPUT.name().equals(port)) return new DigitalWord(1, pwmHigh ? 1 : 0);
		throw new IllegalArgumentException("Unknown MCU output: " + port);
	}
	@Override public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		requireGpioInput(inputPort);
		if (source.width() != 8) throw new IllegalArgumentException("MCU GPIO input requires eight bits");
		gpioSource = source;
		setChanged();
	}
	@Override public void disconnectInput(String inputPort) {
		requireGpioInput(inputPort);
		gpioSource = null;
		setChanged();
	}
	@Override public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		requireGpioInput(inputPort);
		if (value.width() != 8) throw new IllegalArgumentException("MCU GPIO input requires eight bits");
		mcu.peripherals().gpio().sampleExternalInputs((int) value.value());
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		lastDiagnostic = input.getStringOr("last_diagnostic", "No program loaded");
		mcu.restoreMetadata(input.getStringOr("program_source", ""), input.getLongOr("missed_deadlines", 0));
		input.getIntArray("flash").ifPresent(values -> restoreBytes(values, EigenMicrocontroller.FLASH_SIZE, mcu.flash()::restore));
		input.getIntArray("ram").ifPresent(values -> restoreBytes(values, EigenMicrocontroller.RAM_SIZE, mcu.ram()::restore));
		int[] registers = input.getIntArray("registers").orElse(new int[Eigen8Cpu.REGISTER_COUNT]);
		if (registers.length != Eigen8Cpu.REGISTER_COUNT) registers = new int[Eigen8Cpu.REGISTER_COUNT];
		CpuStatus status = enumValue(CpuStatus.values(), input.getIntOr("status", 0), CpuStatus.PAUSED);
		mcu.cpu().restoreState(registers, input.getIntOr("pc", 0), input.getIntOr("sp", Eigen8Cpu.DEFAULT_STACK_POINTER),
				AluFlags.fromMask(input.getIntOr("flags", 0)), status,
				input.getLongOr("cycles", 0), input.getLongOr("instructions", 0));
		mcu.peripherals().gpio().restore(input.getIntOr("gpio_direction", 0), input.getIntOr("gpio_output", 0),
				input.getIntOr("gpio_input", 0));
		mcu.peripherals().adc().restoreResult(input.getIntOr("adc_result", 0));
		mcu.peripherals().setAnalogInputVolts(input.getDoubleOr("analog_input", 0.0));
		mcu.peripherals().pwm().setDutyCode(input.getIntOr("pwm_duty", 0));
		mcu.peripherals().pwm().setFrequencyIndex(Math.clamp(input.getIntOr("pwm_frequency", 2), 0, 3));
		mcu.peripherals().pwm().setEnabled(input.getBooleanOr("pwm_enabled", false));
		mcu.peripherals().timer().restore(
				Math.clamp(input.getIntOr("timer_reload", 1_000), 1, 0xFFFF),
				input.getIntOr("timer_remaining", 1_000), input.getIntOr("timer_vector", 8),
				input.getBooleanOr("timer_enabled", false), input.getBooleanOr("timer_interrupt", false),
				input.getBooleanOr("timer_pending", false));
		gpioSource = DigitalLinkStorage.read(input, "gpio_source");
		publishedGpio = input.getIntOr("published_gpio", 0) & 0xFF;
		pwmHigh = input.getBooleanOr("pwm_high", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("last_diagnostic", lastDiagnostic);
		output.putString("program_source", mcu.programSource());
		output.putLong("missed_deadlines", mcu.missedDeadlines());
		output.putIntArray("flash", packBytes(mcu.flash().copyBytes()));
		output.putIntArray("ram", packBytes(mcu.ram().copyBytes()));
		output.putIntArray("registers", mcu.cpu().registerSnapshot());
		output.putInt("pc", mcu.cpu().programCounter());
		output.putInt("sp", mcu.cpu().stackPointer());
		output.putInt("flags", mcu.cpu().flags().mask());
		output.putInt("status", mcu.cpu().status().ordinal());
		output.putLong("cycles", mcu.cpu().totalCycles());
		output.putLong("instructions", mcu.cpu().totalInstructions());
		output.putInt("gpio_direction", mcu.peripherals().gpio().directionMask());
		output.putInt("gpio_output", mcu.peripherals().gpio().outputLatch());
		output.putInt("gpio_input", mcu.peripherals().gpio().externalInputs());
		output.putInt("adc_result", mcu.peripherals().adc().result());
		output.putDouble("analog_input", mcu.peripherals().analogInputVolts());
		output.putInt("pwm_duty", mcu.peripherals().pwm().dutyCode());
		output.putInt("pwm_frequency", mcu.peripherals().pwm().frequencyIndex());
		output.putBoolean("pwm_enabled", mcu.peripherals().pwm().enabled());
		output.putInt("timer_reload", mcu.peripherals().timer().reloadCycles());
		output.putInt("timer_remaining", mcu.peripherals().timer().remainingCycles());
		output.putInt("timer_vector", mcu.peripherals().timer().interruptVector());
		output.putBoolean("timer_enabled", mcu.peripherals().timer().enabled());
		output.putBoolean("timer_interrupt", mcu.peripherals().timer().interruptEnabled());
		output.putBoolean("timer_pending", mcu.peripherals().timer().overflowPending());
		output.putInt("published_gpio", publishedGpio);
		output.putBoolean("pwm_high", pwmHigh);
		DigitalLinkStorage.write(output, "gpio_source", gpioSource);
	}

	private void publish(DigitalPortSpec port, DigitalWord value, SimulationContext context) {
		if (digitalNetwork != null) {
			digitalNetwork.publish(this, port.name(), value, context.simulationTimeMicros(), context.deltaMicros());
		}
	}
	private static void requireGpioInput(String port) {
		if (!GPIO_INPUT.name().equals(port)) throw new IllegalArgumentException("Unknown MCU input: " + port);
	}
	private static int[] packBytes(byte[] bytes) {
		int[] packed = new int[(bytes.length + 3) / 4];
		for (int index = 0; index < bytes.length; index++) packed[index / 4] |= Byte.toUnsignedInt(bytes[index]) << ((index % 4) * 8);
		return packed;
	}
	private static void restoreBytes(int[] packed, int length, java.util.function.Consumer<byte[]> restorer) {
		if (packed.length != (length + 3) / 4) return;
		byte[] bytes = new byte[length];
		for (int index = 0; index < length; index++) bytes[index] = (byte) (packed[index / 4] >>> ((index % 4) * 8));
		restorer.accept(bytes);
	}
	private static <T> T enumValue(T[] values, int ordinal, T fallback) {
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
	}
}
