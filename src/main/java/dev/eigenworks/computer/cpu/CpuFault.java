package dev.eigenworks.computer.cpu;

/** Stable engineering fault codes surfaced by the CPU debugger. */
public enum CpuFault {
	NONE,
	ILLEGAL_INSTRUCTION,
	INVALID_REGISTER,
	DIVIDE_BY_ZERO,
	MEMORY_ACCESS,
	IO_FAILURE,
	INVALID_STATE
}
