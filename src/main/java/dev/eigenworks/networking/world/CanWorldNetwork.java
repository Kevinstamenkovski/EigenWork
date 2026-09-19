package dev.eigenworks.networking.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dev.eigenworks.networking.CanBus;
import dev.eigenworks.networking.CanFrame;
import dev.eigenworks.networking.CanNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Loaded-element physical CAN graph. Components are rebuilt only after an
 * element loads or unloads; normal bus advancement never searches the world.
 */
public final class CanWorldNetwork {
	private static final long ADVANCE_QUANTUM_MICROS = 1_000;
	private static final int MAX_LOADED_ELEMENTS = 16_384;
	private final Map<CanWorldAddress, WorldCanElement> elements = new LinkedHashMap<>();
	private Map<CanWorldAddress, Component> componentsByElement = Map.of();
	private List<Component> components = List.of();
	private boolean topologyDirty = true;
	private long topologyRevision;
	private long cacheBuildCount;
	private long currentTimeMicros;

	public void register(WorldCanElement element) {
		Objects.requireNonNull(element, "element");
		if (!elements.containsKey(element.canAddress()) && elements.size() >= MAX_LOADED_ELEMENTS) throw new IllegalStateException("CAN NETWORK SIZE LIMIT");
		WorldCanElement previous = elements.putIfAbsent(element.canAddress(), element);
		if (previous != null && previous != element) throw new IllegalArgumentException("Duplicate CAN element: " + element.canAddress());
		markDirty();
	}
	public void unregister(WorldCanElement element) {
		Objects.requireNonNull(element, "element");
		if (elements.remove(element.canAddress(), element)) markDirty();
	}
	public void transmit(WorldCanNode sender, CanFrame frame) {
		Objects.requireNonNull(sender, "sender"); Objects.requireNonNull(frame, "frame"); rebuildIfDirty();
		Component component = componentsByElement.get(sender.canAddress());
		if (component == null || component.nodes.size() < 2) throw new IllegalStateException("CAN NODE DISCONNECTED");
		CanNode node = component.nodes.get(sender);
		if (node == null) throw new IllegalStateException("CAN node is not registered in its component");
		component.bus.transmit(node, frame, currentTimeMicros);
	}
	/** Advances all cached buses using bounded one-millisecond engineering quanta. */
	public void advance(long deltaMicros) {
		if (deltaMicros < 0 || deltaMicros > 1_000_000) throw new IllegalArgumentException("CAN advance must be 0..1 s");
		rebuildIfDirty();
		long target = currentTimeMicros + deltaMicros;
		while (currentTimeMicros < target) {
			currentTimeMicros = Math.min(target, currentTimeMicros + ADVANCE_QUANTUM_MICROS);
			for (Component component : components) {
				component.bus.advance(currentTimeMicros);
				for (Map.Entry<WorldCanNode, CanNode> entry : component.nodes.entrySet()) {
					entry.getValue().poll().ifPresent(entry.getKey()::receiveCanFrame);
				}
			}
		}
	}
	public boolean connected(WorldCanNode node) { rebuildIfDirty(); Component component = componentsByElement.get(node.canAddress()); return component != null && component.nodes.size() >= 2; }
	public int loadedElementCount() { return elements.size(); }
	public int componentCount() { rebuildIfDirty(); return components.size(); }
	public long topologyRevision() { return topologyRevision; }
	public long cacheBuildCount() { return cacheBuildCount; }
	public long currentTimeMicros() { return currentTimeMicros; }

	private void markDirty() { topologyDirty = true; topologyRevision++; }
	private void rebuildIfDirty() {
		if (!topologyDirty) return;
		Map<CanWorldAddress, Component> byElement = new LinkedHashMap<>(); List<Component> rebuilt = new ArrayList<>(); Set<CanWorldAddress> visited = new LinkedHashSet<>();
		for (CanWorldAddress root : elements.keySet()) {
			if (!visited.add(root)) continue;
			Set<CanWorldAddress> memberAddresses = new LinkedHashSet<>(); ArrayDeque<CanWorldAddress> queue = new ArrayDeque<>(); queue.add(root);
			while (!queue.isEmpty()) {
				CanWorldAddress current = queue.removeFirst(); memberAddresses.add(current); WorldCanElement currentElement = elements.get(current);
				for (Direction direction : Direction.values()) {
					CanWorldAddress neighborAddress = new CanWorldAddress(current.dimension(), BlockPos.of(current.packedPosition()).relative(direction).asLong());
					WorldCanElement neighbor = elements.get(neighborAddress);
					if (neighbor == null || (!isCable(currentElement) && !isCable(neighbor))) continue;
					if (visited.add(neighborAddress)) queue.addLast(neighborAddress);
				}
			}
			Component component = new Component();
			for (CanWorldAddress address : memberAddresses) {
				WorldCanElement element = elements.get(address); byElement.put(address, component);
				if (element instanceof WorldCanNode worldNode) { CanNode node = new CanNode(worldNode.canNodeName()); component.bus.attach(node); component.nodes.put(worldNode, node); }
			}
			rebuilt.add(component);
		}
		componentsByElement = Map.copyOf(byElement); components = List.copyOf(rebuilt); topologyDirty = false; cacheBuildCount++;
	}
	private static boolean isCable(WorldCanElement element) { return !(element instanceof WorldCanNode); }

	private static final class Component {
		private final CanBus bus = new CanBus(500_000);
		private final Map<WorldCanNode, CanNode> nodes = new LinkedHashMap<>();
	}
}
