package dev.eigenworks.networking;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

/** Addressable CAN participant with a bounded receive queue. */
public final class CanNode {
	private final String name;
	private final Queue<CanFrame> received = new ArrayDeque<>();
	private int overflowCount;
	public CanNode(String name) { if (name == null || name.isBlank()) throw new IllegalArgumentException("CAN node name is required"); this.name = name; }
	public String name() { return name; }
	void receive(CanFrame frame) { if (received.size() == 64) { received.remove(); overflowCount++; } received.add(frame); }
	public Optional<CanFrame> poll() { return Optional.ofNullable(received.poll()); }
	public int pendingFrames() { return received.size(); }
	public int overflowCount() { return overflowCount; }
}
