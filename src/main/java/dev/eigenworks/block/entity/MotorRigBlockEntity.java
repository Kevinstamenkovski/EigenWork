package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.control.MotorPositionController;
import dev.eigenworks.control.PidSnapshot;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.mechanical.MotorAssembly;
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

/** Playable PWM motor plant with optional protected closed-loop position control. */
public final class MotorRigBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	public enum ControlMode { DIRECT_PWM, POSITION_90_DEGREES }

	private static final long PERIOD_MICROS = 5_000;
	private static final DigitalPortSpec COMMAND = new DigitalPortSpec("pwm_command", 8, DigitalPortDirection.INPUT);
	private static final DigitalPortSpec SETPOINT = output("setpoint");
	private static final DigitalPortSpec POSITION = output("position");
	private static final DigitalPortSpec ERROR = output("error");
	private static final DigitalPortSpec CONTROL_OUTPUT = output("control_output");
	private static final DigitalPortSpec ENCODER_LOW = output("encoder_low");
	private static final DigitalPortSpec ENCODER_COUNTS = new DigitalPortSpec("encoder_counts", 16, DigitalPortDirection.OUTPUT);
	private static final List<DigitalPortSpec> OUTPUTS = List.of(
			SETPOINT, POSITION, ERROR, CONTROL_OUTPUT, ENCODER_LOW, ENCODER_COUNTS);

	private final MotorAssembly assembly = new MotorAssembly();
	private final MotorPositionController positionController = new MotorPositionController();
	private DigitalSourceEndpoint commandSource;
	private DigitalDeviceAddress address;
	private DigitalWorldNetwork network;
	private String simulationId;
	private int commandCode = 128;
	private long publishedCounts;
	private boolean controllerDue;
	private ControlMode controlMode = ControlMode.DIRECT_PWM;

	public MotorRigBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MOTOR_RIG, pos, state); }
	@Override public void bindToLevel(ServerLevel level) { simulationId = "motor_rig:" + level.dimension().identifier() + ":" + worldPosition.asLong(); }
	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() {
		if (simulationId == null) throw new IllegalStateException("Motor rig is unloaded");
		return simulationId;
	}
	@Override public long updatePeriodMicros() { return PERIOD_MICROS; }

	@Override public void simulate(SimulationContext context) {
		controllerDue = !controllerDue;
		if (controlMode == ControlMode.POSITION_90_DEGREES) {
			if (controllerDue) positionController.update(assembly);
		} else {
			assembly.driver().setCommand(Math.clamp((commandCode - 128) / 127.0, -1, 1));
		}
		assembly.step(context.deltaMicros() / 1_000_000.0);
		long counts = assembly.encoderCounts();
		if (counts != publishedCounts) { publishedCounts = counts; setChanged(); }
		if (network != null && controllerDue) publishDiagnostics(context, counts);
	}

	private void publishDiagnostics(SimulationContext context, long counts) {
		network.publish(this, "setpoint", outputValue("setpoint"), context.simulationTimeMicros(), 10_000);
		network.publish(this, "position", outputValue("position"), context.simulationTimeMicros(), 10_000);
		network.publish(this, "error", outputValue("error"), context.simulationTimeMicros(), 10_000);
		network.publish(this, "control_output", outputValue("control_output"), context.simulationTimeMicros(), 10_000);
		network.publish(this, "encoder_low", new DigitalWord(8, counts), context.simulationTimeMicros(), 10_000);
		network.publish(this, "encoder_counts", new DigitalWord(16, counts), context.simulationTimeMicros(), 10_000);
	}

	public MotorAssembly assembly() { return assembly; }
	public int commandCode() { return commandCode; }
	public long encoderCounts() { return assembly.encoderCounts(); }
	public ControlMode controlMode() { return controlMode; }
	public PidSnapshot controllerSnapshot() { return positionController.snapshot(); }
	public void toggleControlMode() {
		controlMode = controlMode == ControlMode.DIRECT_PWM ? ControlMode.POSITION_90_DEGREES : ControlMode.DIRECT_PWM;
		positionController.reset();
		setChanged();
	}
	public void resetRig() {
		assembly.motor().restore(0, 0, 0);
		assembly.driver().setCommand(0);
		positionController.reset();
		commandCode = 128;
		publishedCounts = 0;
		setChanged();
	}

	@Override public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		address = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong());
		this.network = network;
	}
	@Override public void unbindDigitalNetwork() { network = null; }
	@Override public DigitalDeviceAddress digitalAddress() {
		if (address == null) throw new IllegalStateException("Motor rig is not network-bound");
		return address;
	}
	@Override public List<DigitalPortSpec> outputPorts() { return OUTPUTS; }
	@Override public List<DigitalInputBinding> inputBindings() { return List.of(new DigitalInputBinding(COMMAND, commandSource)); }
	@Override public DigitalWord outputValue(String port) {
		return switch (port) {
			case "setpoint" -> encodeAngle(positionController.targetRadians());
			case "position" -> encodeAngle(assembly.outputAngle());
			case "error" -> encodeSigned(positionController.targetRadians() - assembly.outputAngle(), Math.PI);
			case "control_output" -> encodeSigned(assembly.driver().command(), 1.0);
			case "encoder_low" -> new DigitalWord(8, encoderCounts());
			case "encoder_counts" -> new DigitalWord(16, encoderCounts());
			default -> throw new IllegalArgumentException("Unknown motor output: " + port);
		};
	}
	@Override public void connectInput(String port, DigitalSourceEndpoint source) {
		requireCommand(port);
		if (source.width() != 8) throw new IllegalArgumentException("Motor PWM command requires eight bits");
		commandSource = source;
		setChanged();
	}
	@Override public void disconnectInput(String port) { requireCommand(port); commandSource = null; commandCode = 128; setChanged(); }
	@Override public void acceptInput(String port, DigitalWord value, long timestamp, long period) {
		requireCommand(port);
		if (value.width() != 8) throw new IllegalArgumentException("Motor PWM command requires eight bits");
		commandCode = (int) value.value();
		setChanged();
	}

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		commandCode = Math.clamp(input.getIntOr("command", 128), 0, 255);
		assembly.setLoadTorque(input.getDoubleOr("load_torque", 0));
		assembly.motor().restore(input.getDoubleOr("current", 0), input.getDoubleOr("velocity", 0), input.getDoubleOr("position", 0));
		publishedCounts = input.getLongOr("counts", 0);
		int mode = Math.clamp(input.getIntOr("control_mode", 0), 0, ControlMode.values().length - 1);
		controlMode = ControlMode.values()[mode];
		commandSource = DigitalLinkStorage.read(input, "command_source");
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("command", commandCode);
		output.putDouble("load_torque", assembly.loadTorque());
		output.putDouble("current", assembly.motor().currentAmperes());
		output.putDouble("velocity", assembly.motor().angularVelocityRadPerSec());
		output.putDouble("position", assembly.motor().angularPositionRadians());
		output.putLong("counts", publishedCounts);
		output.putInt("control_mode", controlMode.ordinal());
		DigitalLinkStorage.write(output, "command_source", commandSource);
	}

	private static DigitalPortSpec output(String name) { return new DigitalPortSpec(name, 8, DigitalPortDirection.OUTPUT); }
	private static DigitalWord encodeAngle(double radians) { return encodeSigned(radians, Math.PI); }
	private static DigitalWord encodeSigned(double value, double magnitude) {
		double normalized = Math.clamp(value / magnitude, -1.0, 1.0);
		return new DigitalWord(8, Math.round((normalized + 1.0) * 127.5));
	}
	private static void requireCommand(String port) {
		if (!COMMAND.name().equals(port)) throw new IllegalArgumentException("Unknown motor input: " + port);
	}
}
