package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.robotics.PlanarRobotArm;
import dev.eigenworks.robotics.Pose2D;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-owned two-link planar manipulator and Cartesian target adapter. */
public final class RobotArmBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	private static final double REACH_METERS = 2.0;
	private static final DigitalPortSpec TARGET_X = port("target_x", DigitalPortDirection.INPUT);
	private static final DigitalPortSpec TARGET_Y = port("target_y", DigitalPortDirection.INPUT);
	private static final List<DigitalPortSpec> OUTPUTS = List.of(port("x", DigitalPortDirection.OUTPUT),
			port("y", DigitalPortDirection.OUTPUT), port("joint1", DigitalPortDirection.OUTPUT), port("joint2", DigitalPortDirection.OUTPUT));
	private final PlanarRobotArm arm = new PlanarRobotArm(1, 1);
	private DigitalSourceEndpoint targetXSource;
	private DigitalSourceEndpoint targetYSource;
	private DigitalDeviceAddress address;
	private DigitalWorldNetwork network;
	private String simulationId;
	private double requestedX = 2;
	private double requestedY;
	private int presetIndex;

	public RobotArmBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ROBOT_ARM, pos, state); }
	@Override public void bindToLevel(ServerLevel level) { simulationId = "robot_arm:" + level.dimension().identifier() + ":" + worldPosition.asLong(); }
	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() { if (simulationId == null) throw new IllegalStateException("Robot Arm is unloaded"); return simulationId; }
	@Override public long updatePeriodMicros() { return 10_000; }
	@Override public void simulate(SimulationContext context) {
		arm.step(context.deltaMicros() / 1_000_000.0);
		if (network != null) for (DigitalPortSpec output : OUTPUTS) network.publish(this, output.name(), outputValue(output.name()), context.simulationTimeMicros(), context.deltaMicros());
		setChanged();
	}
	public boolean commandTarget(double x, double y) {
		requestedX = x; requestedY = y;
		boolean accepted = arm.moveTo(x, y, false);
		setChanged(); return accepted;
	}
	public boolean nextPreset() {
		double[][] presets = {{1, 1}, {0.5, 1.5}, {-0.75, 1.25}, {1.5, 0.25}};
		double[] selected = presets[presetIndex++ % presets.length];
		return commandTarget(selected[0], selected[1]);
	}
	public void resetArm() { requestedX = 2; requestedY = 0; arm.restore(0, 0, 2, 0); setChanged(); }
	public PlanarRobotArm arm() { return arm; }

	@Override public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) { address = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong()); this.network = network; }
	@Override public void unbindDigitalNetwork() { network = null; }
	@Override public DigitalDeviceAddress digitalAddress() { if (address == null) throw new IllegalStateException("Robot Arm is not network-bound"); return address; }
	@Override public List<DigitalPortSpec> outputPorts() { return OUTPUTS; }
	@Override public List<DigitalInputBinding> inputBindings() { return List.of(new DigitalInputBinding(TARGET_X, targetXSource), new DigitalInputBinding(TARGET_Y, targetYSource)); }
	@Override public DigitalWord outputValue(String port) {
		Pose2D pose = arm.endEffector();
		return switch (port) {
			case "x" -> encode(pose.xMeters(), REACH_METERS);
			case "y" -> encode(pose.yMeters(), REACH_METERS);
			case "joint1" -> encode(arm.joint1(), Math.PI);
			case "joint2" -> encode(arm.joint2(), Math.PI);
			default -> throw new IllegalArgumentException("Unknown Robot Arm output: " + port);
		};
	}
	@Override public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		if (source.width() != 8) throw new IllegalArgumentException("Robot targets require eight bits");
		if (TARGET_X.name().equals(inputPort)) targetXSource = source;
		else if (TARGET_Y.name().equals(inputPort)) targetYSource = source;
		else throw new IllegalArgumentException("Unknown Robot Arm input: " + inputPort);
		setChanged();
	}
	@Override public void disconnectInput(String inputPort) {
		if (TARGET_X.name().equals(inputPort)) targetXSource = null;
		else if (TARGET_Y.name().equals(inputPort)) targetYSource = null;
		else throw new IllegalArgumentException("Unknown Robot Arm input: " + inputPort);
		setChanged();
	}
	@Override public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		if (value.width() != 8) throw new IllegalArgumentException("Robot targets require eight bits");
		if (TARGET_X.name().equals(inputPort)) requestedX = decode(value, REACH_METERS);
		else if (TARGET_Y.name().equals(inputPort)) requestedY = decode(value, REACH_METERS);
		else throw new IllegalArgumentException("Unknown Robot Arm input: " + inputPort);
		arm.moveTo(requestedX, requestedY, false);
		setChanged();
	}

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		requestedX = input.getDoubleOr("target_x", 2); requestedY = input.getDoubleOr("target_y", 0);
		arm.restore(input.getDoubleOr("joint_1", 0), input.getDoubleOr("joint_2", 0), requestedX, requestedY);
		presetIndex = input.getIntOr("preset", 0);
		targetXSource = DigitalLinkStorage.read(input, "target_x_source"); targetYSource = DigitalLinkStorage.read(input, "target_y_source");
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("target_x", requestedX); output.putDouble("target_y", requestedY);
		output.putDouble("joint_1", arm.joint1()); output.putDouble("joint_2", arm.joint2()); output.putInt("preset", presetIndex);
		DigitalLinkStorage.write(output, "target_x_source", targetXSource); DigitalLinkStorage.write(output, "target_y_source", targetYSource);
	}
	private static DigitalPortSpec port(String name, DigitalPortDirection direction) { return new DigitalPortSpec(name, 8, direction); }
	private static DigitalWord encode(double value, double magnitude) { return new DigitalWord(8, Math.round((Math.clamp(value / magnitude, -1, 1) + 1) * 127.5)); }
	private static double decode(DigitalWord value, double magnitude) { return (value.value() / 127.5 - 1) * magnitude; }
}
