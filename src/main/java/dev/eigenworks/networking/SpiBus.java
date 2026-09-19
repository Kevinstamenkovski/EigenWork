package dev.eigenworks.networking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Full-duplex timed SPI bus with explicit active chip select. */
public final class SpiBus {
	private final int frequencyHertz;
	private final Map<Integer, SpiPeripheral> peripherals = new LinkedHashMap<>();
	private Pending pending;
	public SpiBus(int frequencyHertz) {
		if (frequencyHertz < 1_000 || frequencyHertz > 50_000_000) throw new IllegalArgumentException("SPI frequency must be within 1 kHz..50 MHz");
		this.frequencyHertz = frequencyHertz;
	}
	public void attach(int chipSelect, SpiPeripheral peripheral) {
		if (chipSelect < 0 || chipSelect > 15) throw new IllegalArgumentException("SPI chip select must be within 0..15");
		if (peripherals.putIfAbsent(chipSelect, peripheral) != null) throw new IllegalArgumentException("SPI CHIP SELECT CONFLICT");
	}
	public void beginTransfer(int chipSelect, byte[] data, long nowMicros) {
		if (pending != null) throw new IllegalStateException("SPI BUSY");
		if (data.length == 0 || data.length > 256 || nowMicros < 0) throw new IllegalArgumentException("Invalid SPI transfer");
		SpiPeripheral peripheral = peripherals.get(chipSelect);
		if (peripheral == null) throw new IllegalArgumentException("SPI NO PERIPHERAL");
		long duration = Math.max(1, (long) Math.ceil(data.length * 8L * 1_000_000.0 / frequencyHertz));
		pending = new Pending(peripheral, data.clone(), Math.addExact(nowMicros, duration));
	}
	public Optional<byte[]> advance(long nowMicros) {
		if (pending == null || nowMicros < pending.completionMicros) return Optional.empty();
		Pending complete = pending; pending = null;
		byte[] response = complete.peripheral.transfer(complete.data.clone());
		if (response.length != complete.data.length) throw new IllegalStateException("SPI peripheral returned wrong transfer length");
		return Optional.of(response.clone());
	}
	public boolean busy() { return pending != null; }
	public int frequencyHertz() { return frequencyHertz; }
	private record Pending(SpiPeripheral peripheral, byte[] data, long completionMicros) { }
}
