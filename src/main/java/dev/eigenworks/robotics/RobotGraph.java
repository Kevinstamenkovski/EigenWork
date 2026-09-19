package dev.eigenworks.robotics;

import java.util.ArrayList;
import java.util.List;

/** Explicit cached serial robot topology; no world discovery occurs during simulation. */
public final class RobotGraph {
	private final List<RobotJoint> joints;
	private final List<RobotLink> links;
	private List<HomogeneousTransform> cachedTransforms = List.of();
	private double[] cachedPositions;
	private long topologyRevision = 1;

	public RobotGraph(List<RobotJoint> joints, List<RobotLink> links) {
		if (joints == null || links == null || joints.isEmpty() || joints.size() != links.size()) throw new IllegalArgumentException("Serial robot requires one link per joint");
		this.joints = List.copyOf(joints); this.links = List.copyOf(links); cachedPositions = new double[joints.size()];
		java.util.Arrays.fill(cachedPositions, Double.NaN);
	}
	public List<HomogeneousTransform> jointTransforms() {
		boolean dirty = cachedTransforms.isEmpty();
		for (int index = 0; index < joints.size(); index++) dirty |= cachedPositions[index] != joints.get(index).position();
		if (!dirty) return cachedTransforms;
		List<HomogeneousTransform> result = new ArrayList<>();
		HomogeneousTransform cumulative = HomogeneousTransform.identity();
		for (int index = 0; index < joints.size(); index++) {
			RobotJoint joint = joints.get(index); RobotLink link = links.get(index);
			HomogeneousTransform local = joint.type() == JointType.REVOLUTE
					? HomogeneousTransform.revoluteLink(joint.position(), link.lengthMeters())
					: HomogeneousTransform.prismaticLink(joint.position(), link.lengthMeters());
			cumulative = cumulative.multiply(local); result.add(cumulative); cachedPositions[index] = joint.position();
		}
		cachedTransforms = List.copyOf(result);
		return cachedTransforms;
	}
	public Pose2D endEffectorPose() { return jointTransforms().getLast().planarPose(); }
	public List<RobotJoint> joints() { return joints; } public List<RobotLink> links() { return links; }
	public long topologyRevision() { return topologyRevision; }
}
