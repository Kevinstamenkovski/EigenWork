package dev.eigenworks.block.entity;

import dev.eigenworks.computer.ComputerMenu;
import dev.eigenworks.computer.assembly.AssemblyDiagnostic;
import dev.eigenworks.computer.assembly.AssemblyResult;
import dev.eigenworks.computer.assembly.Eigen8Assembler;
import dev.eigenworks.computer.cpu.AluFlags;
import dev.eigenworks.computer.cpu.CpuInstruction;
import dev.eigenworks.computer.cpu.CpuStatus;
import dev.eigenworks.computer.cpu.Eigen8Cpu;
import dev.eigenworks.computer.cpu.PortBank;
import dev.eigenworks.computer.memory.RamMemory;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-authoritative adapter for the pure Eigen-8 computer. */
public final class ComputerBlockEntity extends BlockEntity implements LoadedSimulationDevice, MenuProvider {
	public static final int RAM_SIZE = 65_536;
	public static final int DEMO_RESULT_ADDRESS = 0x0020;
	public static final int MAX_SOURCE_CHARS = 32_000;
	private static final long UPDATE_PERIOD_MICROS = 50_000L;
	private static final int CYCLES_PER_TICK = 1_000;

	private final RamMemory memory = new RamMemory(RAM_SIZE);
	private final PortBank ports = new PortBank();
	private final Eigen8Cpu cpu = new Eigen8Cpu(memory, ports);
	private final Eigen8Assembler assembler = new Eigen8Assembler();
	private String programSource = "";
	private String lastDiagnostic = "No program loaded";
	private String simulationId;

	public ComputerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.COMPUTER, pos, state);
	}

	@Override
	public void bindToLevel(ServerLevel level) {
		simulationId = "computer:" + level.dimension().identifier() + ":" + worldPosition.asLong();
	}

	@Override
	public void unbindFromLevel() {
		simulationId = null;
	}

	@Override
	public String simulationId() {
		if (simulationId == null) {
			throw new IllegalStateException("Computer is not bound to a server level");
		}
		return simulationId;
	}

	@Override
	public long updatePeriodMicros() {
		return UPDATE_PERIOD_MICROS;
	}

	@Override
	public void simulate(SimulationContext context) {
		if (cpu.status() == CpuStatus.RUNNING) {
			cpu.runCycles(CYCLES_PER_TICK);
			setChanged();
		}
	}

	public AssemblyResult assembleAndLoad(String source) {
		if (source == null || source.length() > MAX_SOURCE_CHARS) {
			AssemblyDiagnostic diagnostic = new AssemblyDiagnostic(1, 1,
					"Program source must contain at most " + MAX_SOURCE_CHARS + " characters", "");
			lastDiagnostic = diagnostic.displayMessage();
			return new AssemblyResult(null, java.util.List.of(diagnostic));
		}
		AssemblyResult result = assembler.assemble(source);
		if (!result.successful()) {
			lastDiagnostic = result.diagnostics().getFirst().displayMessage();
			return result;
		}
		byte[] bytecode = result.program().bytecode();
		memory.clear();
		memory.load(0, bytecode);
		ports.clear();
		cpu.reset();
		programSource = source;
		lastDiagnostic = "ASSEMBLED " + bytecode.length + " BYTES";
		setChanged();
		return result;
	}

	public void runCpu() { cpu.run(); setChanged(); }
	public void pauseCpu() { cpu.pause(); setChanged(); }
	public void resetCpu() { cpu.reset(); setChanged(); }
	public void stepCpu() { cpu.step(); setChanged(); }
	public int memoryValue(int address) { return memory.read(address); }
	public Eigen8Cpu cpu() { return cpu; }
	public String programSource() { return programSource; }
	public String lastDiagnostic() { return lastDiagnostic; }

	public boolean stillValid(Player player) {
		return level != null && level.getBlockEntity(worldPosition) == this
				&& player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
						worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("screen.eigenworks.computer");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new ComputerMenu(containerId, this);
	}

	public ContainerData debugData() {
		return new ContainerData() {
			@Override
			public int get(int index) {
				return switch (index) {
					case 0 -> cpu.programCounter();
					case 1 -> cpu.stackPointer();
					case 2, 3, 4, 5, 6, 7, 8, 9 -> cpu.registerValue(index - 2);
					case 10 -> cpu.flags().mask();
					case 11 -> cpu.status().ordinal();
					case 12 -> {
						CpuInstruction instruction = cpu.currentInstruction();
						yield instruction == null ? 0x100 : instruction.opcode();
					}
					case 13 -> memory.read(DEMO_RESULT_ADDRESS);
					case 14 -> (int) (cpu.totalCycles() & 0xFFFF);
					case 15 -> (int) ((cpu.totalCycles() >>> 16) & 0xFFFF);
					default -> 0;
				};
			}

			@Override public void set(int index, int value) { }
			@Override public int getCount() { return ComputerMenu.DATA_COUNT; }
		};
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		programSource = input.getStringOr("program_source", "");
		lastDiagnostic = input.getStringOr("last_diagnostic", "No program loaded");
		input.getIntArray("ram").ifPresent(packed -> restorePackedRam(packed));
		int[] registers = input.getIntArray("registers").orElse(new int[Eigen8Cpu.REGISTER_COUNT]);
		if (registers.length != Eigen8Cpu.REGISTER_COUNT) {
			registers = new int[Eigen8Cpu.REGISTER_COUNT];
		}
		CpuStatus restoredStatus = enumValue(CpuStatus.values(), input.getIntOr("status", CpuStatus.PAUSED.ordinal()), CpuStatus.PAUSED);
		cpu.restoreState(registers, input.getIntOr("pc", 0), input.getIntOr("sp", Eigen8Cpu.DEFAULT_STACK_POINTER),
				AluFlags.fromMask(input.getIntOr("flags", 0)), restoredStatus,
				input.getLongOr("cycles", 0L), input.getLongOr("instructions", 0L));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("program_source", programSource);
		output.putString("last_diagnostic", lastDiagnostic);
		output.putIntArray("ram", packRam(memory.copyBytes()));
		output.putIntArray("registers", cpu.registerSnapshot());
		output.putInt("pc", cpu.programCounter());
		output.putInt("sp", cpu.stackPointer());
		output.putInt("flags", cpu.flags().mask());
		output.putInt("status", cpu.status().ordinal());
		output.putLong("cycles", cpu.totalCycles());
		output.putLong("instructions", cpu.totalInstructions());
	}

	private static int[] packRam(byte[] bytes) {
		int[] packed = new int[bytes.length / 4];
		for (int index = 0; index < packed.length; index++) {
			int offset = index * 4;
			packed[index] = Byte.toUnsignedInt(bytes[offset])
					| (Byte.toUnsignedInt(bytes[offset + 1]) << 8)
					| (Byte.toUnsignedInt(bytes[offset + 2]) << 16)
					| (Byte.toUnsignedInt(bytes[offset + 3]) << 24);
		}
		return packed;
	}

	private void restorePackedRam(int[] packed) {
		if (packed.length != RAM_SIZE / 4) {
			return;
		}
		byte[] bytes = new byte[RAM_SIZE];
		for (int index = 0; index < packed.length; index++) {
			int offset = index * 4;
			bytes[offset] = (byte) packed[index];
			bytes[offset + 1] = (byte) (packed[index] >>> 8);
			bytes[offset + 2] = (byte) (packed[index] >>> 16);
			bytes[offset + 3] = (byte) (packed[index] >>> 24);
		}
		memory.restore(bytes);
	}

	private static <T> T enumValue(T[] values, int ordinal, T fallback) {
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
	}
}
