package dev.eigenworks.signal;

/** Unsigned 1-64 bit word with values masked to its configured width. */
public record DigitalWord(int width, long value) implements SignalValue {
	public DigitalWord {
		if (width < 1 || width > Long.SIZE) {
			throw new IllegalArgumentException("Digital word width must be between 1 and 64");
		}
		if (width < Long.SIZE) {
			value &= (1L << width) - 1L;
		}
	}

	public boolean bit(int index) {
		if (index < 0 || index >= width) {
			throw new IndexOutOfBoundsException(index);
		}
		return ((value >>> index) & 1L) != 0;
	}
}

