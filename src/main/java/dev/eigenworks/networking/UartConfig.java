package dev.eigenworks.networking;

/** UART line configuration with derived frame timing. */
public record UartConfig(int baudRate, int dataBits, int stopBits, UartParity parity) {
	public UartConfig {
		if (baudRate < 300 || baudRate > 4_000_000) throw new IllegalArgumentException("UART baud rate must be within 300..4000000");
		if (dataBits < 5 || dataBits > 8) throw new IllegalArgumentException("UART data bits must be within 5..8");
		if (stopBits < 1 || stopBits > 2) throw new IllegalArgumentException("UART stop bits must be 1 or 2");
		if (parity == null) throw new IllegalArgumentException("UART parity is required");
	}
	public int frameBits() { return 1 + dataBits + stopBits + (parity == UartParity.NONE ? 0 : 1); }
	public long frameDurationMicros() { return Math.max(1, (long) Math.ceil(frameBits() * 1_000_000.0 / baudRate)); }
}
