package dev.eigenworks.computer.memory;

/** Nonvolatile readable memory that rejects writes. */
public final class RomMemory implements ByteMemory {
	private final byte[] data;

	public RomMemory(byte[] image) {
		if (image == null || image.length == 0 || image.length > 65_536) {
			throw new IllegalArgumentException("ROM image must contain between 1 and 65536 bytes");
		}
		data = image.clone();
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public int read(int address) {
		if (address < 0 || address >= data.length) {
			throw new MemoryAccessException("ROM address out of range: " + address);
		}
		return Byte.toUnsignedInt(data[address]);
	}

	@Override
	public void write(int address, int value) {
		throw new MemoryAccessException("WRITE TO READ-ONLY MEMORY at address " + address);
	}
}
