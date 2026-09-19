package dev.eigenworks.robotics.world;

import java.util.List;
import dev.eigenworks.robotics.HomogeneousTransform;

/** Validated ordered view of one loaded serial robot component. */
public record RobotAssemblySnapshot(List<WorldRobotJoint> joints, HomogeneousTransform endEffector, long topologyRevision) {
	public RobotAssemblySnapshot { joints = List.copyOf(joints); }
	public int axes() { return joints.size(); }
}
