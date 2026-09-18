package dev.eigenworks.digital;

import java.util.Objects;

import dev.eigenworks.signal.DigitalWord;

/** Width-safe combinational logic operations. */
public final class DigitalLogic {
	private DigitalLogic() {
	}

	public static DigitalWord not(DigitalWord input) {
		Objects.requireNonNull(input, "input");
		return new DigitalWord(input.width(), ~input.value());
	}

	public static DigitalWord and(DigitalWord left, DigitalWord right) {
		requireSameWidth(left, right);
		return new DigitalWord(left.width(), left.value() & right.value());
	}

	public static DigitalWord or(DigitalWord left, DigitalWord right) {
		requireSameWidth(left, right);
		return new DigitalWord(left.width(), left.value() | right.value());
	}

	public static DigitalWord xor(DigitalWord left, DigitalWord right) {
		requireSameWidth(left, right);
		return new DigitalWord(left.width(), left.value() ^ right.value());
	}

	public static DigitalWord nand(DigitalWord left, DigitalWord right) {
		return not(and(left, right));
	}

	public static DigitalWord nor(DigitalWord left, DigitalWord right) {
		return not(or(left, right));
	}

	public static DigitalWord multiplex(boolean select, DigitalWord lowInput, DigitalWord highInput) {
		requireSameWidth(lowInput, highInput);
		return select ? highInput : lowInput;
	}

	private static void requireSameWidth(DigitalWord left, DigitalWord right) {
		Objects.requireNonNull(left, "left");
		Objects.requireNonNull(right, "right");
		if (left.width() != right.width()) {
			throw new IllegalArgumentException(
					"Digital word width mismatch: " + left.width() + " != " + right.width());
		}
	}
}

