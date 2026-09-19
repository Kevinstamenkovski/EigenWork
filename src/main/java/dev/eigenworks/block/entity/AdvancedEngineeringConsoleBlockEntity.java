package dev.eigenworks.block.entity;

import dev.eigenworks.computer.accelerator.FloatingPointUnit;
import dev.eigenworks.computer.accelerator.MatrixAccelerator;
import dev.eigenworks.electrical.RcTransient;
import dev.eigenworks.electrical.RlTransient;
import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.mathematics.Vector;
import dev.eigenworks.networking.CanBus;
import dev.eigenworks.networking.CanFrame;
import dev.eigenworks.networking.CanNode;
import dev.eigenworks.networking.ImpairedLink;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.robotics.PlanarThreeLinkKinematics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-side diagnostics for Milestone 13 systems. */
public final class AdvancedEngineeringConsoleBlockEntity extends BlockEntity {
	public enum Module { CAN_NETWORK, FPU, MATRIX_ACCELERATOR, TRANSIENT_CIRCUIT, THREE_LINK_ROBOT }
	private Module selected = Module.CAN_NETWORK;
	private String result = "ADVANCED SYSTEMS READY";
	private int lastCycles;
	private boolean successful = true;
	public AdvancedEngineeringConsoleBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADVANCED_ENGINEERING_CONSOLE, pos, state); }
	public void nextModule() { selected = Module.values()[(selected.ordinal() + 1) % Module.values().length]; result = "SELECTED " + selected; successful = true; lastCycles = 0; setChanged(); }
	public boolean runDiagnostic() {
		try {
			result = switch (selected) {
				case CAN_NETWORK -> diagnoseCan();
				case FPU -> diagnoseFpu();
				case MATRIX_ACCELERATOR -> diagnoseMatrix();
				case TRANSIENT_CIRCUIT -> diagnoseCircuit();
				case THREE_LINK_ROBOT -> diagnoseRobot();
			};
			successful = true;
		} catch (RuntimeException exception) { result = "FAULT: " + exception.getMessage(); successful = false; lastCycles = 0; }
		setChanged(); return successful;
	}
	private String diagnoseCan() {
		CanBus bus = new CanBus(500_000); CanNode computer = new CanNode("computer"), joint = new CanNode("joint"), monitor = new CanNode("monitor");
		bus.attach(computer); bus.attach(joint); bus.attach(monitor);
		bus.transmit(computer, new CanFrame(0x200, new byte[] {90}), 0); bus.transmit(joint, new CanFrame(0x100, new byte[] {42}), 0);
		bus.advance(0); bus.advance(1_000); int first = monitor.poll().orElseThrow().identifier(); bus.advance(1_000); bus.advance(2_000); int second = monitor.poll().orElseThrow().identifier();
		ImpairedLink link = new ImpairedLink(2_000, 0, 0, 10_000, 10_000, 13); link.send(new byte[] {1, 2}, 0); boolean delayed = link.receive(1_000).isEmpty() && link.receive(2_200).isPresent();
		lastCycles = 0; return "CAN 500k: 0x%03X then 0x%03X, losses=%d, delayed=%s".formatted(first, second, bus.arbitrationLosses(), delayed);
	}
	private String diagnoseFpu() { var value = new FloatingPointUnit().execute(FloatingPointUnit.Operation.SQRT, 2, 0); lastCycles = value.cycles(); return "sqrt(2)=%.9f, %d cycles".formatted(value.value(), lastCycles); }
	private String diagnoseMatrix() { var value = new MatrixAccelerator(8).multiply(new Matrix(new double[][] {{1, 2}, {3, 4}}), new Vector(5, 6)); lastCycles = value.cycles(); return "A*v=[%.1f, %.1f], %d cycles".formatted(value.value().get(0), value.value().get(1), lastCycles); }
	private String diagnoseCircuit() { RcTransient rc = new RcTransient(1_000, 0.001, 0); RlTransient rl = new RlTransient(10, 0.1, 0); for (int i = 0; i < 100; i++) { rc.step(5, 0.01); rl.step(20, 0.01); } lastCycles = 100; return "RC=%.3f V, RL=%.3f A after 1 s".formatted(rc.voltage(), rl.current()); }
	private String diagnoseRobot() { PlanarThreeLinkKinematics arm = new PlanarThreeLinkKinematics(1, 1, .5); var ik = arm.inverse(1.4, 1, new Vector(.2, .2, .2), .08, 500, 1e-5); if (!ik.converged()) throw new IllegalStateException(ik.diagnostic()); var pose = arm.forward(ik.joints()); lastCycles = ik.iterations(); return "3R IK=(%.3f, %.3f) m, iterations=%d".formatted(pose.xMeters(), pose.yMeters(), lastCycles); }
	public Module selectedModule() { return selected; }
	public String result() { return result; }
	public int lastCycles() { return lastCycles; }
	public boolean successful() { return successful; }
	@Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); selected = Module.values()[Math.clamp(input.getIntOr("module", 0), 0, Module.values().length - 1)]; result = input.getStringOr("result", "ADVANCED SYSTEMS READY"); lastCycles = input.getIntOr("cycles", 0); successful = input.getBooleanOr("successful", true); }
	@Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putInt("module", selected.ordinal()); output.putString("result", result); output.putInt("cycles", lastCycles); output.putBoolean("successful", successful); }
}
