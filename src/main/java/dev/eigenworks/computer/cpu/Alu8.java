package dev.eigenworks.computer.cpu;

/** Mathematically defined eight-bit arithmetic and logic unit. */
public final class Alu8 {
	private Alu8() {
	}

	public static AluResult add(int left, int right) {
		left = byteValue(left);
		right = byteValue(right);
		int sum = left + right;
		int result = sum & 0xFF;
		boolean overflow = ((~(left ^ right) & (left ^ result)) & 0x80) != 0;
		return result(result, sum > 0xFF, overflow);
	}

	public static AluResult subtract(int left, int right) {
		left = byteValue(left);
		right = byteValue(right);
		int result = (left - right) & 0xFF;
		boolean overflow = (((left ^ right) & (left ^ result)) & 0x80) != 0;
		return result(result, left >= right, overflow);
	}

	public static AluResult multiply(int left, int right) {
		left = byteValue(left);
		right = byteValue(right);
		int product = left * right;
		int signedProduct = (byte) left * (byte) right;
		return result(product & 0xFF, product > 0xFF, signedProduct < -128 || signedProduct > 127);
	}

	public static AluResult and(int left, int right) {
		return result(byteValue(left) & byteValue(right), false, false);
	}

	public static AluResult or(int left, int right) {
		return result(byteValue(left) | byteValue(right), false, false);
	}

	public static AluResult xor(int left, int right) {
		return result(byteValue(left) ^ byteValue(right), false, false);
	}

	public static AluResult not(int value) {
		return result((~byteValue(value)) & 0xFF, false, false);
	}

	public static AluResult shiftLeft(int value) {
		value = byteValue(value);
		int result = (value << 1) & 0xFF;
		boolean carry = (value & 0x80) != 0;
		return result(result, carry, ((result & 0x80) != 0) != carry);
	}

	public static AluResult shiftRight(int value) {
		value = byteValue(value);
		return result(value >>> 1, (value & 1) != 0, false);
	}

	public static AluResult value(int value) {
		return result(byteValue(value), false, false);
	}

	private static AluResult result(int value, boolean carry, boolean overflow) {
		value &= 0xFF;
		return new AluResult(value, new AluFlags(value == 0, (value & 0x80) != 0, carry, overflow));
	}

	private static int byteValue(int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("ALU operand must be an unsigned byte: " + value);
		}
		return value;
	}
}
