package dev.eigenworks.networking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Single-controller timed I2C transaction bus with seven-bit addressing and ACK/NACK. */
public final class I2cBus {
	private final int frequencyHertz;
	private final Map<Integer, I2cPeripheral> peripherals = new LinkedHashMap<>();
	private Pending pending;
	public I2cBus(int frequencyHertz) {
		if (frequencyHertz < 1_000 || frequencyHertz > 3_400_000) throw new IllegalArgumentException("I2C frequency must be within 1 kHz..3.4 MHz");
		this.frequencyHertz = frequencyHertz;
	}
	public void attach(I2cPeripheral peripheral) {
		if (peripherals.putIfAbsent(peripheral.address(), peripheral) != null) throw new IllegalArgumentException("BUS ADDRESS CONFLICT: 0x%02X".formatted(peripheral.address()));
	}
	public void beginWrite(int address, byte[] payload, long nowMicros) { begin(address, payload.clone(), -1, nowMicros); }
	public void beginRead(int address, int length, long nowMicros) { if (length < 0 || length > 256) throw new IllegalArgumentException("I2C read length must be within 0..256"); begin(address, null, length, nowMicros); }
	private void begin(int address, byte[] payload, int readLength, long nowMicros) {
		if (pending != null) throw new IllegalStateException("I2C BUSY");
		if (address < 0 || address > 0x7F || nowMicros < 0) throw new IllegalArgumentException("Invalid I2C transaction");
		int bytes = 1 + (payload == null ? readLength : payload.length);
		long duration = Math.max(1, (long) Math.ceil((2 + bytes * 9L) * 1_000_000.0 / frequencyHertz));
		pending = new Pending(address, payload, readLength, Math.addExact(nowMicros, duration));
	}
	public Optional<I2cResult> advance(long nowMicros) {
		if (pending == null || nowMicros < pending.completionMicros) return Optional.empty();
		Pending complete = pending; pending = null;
		I2cPeripheral peripheral = peripherals.get(complete.address);
		if (peripheral == null) return Optional.of(new I2cResult(false, new byte[0], "I2C NACK"));
		if (complete.payload != null) { peripheral.write(complete.payload); return Optional.of(new I2cResult(true, new byte[0], "I2C WRITE COMPLETE")); }
		return Optional.of(new I2cResult(true, peripheral.read(complete.readLength), "I2C READ COMPLETE"));
	}
	public boolean busy() { return pending != null; }
	public int frequencyHertz() { return frequencyHertz; }
	private record Pending(int address, byte[] payload, int readLength, long completionMicros) { }
}
