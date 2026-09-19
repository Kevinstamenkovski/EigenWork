package dev.eigenworks.networking;

import java.util.OptionalInt;

/** One-frame UART link whose receiver sees data only after line time elapses. */
public final class TimedUartLink {
	private UartConfig config;
	private UartFrame pending;
	private long completionMicros;

	public TimedUartLink(UartConfig config) { this.config = config; }
	public void configure(UartConfig value) { if (busy()) throw new IllegalStateException("UART BUSY"); config = value; }
	public void transmit(int value, long nowMicros) {
		if (nowMicros < 0) throw new IllegalArgumentException("UART timestamp cannot be negative");
		if (busy()) throw new IllegalStateException("UART BUSY");
		pending = UartCodec.encode(value, config);
		completionMicros = Math.addExact(nowMicros, config.frameDurationMicros());
	}
	public OptionalInt receive(long nowMicros) {
		if (pending == null || nowMicros < completionMicros) return OptionalInt.empty();
		int value = UartCodec.decode(pending, config);
		pending = null;
		return OptionalInt.of(value);
	}
	public boolean busy() { return pending != null; }
	public long completionMicros() { return completionMicros; }
	public UartConfig config() { return config; }
}
