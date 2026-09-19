package dev.eigenworks.computer.cpu;

import java.util.Arrays;

/** Deterministic 256-port I/O bank useful for devices, demos, and tests. */
public final class PortBank implements CpuIo {
	private final int[] ports = new int[256];

	@Override
	public int read(int port) {
		return ports[requirePort(port)];
	}

	@Override
	public void write(int port, int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("I/O value must be an unsigned byte");
		}
		ports[requirePort(port)] = value;
	}

	public void clear() {
		Arrays.fill(ports, 0);
	}

	private static int requirePort(int port) {
		if (port < 0 || port > 0xFF) {
			throw new IllegalArgumentException("I/O port must be between 0 and 255");
		}
		return port;
	}
}
