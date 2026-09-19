package dev.eigenworks.networking;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.SplittableRandom;

/** Deterministic latency, jitter, loss, bandwidth, and timeout model for byte messages. */
public final class ImpairedLink {
	private record Delivery(byte[] data, long arrivalMicros, long deadlineMicros) {}
	private final long latencyMicros, jitterMicros, timeoutMicros;
	private final double lossProbability, bytesPerSecond;
	private final SplittableRandom random;
	private final Queue<Delivery> queue = new ArrayDeque<>();
	private long serializedUntil;
	private int dropped, timedOut;
	public ImpairedLink(long latencyMicros, long jitterMicros, double lossProbability, double bytesPerSecond, long timeoutMicros, long seed) {
		if (latencyMicros < 0 || jitterMicros < 0 || !Double.isFinite(lossProbability) || lossProbability < 0 || lossProbability > 1 || !Double.isFinite(bytesPerSecond) || bytesPerSecond <= 0 || timeoutMicros <= 0) throw new IllegalArgumentException("Invalid impaired-link configuration");
		this.latencyMicros = latencyMicros; this.jitterMicros = jitterMicros; this.lossProbability = lossProbability; this.bytesPerSecond = bytesPerSecond; this.timeoutMicros = timeoutMicros; random = new SplittableRandom(seed);
	}
	public boolean send(byte[] data, long nowMicros) {
		if (data == null || data.length == 0 || nowMicros < 0) throw new IllegalArgumentException("Message and time must be valid");
		if (random.nextDouble() < lossProbability) { dropped++; return false; }
		long serialization = Math.max(1, (long) Math.ceil(data.length * 1_000_000.0 / bytesPerSecond));
		long jitter = jitterMicros == 0 ? 0 : random.nextLong(-jitterMicros, jitterMicros + 1);
		long start = Math.max(nowMicros, serializedUntil); serializedUntil = start + serialization;
		long arrival = Math.max(nowMicros, serializedUntil + latencyMicros + jitter);
		queue.add(new Delivery(Arrays.copyOf(data, data.length), arrival, nowMicros + timeoutMicros));
		return true;
	}
	public Optional<byte[]> receive(long nowMicros) {
		while (!queue.isEmpty() && queue.peek().deadlineMicros() < nowMicros) { queue.remove(); timedOut++; }
		if (!queue.isEmpty() && queue.peek().arrivalMicros() <= nowMicros) {
			byte[] data = queue.remove().data();
			return Optional.of(Arrays.copyOf(data, data.length));
		}
		return Optional.empty();
	}
	public int droppedCount() { return dropped; }
	public int timedOutCount() { return timedOut; }
	public int pendingCount() { return queue.size(); }
}
