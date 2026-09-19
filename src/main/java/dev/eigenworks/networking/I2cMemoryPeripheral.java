package dev.eigenworks.networking;

import java.util.Arrays;

/** Small addressed educational EEPROM-like peripheral with a rolling pointer. */
public final class I2cMemoryPeripheral implements I2cPeripheral {
	private final int address;
	private final byte[] memory;
	private int pointer;
	public I2cMemoryPeripheral(int address, int size) {
		if (address < 0x08 || address > 0x77) throw new IllegalArgumentException("I2C address must be a usable seven-bit address");
		if (size < 1 || size > 256) throw new IllegalArgumentException("I2C memory size must be within 1..256");
		this.address = address; memory = new byte[size];
	}
	@Override public int address() { return address; }
	@Override public void write(byte[] payload) {
		if (payload.length == 0) return;
		pointer = Byte.toUnsignedInt(payload[0]) % memory.length;
		for (int index = 1; index < payload.length; index++) { memory[pointer] = payload[index]; pointer = (pointer + 1) % memory.length; }
	}
	@Override public byte[] read(int length) {
		if (length < 0 || length > 256) throw new IllegalArgumentException("I2C read length must be within 0..256");
		byte[] result = new byte[length];
		for (int index = 0; index < length; index++) { result[index] = memory[pointer]; pointer = (pointer + 1) % memory.length; }
		return result;
	}
	public byte[] snapshot() { return Arrays.copyOf(memory, memory.length); }
}
