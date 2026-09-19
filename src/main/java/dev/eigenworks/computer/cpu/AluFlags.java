package dev.eigenworks.computer.cpu;

/** Z/N/C/V flags produced by an eight-bit ALU operation. */
public record AluFlags(boolean zero, boolean negative, boolean carry, boolean overflow) {
	public int mask() {
		return (zero ? 1 : 0) | (negative ? 2 : 0) | (carry ? 4 : 0) | (overflow ? 8 : 0);
	}

	public static AluFlags fromMask(int mask) {
		return new AluFlags((mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0);
	}
}
