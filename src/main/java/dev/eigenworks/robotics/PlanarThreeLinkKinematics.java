package dev.eigenworks.robotics;

import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.mathematics.Vector;

/** Forward/Jacobian and damped-least-squares IK for a redundant three-link planar arm. */
public final class PlanarThreeLinkKinematics {
	private final double[] lengths;
	public PlanarThreeLinkKinematics(double first, double second, double third) {
		lengths = new double[] {first, second, third};
		for (double length : lengths) if (!Double.isFinite(length) || length <= 0) throw new IllegalArgumentException("Link lengths must be positive and finite");
	}
	public Pose2D forward(Vector joints) {
		requireJoints(joints); double angle = 0, x = 0, y = 0;
		for (int index = 0; index < 3; index++) { angle += joints.get(index); x += lengths[index] * Math.cos(angle); y += lengths[index] * Math.sin(angle); }
		return new Pose2D(x, y, angle);
	}
	public Matrix jacobian(Vector joints) {
		requireJoints(joints); double[] angles = {joints.get(0), joints.get(0) + joints.get(1), joints.get(0) + joints.get(1) + joints.get(2)};
		double[][] result = new double[2][3];
		for (int column = 0; column < 3; column++) for (int link = column; link < 3; link++) {
			result[0][column] -= lengths[link] * Math.sin(angles[link]);
			result[1][column] += lengths[link] * Math.cos(angles[link]);
		}
		return new Matrix(result);
	}
	public MultiJointIkResult inverse(double x, double y, Vector initial, double damping, int maximumIterations, double tolerance) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(damping) || damping <= 0 || maximumIterations < 1 || maximumIterations > 10_000 || !Double.isFinite(tolerance) || tolerance <= 0) throw new IllegalArgumentException("Invalid IK configuration");
		requireJoints(initial); Vector joints = initial;
		for (int iteration = 0; iteration < maximumIterations; iteration++) {
			Pose2D pose = forward(joints); Vector error = new Vector(x - pose.xMeters(), y - pose.yMeters());
			if (error.norm() <= tolerance) return new MultiJointIkResult(true, normalize(joints), iteration, "IK CONVERGED");
			Matrix j = jacobian(joints);
			Vector delta = j.transpose().multiply(j.multiply(j.transpose()).add(Matrix.identity(2).scale(damping * damping)).solve(error)).scale(0.55);
			joints = normalize(joints.add(new Vector(Math.clamp(delta.get(0), -.2, .2), Math.clamp(delta.get(1), -.2, .2), Math.clamp(delta.get(2), -.2, .2))));
		}
		return new MultiJointIkResult(false, normalize(joints), maximumIterations, "IK DID NOT CONVERGE");
	}
	public double manipulability(Vector joints) {
		Matrix j = jacobian(joints); return Math.sqrt(Math.max(0, j.multiply(j.transpose()).determinant()));
	}
	private static Vector normalize(Vector vector) { return new Vector(Math.atan2(Math.sin(vector.get(0)), Math.cos(vector.get(0))), Math.atan2(Math.sin(vector.get(1)), Math.cos(vector.get(1))), Math.atan2(Math.sin(vector.get(2)), Math.cos(vector.get(2)))); }
	private static void requireJoints(Vector joints) { if (joints == null || joints.size() != 3) throw new IllegalArgumentException("Three joint angles are required"); }
}
