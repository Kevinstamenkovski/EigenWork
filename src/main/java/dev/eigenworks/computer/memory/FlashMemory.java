package dev.eigenworks.computer.memory;

import java.util.Arrays;

/** Nonvolatile program memory writable only through the external programmer API. */
public final class FlashMemory implements ByteMemory {
	private final byte[] data;

	public FlashMemory(int size) {
		if (size <= 0 || size > 65_536) {
			throw new IllegalArgumentException("Flash size must be between 1 and 65536 bytes");
		}
		data = new byte[size];
		Arrays.fill(data, (byte) 0xFF);
	}

	@Override public int size() { return data.length; }

	@Override
	public int read(int address) {
		return Byte.toUnsignedInt(data[requireAddress(address)]);
	}

	@Override
	public void write(int address, int value) {
		throw new MemoryAccessException("WRITE TO FLASH REQUIRES PROGRAMMER at address " + address);
	}

	public void program(byte[] image) {
		if (image == null || image.length > data.length) {
			throw new IllegalArgumentException("Flash image exceeds " + data.length + " bytes");
		}
		erase();
		System.arraycopy(image, 0, data, 0, image.length);
	}

	public void erase() {
		Arrays.fill(data, (byte) 0xFF);
	}

	public byte[] copyBytes() { return data.clone(); }

	public void restore(byte[] image) {
		if (image == null || image.length != data.length) {
			throw new IllegalArgumentException("Flash image must contain exactly " + data.length + " bytes");
		}
		System.arraycopy(image, 0, data, 0, data.length);
	}

	private int requireAddress(int address) {
		if (address < 0 || address >= data.length) {
			throw new MemoryAccessException("Flash address out of range: " + address);
		}
		return address;
	}
}
