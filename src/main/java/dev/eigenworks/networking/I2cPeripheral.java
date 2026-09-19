package dev.eigenworks.networking;

public interface I2cPeripheral {
	int address();
	void write(byte[] payload);
	byte[] read(int length);
}
