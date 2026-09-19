package dev.eigenworks.computer.cpu;

/** Unsigned byte result and the flags generated with it. */
public record AluResult(int value, AluFlags flags) {
	public AluResult {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("ALU result must be an unsigned byte");
		}
		if (flags == null) {
			throw new NullPointerException("flags");
		}
	}
}
