package dev.eigenworks.computer.memory;

/** Byte-addressable memory with unsigned byte values. */
public interface ByteMemory {
	int size();

	int read(int address);

	void write(int address, int value);
}
