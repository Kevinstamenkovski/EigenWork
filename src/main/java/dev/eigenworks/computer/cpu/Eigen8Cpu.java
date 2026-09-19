package dev.eigenworks.computer.cpu;

import java.util.Arrays;

import dev.eigenworks.computer.memory.ByteMemory;
import dev.eigenworks.computer.memory.MemoryAccessException;

/**
 * Deterministic educational eight-bit CPU. Instructions are executed atomically;
 * cycle budgets model throughput without simulating physical nanoseconds.
 */
public final class Eigen8Cpu {
	public static final int REGISTER_COUNT = 8;
	public static final int DEFAULT_STACK_POINTER = 0xFEFF;

	private final ByteMemory memory;
	private final CpuIo io;
	private final int[] registers = new int[REGISTER_COUNT];
	private int programCounter;
	private int stackPointer = DEFAULT_STACK_POINTER;
	private AluFlags flags = new AluFlags(false, false, false, false);
	private CpuStatus status = CpuStatus.PAUSED;
	private CpuFault fault = CpuFault.NONE;
	private String faultMessage = "";
	private long totalCycles;
	private long totalInstructions;
	private int pendingInterrupt = -1;

	public Eigen8Cpu(ByteMemory memory) {
		this(memory, CpuIo.DISCONNECTED);
	}

	public Eigen8Cpu(ByteMemory memory, CpuIo io) {
		if (memory == null || io == null) {
			throw new NullPointerException("CPU memory and I/O are required");
		}
		if (memory.size() > 65_536) {
			throw new IllegalArgumentException("Eigen-8 supports at most 65536 addressable bytes");
		}
		this.memory = memory;
		this.io = io;
	}

	public void reset() {
		Arrays.fill(registers, 0);
		programCounter = 0;
		stackPointer = DEFAULT_STACK_POINTER;
		flags = new AluFlags(false, false, false, false);
		status = CpuStatus.PAUSED;
		fault = CpuFault.NONE;
		faultMessage = "";
		totalCycles = 0;
		totalInstructions = 0;
		pendingInterrupt = -1;
	}

	public void run() {
		if (status == CpuStatus.HALTED || status == CpuStatus.FAULTED) {
			return;
		}
		status = CpuStatus.RUNNING;
	}

	public void pause() {
		if (status == CpuStatus.RUNNING) {
			status = CpuStatus.PAUSED;
		}
	}

	public CpuRunReport runCycles(int cycleBudget) {
		if (cycleBudget <= 0) {
			throw new IllegalArgumentException("CPU cycle budget must be positive");
		}
		if (status != CpuStatus.RUNNING) {
			return new CpuRunReport(0, 0, status, fault);
		}
		int instructions = 0;
		int cycles = 0;
		while (status == CpuStatus.RUNNING && cycles < cycleBudget) {
			int consumed = executeOne();
			if (consumed <= 0) {
				break;
			}
			cycles += consumed;
			instructions++;
		}
		return new CpuRunReport(instructions, cycles, status, fault);
	}

	public CpuRunReport step() {
		if (status == CpuStatus.HALTED || status == CpuStatus.FAULTED) {
			return new CpuRunReport(0, 0, status, fault);
		}
		status = CpuStatus.RUNNING;
		int cycles = executeOne();
		if (status == CpuStatus.RUNNING) {
			status = CpuStatus.PAUSED;
		}
		return new CpuRunReport(cycles > 0 ? 1 : 0, Math.max(0, cycles), status, fault);
	}

	private int executeOne() {
		try {
			if (pendingInterrupt >= 0) {
				int vector = pendingInterrupt;
				pendingInterrupt = -1;
				pushWord(programCounter);
				programCounter = readWord(vector * 2);
				totalCycles += CpuInstruction.INT.cycles();
				totalInstructions++;
				return CpuInstruction.INT.cycles();
			}
			int opcodeAddress = programCounter;
			int opcode = fetchByte();
			CpuInstruction instruction = CpuInstruction.decode(opcode);
			if (instruction == null) {
				setFault(CpuFault.ILLEGAL_INSTRUCTION,
						"CPU ILLEGAL INSTRUCTION 0x%02X at 0x%04X".formatted(opcode, opcodeAddress));
				return 0;
			}
			execute(instruction);
			if (status != CpuStatus.FAULTED) {
				totalCycles += instruction.cycles();
				totalInstructions++;
			}
			return instruction.cycles();
		} catch (MemoryAccessException exception) {
			setFault(CpuFault.MEMORY_ACCESS, exception.getMessage());
		} catch (IllegalArgumentException exception) {
			if (status != CpuStatus.FAULTED) {
				setFault(CpuFault.INVALID_STATE, exception.getMessage());
			}
		} catch (RuntimeException exception) {
			setFault(CpuFault.IO_FAILURE, exception.getClass().getSimpleName() + ": " + exception.getMessage());
		}
		return 0;
	}

	private void execute(CpuInstruction instruction) {
		switch (instruction) {
			case NOP -> { }
			case LOAD_MEMORY -> setRegister(fetchRegister(), memory.read(fetchAddress()));
			case LOAD_IMMEDIATE -> setRegister(fetchRegister(), fetchByte());
			case STORE -> {
				int address = fetchAddress();
				memory.write(address, register(fetchRegister()));
			}
			case MOV -> setRegister(fetchRegister(), register(fetchRegister()));
			case PUSH -> push(register(fetchRegister()));
			case POP -> setRegister(fetchRegister(), pop());
			case ADD -> binary(Alu8::add);
			case SUB -> binary(Alu8::subtract);
			case MUL -> binary(Alu8::multiply);
			case DIV -> divide(false);
			case MOD -> divide(true);
			case AND -> binary(Alu8::and);
			case OR -> binary(Alu8::or);
			case XOR -> binary(Alu8::xor);
			case NOT -> unary(Alu8::not);
			case SHL -> unary(Alu8::shiftLeft);
			case SHR -> unary(Alu8::shiftRight);
			case CMP -> {
				int left = register(fetchRegister());
				int right = register(fetchRegister());
				flags = Alu8.subtract(left, right).flags();
			}
			case JMP -> programCounter = fetchAddress();
			case JE -> branch(flags.zero());
			case JNE -> branch(!flags.zero());
			case JG -> branch(!flags.zero() && flags.negative() == flags.overflow());
			case JL -> branch(flags.negative() != flags.overflow());
			case CALL -> {
				int target = fetchAddress();
				pushWord(programCounter);
				programCounter = target;
			}
			case RET -> programCounter = popWord();
			case IN -> {
				int destination = fetchRegister();
				int input = io.read(fetchByte());
				setRegister(destination, input);
			}
			case OUT -> {
				int port = fetchByte();
				io.write(port, register(fetchRegister()));
			}
			case INT -> {
				int vector = fetchByte();
				pushWord(programCounter);
				programCounter = readWord(vector * 2);
			}
			case HALT -> status = CpuStatus.HALTED;
		}
	}

	private void binary(BinaryOperation operation) {
		int destination = fetchRegister();
		int left = register(fetchRegister());
		int right = register(fetchRegister());
		apply(destination, operation.apply(left, right));
	}

	private void unary(UnaryOperation operation) {
		int destination = fetchRegister();
		apply(destination, operation.apply(register(fetchRegister())));
	}

	private void divide(boolean remainder) {
		int destination = fetchRegister();
		int left = register(fetchRegister());
		int right = register(fetchRegister());
		if (right == 0) {
			setFault(CpuFault.DIVIDE_BY_ZERO, "CPU DIVIDE BY ZERO");
			return;
		}
		apply(destination, Alu8.value(remainder ? left % right : left / right));
	}

	private void branch(boolean condition) {
		int target = fetchAddress();
		if (condition) {
			programCounter = target;
		}
	}

	private void apply(int destination, AluResult result) {
		registers[destination] = result.value();
		flags = result.flags();
	}

	private void setRegister(int register, int value) {
		registers[register] = requireByte(value);
		flags = Alu8.value(value).flags();
	}

	private int fetchRegister() {
		int index = fetchByte();
		if (index < 0 || index >= REGISTER_COUNT) {
			setFault(CpuFault.INVALID_REGISTER, "CPU INVALID REGISTER: " + index);
			throw new IllegalArgumentException("Invalid register index " + index);
		}
		return index;
	}

	private int fetchAddress() {
		return (fetchByte() << 8) | fetchByte();
	}

	private int fetchByte() {
		int value = memory.read(programCounter);
		programCounter = (programCounter + 1) & 0xFFFF;
		return value;
	}

	private int readWord(int address) {
		return (memory.read(address & 0xFFFF) << 8) | memory.read((address + 1) & 0xFFFF);
	}

	private void pushWord(int value) {
		push(value & 0xFF);
		push((value >>> 8) & 0xFF);
	}

	private int popWord() {
		int high = pop();
		int low = pop();
		return (high << 8) | low;
	}

	private void push(int value) {
		memory.write(stackPointer, requireByte(value));
		stackPointer = (stackPointer - 1) & 0xFFFF;
	}

	private int pop() {
		stackPointer = (stackPointer + 1) & 0xFFFF;
		return memory.read(stackPointer);
	}

	private int register(int index) {
		return registers[index];
	}

	private void setFault(CpuFault fault, String message) {
		this.fault = fault;
		faultMessage = message == null ? "" : message;
		status = CpuStatus.FAULTED;
	}

	public int registerValue(int index) {
		if (index < 0 || index >= REGISTER_COUNT) {
			throw new IllegalArgumentException("Register index must be between 0 and 7");
		}
		return registers[index];
	}

	public int[] registerSnapshot() {
		return registers.clone();
	}

	public int programCounter() {
		return programCounter;
	}

	public int stackPointer() {
		return stackPointer;
	}

	public AluFlags flags() {
		return flags;
	}

	public CpuStatus status() {
		return status;
	}

	public CpuFault fault() {
		return fault;
	}

	public String faultMessage() {
		return faultMessage;
	}

	public long totalCycles() {
		return totalCycles;
	}

	public long totalInstructions() {
		return totalInstructions;
	}

	/** Queues one hardware interrupt for service before the next instruction. */
	public boolean requestInterrupt(int vector) {
		if (vector < 0 || vector > 0xFF) {
			throw new IllegalArgumentException("Interrupt vector must be between 0 and 255");
		}
		if (pendingInterrupt >= 0 || status == CpuStatus.HALTED || status == CpuStatus.FAULTED) {
			return false;
		}
		pendingInterrupt = vector;
		return true;
	}

	public int pendingInterrupt() {
		return pendingInterrupt;
	}

	public CpuInstruction currentInstruction() {
		return CpuInstruction.decode(memory.read(programCounter));
	}

	public void restoreState(
			int[] restoredRegisters,
			int restoredProgramCounter,
			int restoredStackPointer,
			AluFlags restoredFlags,
			CpuStatus restoredStatus,
			long restoredCycles,
			long restoredInstructions) {
		if (restoredRegisters == null || restoredRegisters.length != REGISTER_COUNT) {
			throw new IllegalArgumentException("CPU state requires eight registers");
		}
		for (int value : restoredRegisters) {
			requireByte(value);
		}
		System.arraycopy(restoredRegisters, 0, registers, 0, REGISTER_COUNT);
		programCounter = requireWord(restoredProgramCounter);
		stackPointer = requireWord(restoredStackPointer);
		flags = restoredFlags == null ? new AluFlags(false, false, false, false) : restoredFlags;
		status = restoredStatus == CpuStatus.RUNNING ? CpuStatus.PAUSED : restoredStatus;
		fault = status == CpuStatus.FAULTED ? CpuFault.INVALID_STATE : CpuFault.NONE;
		faultMessage = status == CpuStatus.FAULTED ? "Restored faulted CPU state" : "";
		totalCycles = Math.max(0L, restoredCycles);
		totalInstructions = Math.max(0L, restoredInstructions);
		pendingInterrupt = -1;
	}

	private static int requireByte(int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("Value must be an unsigned byte: " + value);
		}
		return value;
	}

	private static int requireWord(int value) {
		if (value < 0 || value > 0xFFFF) {
			throw new IllegalArgumentException("Value must be an unsigned word: " + value);
		}
		return value;
	}

	@FunctionalInterface
	private interface BinaryOperation {
		AluResult apply(int left, int right);
	}

	@FunctionalInterface
	private interface UnaryOperation {
		AluResult apply(int value);
	}
}
