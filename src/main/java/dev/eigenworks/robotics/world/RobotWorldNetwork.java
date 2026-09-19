package dev.eigenworks.robotics.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import dev.eigenworks.robotics.DhLink;
import dev.eigenworks.robotics.SerialManipulatorKinematics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Loaded-only multi-block serial robot graph, rebuilt only after topology changes. */
public final class RobotWorldNetwork {
	private static final int MAX_LOADED_JOINTS = 4_096;
	private final Map<RobotWorldAddress, WorldRobotJoint> joints = new LinkedHashMap<>();
	private Map<RobotWorldAddress, List<WorldRobotJoint>> componentsByJoint = Map.of();
	private boolean dirty = true;
	private long topologyRevision;
	private long cacheBuildCount;

	public void register(WorldRobotJoint joint) {
		Objects.requireNonNull(joint, "joint");
		if (!joints.containsKey(joint.robotAddress()) && joints.size() >= MAX_LOADED_JOINTS) throw new IllegalStateException("ROBOT NETWORK SIZE LIMIT");
		WorldRobotJoint previous = joints.putIfAbsent(joint.robotAddress(), joint);
		if (previous != null && previous != joint) throw new IllegalArgumentException("Duplicate robot joint address");
		markDirty();
	}
	public void unregister(WorldRobotJoint joint) { if (joints.remove(joint.robotAddress(), joint)) markDirty(); }
	public void configurationChanged() { topologyRevision++; }
	public int loadedJointCount() { return joints.size(); }
	public long topologyRevision() { return topologyRevision; }
	public long cacheBuildCount() { return cacheBuildCount; }

	public RobotAssemblySnapshot assembly(WorldRobotJoint member) {
		rebuildIfDirty();
		List<WorldRobotJoint> component = componentsByJoint.get(member.robotAddress());
		if (component == null) throw new IllegalStateException("ROBOT JOINT DISCONNECTED");
		if (component.size() > 6) throw new IllegalStateException("ROBOT EXCEEDS SIX AXES");
		List<WorldRobotJoint> ordered = component.stream().sorted(Comparator.comparingInt(WorldRobotJoint::axisIndex)).toList();
		Set<Integer> axes = new LinkedHashSet<>();
		for (int i = 0; i < ordered.size(); i++) {
			if (!axes.add(ordered.get(i).axisIndex()) || ordered.get(i).axisIndex() != i) throw new IllegalStateException("ROBOT AXIS INDEX CONFLICT");
			if (i > 0 && !adjacent(ordered.get(i-1).robotAddress(), ordered.get(i).robotAddress())) throw new IllegalStateException("ROBOT AXIS ORDER IS NOT A PHYSICAL CHAIN");
		}
		SerialManipulatorKinematics kinematics = new SerialManipulatorKinematics(ordered.stream().map(j -> new DhLink(j.linkLengthMeters(), 0, 0, 0)).toList());
		double[] angles = ordered.stream().mapToDouble(WorldRobotJoint::angleRadians).toArray();
		return new RobotAssemblySnapshot(ordered, kinematics.forward(angles), topologyRevision);
	}

	private void markDirty() { dirty = true; topologyRevision++; }
	private void rebuildIfDirty() {
		if (!dirty) return;
		Map<RobotWorldAddress, List<WorldRobotJoint>> rebuilt = new LinkedHashMap<>(); Set<RobotWorldAddress> visited = new LinkedHashSet<>();
		for (RobotWorldAddress root : joints.keySet()) {
			if (!visited.add(root)) continue;
			List<WorldRobotJoint> component = new ArrayList<>(); ArrayDeque<RobotWorldAddress> queue = new ArrayDeque<>(); queue.add(root);
			while (!queue.isEmpty()) {
				RobotWorldAddress current = queue.removeFirst(); component.add(joints.get(current));
				for (Direction direction : Direction.values()) {
					RobotWorldAddress neighbor = new RobotWorldAddress(current.dimension(), BlockPos.of(current.packedPosition()).relative(direction).asLong());
					if (joints.containsKey(neighbor) && visited.add(neighbor)) queue.addLast(neighbor);
				}
			}
			List<WorldRobotJoint> frozen = List.copyOf(component);
			for (WorldRobotJoint joint : component) rebuilt.put(joint.robotAddress(), frozen);
		}
		componentsByJoint = Map.copyOf(rebuilt); dirty = false; cacheBuildCount++;
	}
	private static boolean adjacent(RobotWorldAddress a, RobotWorldAddress b) {
		if (!a.dimension().equals(b.dimension())) return false;
		BlockPos pa = BlockPos.of(a.packedPosition()), pb = BlockPos.of(b.packedPosition());
		return Math.abs(pa.getX()-pb.getX()) + Math.abs(pa.getY()-pb.getY()) + Math.abs(pa.getZ()-pb.getZ()) == 1;
	}
}
