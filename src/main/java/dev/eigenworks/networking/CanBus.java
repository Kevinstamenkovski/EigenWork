package dev.eigenworks.networking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Timed CAN bus with nondestructive identifier arbitration and broadcast delivery. */
public final class CanBus {
	private record Pending(CanNode sender, CanFrame frame, long readyMicros, long sequence) {}
	private final int bitrate;
	private final List<CanNode> nodes = new ArrayList<>();
	private final List<Pending> pending = new ArrayList<>();
	private Pending active;
	private long completionMicros;
	private long sequence;
	private long busyMicros;
	private int arbitrationLosses;
	public CanBus(int bitrate) { if (bitrate < 10_000 || bitrate > 1_000_000) throw new IllegalArgumentException("CAN bitrate must be 10 kbit/s..1 Mbit/s"); this.bitrate = bitrate; }
	public void attach(CanNode node) { if (nodes.contains(node)) throw new IllegalArgumentException("CAN node already attached"); nodes.add(node); }
	public void transmit(CanNode sender, CanFrame frame, long nowMicros) {
		if (!nodes.contains(sender)) throw new IllegalArgumentException("CAN sender is not attached");
		if (nowMicros < 0) throw new IllegalArgumentException("CAN time cannot be negative");
		pending.add(new Pending(sender, frame, nowMicros, sequence++));
	}
	/** Advances arbitration/delivery and returns the most recently delivered frame, if any. */
	public Optional<CanFrame> advance(long nowMicros) {
		CanFrame delivered = null;
		if (active != null && nowMicros >= completionMicros) {
			for (CanNode node : nodes) if (node != active.sender()) node.receive(active.frame());
			delivered = active.frame(); active = null;
		}
		if (active == null) {
			List<Pending> eligible = pending.stream().filter(item -> item.readyMicros() <= nowMicros).toList();
			if (!eligible.isEmpty()) {
				Pending winner = eligible.stream().min(Comparator.comparingInt((Pending item) -> item.frame().identifier()).thenComparingLong(Pending::sequence)).orElseThrow();
				arbitrationLosses += eligible.size() - 1;
				pending.remove(winner); active = winner;
				long duration = Math.max(1, (long) Math.ceil((47.0 + winner.frame().payload().length * 10.0) * 1_000_000.0 / bitrate));
				completionMicros = nowMicros + duration; busyMicros += duration;
			}
		}
		return Optional.ofNullable(delivered);
	}
	public boolean busy() { return active != null || !pending.isEmpty(); }
	public int arbitrationLosses() { return arbitrationLosses; }
	public long busyMicros() { return busyMicros; }
	public int bitrate() { return bitrate; }
}
