package dev.eigenworks.robotics;

import java.util.ArrayList;
import java.util.List;
import dev.eigenworks.mathematics.Matrix;

/** General one-to-six-axis serial revolute manipulator using standard DH transforms. */
public final class SerialManipulatorKinematics {
	private final List<DhLink> links;
	public SerialManipulatorKinematics(List<DhLink> links) {
		if (links == null || links.isEmpty() || links.size() > 6 || links.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("Serial manipulator requires 1..6 DH links");
		this.links = List.copyOf(links);
	}
	public int axes() { return links.size(); }
	public List<DhLink> links() { return links; }

	public HomogeneousTransform forward(double... jointRadians) {
		validateJoints(jointRadians);
		HomogeneousTransform transform = HomogeneousTransform.identity();
		for (int i = 0; i < links.size(); i++) transform = transform.multiply(local(links.get(i), jointRadians[i]));
		return transform;
	}

	/** Returns the 6xN geometric Jacobian: linear rows first, then angular rows. */
	public Matrix jacobian(double... jointRadians) {
		validateJoints(jointRadians);
		List<HomogeneousTransform> frames = new ArrayList<>();
		HomogeneousTransform cumulative = HomogeneousTransform.identity(); frames.add(cumulative);
		for (int i = 0; i < links.size(); i++) { cumulative = cumulative.multiply(local(links.get(i), jointRadians[i])); frames.add(cumulative); }
		Matrix end = frames.getLast().matrix();
		double[] endPosition = {end.get(0, 3), end.get(1, 3), end.get(2, 3)};
		double[][] result = new double[6][links.size()];
		for (int column = 0; column < links.size(); column++) {
			Matrix frame = frames.get(column).matrix();
			double[] origin = {frame.get(0, 3), frame.get(1, 3), frame.get(2, 3)};
			double[] axis = {frame.get(0, 2), frame.get(1, 2), frame.get(2, 2)};
			double[] radius = {endPosition[0] - origin[0], endPosition[1] - origin[1], endPosition[2] - origin[2]};
			double[] linear = cross(axis, radius);
			for (int row = 0; row < 3; row++) { result[row][column] = linear[row]; result[row + 3][column] = axis[row]; }
		}
		return new Matrix(result);
	}

	private static HomogeneousTransform local(DhLink link, double jointRadians) {
		double theta = jointRadians + link.thetaOffsetRadians();
		double ct = Math.cos(theta), st = Math.sin(theta), ca = Math.cos(link.alphaRadians()), sa = Math.sin(link.alphaRadians());
		return new HomogeneousTransform(new Matrix(new double[][] {
			{ct, -st * ca, st * sa, link.aMeters() * ct},
			{st, ct * ca, -ct * sa, link.aMeters() * st},
			{0, sa, ca, link.dMeters()},
			{0, 0, 0, 1}
		}));
	}
	private void validateJoints(double[] joints) {
		if (joints == null || joints.length != links.size()) throw new IllegalArgumentException("Expected " + links.size() + " joint positions");
		for (double joint : joints) if (!Double.isFinite(joint)) throw new IllegalArgumentException("Joint positions must be finite");
	}
	private static double[] cross(double[] a, double[] b) {
		return new double[] {a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]};
	}
}
