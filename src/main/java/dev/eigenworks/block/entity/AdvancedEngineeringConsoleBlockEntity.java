package dev.eigenworks.block.entity;

import dev.eigenworks.computer.accelerator.FloatingPointUnit;
import dev.eigenworks.computer.accelerator.MatrixAccelerator;
import dev.eigenworks.computer.accelerator.AdvancedConsoleMenu;
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
import net.minecraft.network.chat.Component;import net.minecraft.world.MenuProvider;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-side diagnostics for Milestone 13 systems. */
public final class AdvancedEngineeringConsoleBlockEntity extends BlockEntity implements MenuProvider {
	public enum Module { CAN_NETWORK, FPU, MATRIX_ACCELERATOR, TRANSIENT_CIRCUIT, THREE_LINK_ROBOT }
	private Module selected = Module.CAN_NETWORK;
	private String result = "ADVANCED SYSTEMS READY";
	private int lastCycles;
	private boolean successful = true;
	private int bitrate=500_000,latencyMicros=2_000;private double ikDamping=.08;
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
		CanBus bus = new CanBus(bitrate); CanNode computer = new CanNode("computer"), joint = new CanNode("joint"), monitor = new CanNode("monitor");
		bus.attach(computer); bus.attach(joint); bus.attach(monitor);
		bus.transmit(computer, new CanFrame(0x200, new byte[] {90}), 0); bus.transmit(joint, new CanFrame(0x100, new byte[] {42}), 0);
		bus.advance(0); bus.advance(1_000); int first = monitor.poll().orElseThrow().identifier(); bus.advance(1_000); bus.advance(2_000); int second = monitor.poll().orElseThrow().identifier();
		ImpairedLink link = new ImpairedLink(latencyMicros, 0, 0, 10_000, 100_000, 13); link.send(new byte[] {1, 2}, 0); boolean delayed = link.receive(Math.max(0,latencyMicros-1)).isEmpty() && link.receive(latencyMicros+1_000).isPresent();
		lastCycles = 0; return "CAN %dk: 0x%03X then 0x%03X, losses=%d, delayed=%s".formatted(bitrate/1000,first, second, bus.arbitrationLosses(), delayed);
	}
	private String diagnoseFpu() { var value = new FloatingPointUnit().execute(FloatingPointUnit.Operation.SQRT, 2, 0); lastCycles = value.cycles(); return "sqrt(2)=%.9f, %d cycles".formatted(value.value(), lastCycles); }
	private String diagnoseMatrix() { var value = new MatrixAccelerator(8).multiply(new Matrix(new double[][] {{1, 2}, {3, 4}}), new Vector(5, 6)); lastCycles = value.cycles(); return "A*v=[%.1f, %.1f], %d cycles".formatted(value.value().get(0), value.value().get(1), lastCycles); }
	private String diagnoseCircuit() { RcTransient rc = new RcTransient(1_000, 0.001, 0); RlTransient rl = new RlTransient(10, 0.1, 0); for (int i = 0; i < 100; i++) { rc.step(5, 0.01); rl.step(20, 0.01); } lastCycles = 100; return "RC=%.3f V, RL=%.3f A after 1 s".formatted(rc.voltage(), rl.current()); }
	private String diagnoseRobot() { PlanarThreeLinkKinematics arm = new PlanarThreeLinkKinematics(1, 1, .5); var ik = arm.inverse(1.4, 1, new Vector(.2, .2, .2), ikDamping, 500, 1e-5); if (!ik.converged()) throw new IllegalStateException(ik.diagnostic()); var pose = arm.forward(ik.joints()); lastCycles = ik.iterations(); return "3R IK=(%.3f, %.3f) m, iterations=%d".formatted(pose.xMeters(), pose.yMeters(), lastCycles); }
	public void nextBitrate(){bitrate=switch(bitrate){case 125_000->250_000;case 250_000->500_000;case 500_000->1_000_000;default->125_000;};setChanged();}public void nextLatency(){latencyMicros=switch(latencyMicros){case 0->20_000;case 20_000->80_000;default->0;};setChanged();}public void nextDamping(){ikDamping=ikDamping<.03?.08:ikDamping<.1?.2:.02;setChanged();}
	public int bitrate(){return bitrate;}public int latencyMicros(){return latencyMicros;}public double ikDamping(){return ikDamping;}
	public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}@Override public Component getDisplayName(){return Component.translatable("screen.eigenworks.advanced_console");}@Override public AbstractContainerMenu createMenu(int id,Inventory i,Player p){return new AdvancedConsoleMenu(id,this);}public ContainerData menuData(){return new ContainerData(){public int get(int i){return switch(i){case 0->selected.ordinal();case 1->bitrate;case 2->latencyMicros/1000;case 3->(int)Math.round(ikDamping*1000);case 4->lastCycles;case 5->successful?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return AdvancedConsoleMenu.DATA_COUNT;}};}
	public Module selectedModule() { return selected; }
	public String result() { return result; }
	public int lastCycles() { return lastCycles; }
	public boolean successful() { return successful; }
	@Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); selected = Module.values()[Math.clamp(input.getIntOr("module", 0), 0, Module.values().length - 1)]; result = input.getStringOr("result", "ADVANCED SYSTEMS READY"); lastCycles = input.getIntOr("cycles", 0); successful = input.getBooleanOr("successful", true);bitrate=switch(input.getIntOr("bitrate",500_000)){case 125_000->125_000;case 250_000->250_000;case 1_000_000->1_000_000;default->500_000;};latencyMicros=switch(input.getIntOr("latency",2_000)){case 0->0;case 20_000->20_000;case 80_000->80_000;default->2_000;};ikDamping=Math.clamp(input.getDoubleOr("ik_damping",.08),.001,1); }
	@Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putInt("module", selected.ordinal()); output.putString("result", result); output.putInt("cycles", lastCycles); output.putBoolean("successful", successful);output.putInt("bitrate",bitrate);output.putInt("latency",latencyMicros);output.putDouble("ik_damping",ikDamping); }
}
