package dev.eigenworks.computer.cpu;

/** Encoded operand layout following an instruction opcode. */
public enum OperandFormat {
	NONE,
	REGISTER,
	TWO_REGISTERS,
	THREE_REGISTERS,
	REGISTER_IMMEDIATE8,
	REGISTER_ADDRESS16,
	ADDRESS16_REGISTER,
	ADDRESS16,
	REGISTER_PORT8,
	PORT8_REGISTER,
	VECTOR8
}
