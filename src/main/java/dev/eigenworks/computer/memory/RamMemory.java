package dev.eigenworks.computer.memory;

import java.util.Arrays;

/** Volatile readable and writable byte memory. */
public final class RamMemory implements ByteMemory {
	private final byte[] data;

	public RamMemory(int size) {
		if (size <= 0 || size > 65_536) {
			throw new IllegalArgumentException("RAM size must be between 1 and 65536 bytes");
		}
		data = new byte[size];
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public int read(int address) {
		return Byte.toUnsignedInt(data[requireAddress(address)]);
	}

	@Override
	public void write(int address, int value) {
		data[requireAddress(address)] = (byte) requireByte(value);
	}

	public void clear() {
		Arrays.fill(data, (byte) 0);
	}

	public void load(int address, byte[] bytes) {
		if (bytes == null) {
			throw new NullPointerException("bytes");
		}
		if (address < 0 || address + bytes.length > data.length) {
			throw new MemoryAccessException("Program does not fit RAM at address " + address);
		}
		System.arraycopy(bytes, 0, data, address, bytes.length);
	}

	public byte[] copyBytes() {
		return data.clone();
	}

	public void restore(byte[] bytes) {
		if (bytes == null || bytes.length != data.length) {
			throw new IllegalArgumentException("RAM image must contain exactly " + data.length + " bytes");
		}
		System.arraycopy(bytes, 0, data, 0, data.length);
	}

	private int requireAddress(int address) {
		if (address < 0 || address >= data.length) {
			throw new MemoryAccessException("RAM address out of range: " + address);
		}
		return address;
	}

	static int requireByte(int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("Memory value must be an unsigned byte: " + value);
		}
		return value;
	}
}
