package dev.eigenworks.computer.cpu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Frozen Eigen-8 opcode table, instruction sizes, and logical cycle costs. */
public enum CpuInstruction {
	NOP(0x00, OperandFormat.NONE, 1),
	LOAD_MEMORY(0x10, OperandFormat.REGISTER_ADDRESS16, 4),
	LOAD_IMMEDIATE(0x11, OperandFormat.REGISTER_IMMEDIATE8, 2),
	STORE(0x12, OperandFormat.ADDRESS16_REGISTER, 4),
	MOV(0x13, OperandFormat.TWO_REGISTERS, 1),
	PUSH(0x14, OperandFormat.REGISTER, 2),
	POP(0x15, OperandFormat.REGISTER, 2),
	ADD(0x20, OperandFormat.THREE_REGISTERS, 1),
	SUB(0x21, OperandFormat.THREE_REGISTERS, 1),
	MUL(0x22, OperandFormat.THREE_REGISTERS, 3),
	DIV(0x23, OperandFormat.THREE_REGISTERS, 4),
	MOD(0x24, OperandFormat.THREE_REGISTERS, 4),
	AND(0x25, OperandFormat.THREE_REGISTERS, 1),
	OR(0x26, OperandFormat.THREE_REGISTERS, 1),
	XOR(0x27, OperandFormat.THREE_REGISTERS, 1),
	NOT(0x28, OperandFormat.TWO_REGISTERS, 1),
	SHL(0x29, OperandFormat.TWO_REGISTERS, 1),
	SHR(0x2A, OperandFormat.TWO_REGISTERS, 1),
	CMP(0x2B, OperandFormat.TWO_REGISTERS, 1),
	JMP(0x30, OperandFormat.ADDRESS16, 2),
	JE(0x31, OperandFormat.ADDRESS16, 2),
	JNE(0x32, OperandFormat.ADDRESS16, 2),
	JG(0x33, OperandFormat.ADDRESS16, 2),
	JL(0x34, OperandFormat.ADDRESS16, 2),
	CALL(0x35, OperandFormat.ADDRESS16, 4),
	RET(0x36, OperandFormat.NONE, 4),
	IN(0x40, OperandFormat.REGISTER_PORT8, 3),
	OUT(0x41, OperandFormat.PORT8_REGISTER, 3),
	INT(0x42, OperandFormat.VECTOR8, 6),
	HALT(0xFF, OperandFormat.NONE, 1);

	private static final Map<Integer, CpuInstruction> BY_OPCODE;

	static {
		Map<Integer, CpuInstruction> instructions = new HashMap<>();
		for (CpuInstruction instruction : values()) {
			if (instructions.put(instruction.opcode, instruction) != null) {
				throw new IllegalStateException("Duplicate CPU opcode: " + instruction.opcode);
			}
		}
		BY_OPCODE = Collections.unmodifiableMap(instructions);
	}

	private final int opcode;
	private final OperandFormat format;
	private final int cycles;

	CpuInstruction(int opcode, OperandFormat format, int cycles) {
		this.opcode = opcode;
		this.format = format;
		this.cycles = cycles;
	}

	public int opcode() {
		return opcode;
	}

	public OperandFormat format() {
		return format;
	}

	public int cycles() {
		return cycles;
	}

	public int size() {
		return switch (format) {
			case NONE -> 1;
			case REGISTER, VECTOR8 -> 2;
			case TWO_REGISTERS, REGISTER_IMMEDIATE8, ADDRESS16,
					REGISTER_PORT8, PORT8_REGISTER -> 3;
			case THREE_REGISTERS, REGISTER_ADDRESS16, ADDRESS16_REGISTER -> 4;
		};
	}

	public static CpuInstruction decode(int opcode) {
		return BY_OPCODE.get(opcode & 0xFF);
	}
}
