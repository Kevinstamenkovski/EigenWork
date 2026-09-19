package dev.eigenworks.robotics;

import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.mathematics.Vector;

/** Analytic and damped-least-squares kinematics for a two-revolute-joint planar arm. */
public final class PlanarArmKinematics {
	private final double link1;
	private final double link2;
	public PlanarArmKinematics(double link1, double link2) {
		if (!Double.isFinite(link1) || !Double.isFinite(link2) || link1 <= 0 || link2 <= 0) throw new IllegalArgumentException("Arm link lengths must be positive and finite");
		this.link1 = link1; this.link2 = link2;
	}
	public Pose2D forward(double joint1, double joint2) {
		requireAngles(joint1, joint2);
		return new Pose2D(link1 * Math.cos(joint1) + link2 * Math.cos(joint1 + joint2),
				link1 * Math.sin(joint1) + link2 * Math.sin(joint1 + joint2), joint1 + joint2);
	}
	public IkResult analyticInverse(double x, double y, boolean elbowUp) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("IK target must be finite");
		double cosine2 = (x * x + y * y - link1 * link1 - link2 * link2) / (2 * link1 * link2);
		if (cosine2 < -1.0 - 1e-10 || cosine2 > 1.0 + 1e-10) return new IkResult(false, 0, 0, 0, "IK TARGET UNREACHABLE");
		cosine2 = Math.clamp(cosine2, -1, 1);
		double joint2 = (elbowUp ? -1 : 1) * Math.acos(cosine2);
		double joint1 = Math.atan2(y, x) - Math.atan2(link2 * Math.sin(joint2), link1 + link2 * Math.cos(joint2));
		return new IkResult(true, normalize(joint1), normalize(joint2), 1, "IK CONVERGED");
	}
	public Matrix jacobian(double joint1, double joint2) {
		requireAngles(joint1, joint2);
		double combined = joint1 + joint2;
		return new Matrix(new double[][] {
			{-link1 * Math.sin(joint1) - link2 * Math.sin(combined), -link2 * Math.sin(combined)},
			{ link1 * Math.cos(joint1) + link2 * Math.cos(combined),  link2 * Math.cos(combined)}
		});
	}
	public double manipulability(double joint1, double joint2) { return Math.abs(link1 * link2 * Math.sin(joint2)); }
	public IkResult numericalInverse(double x, double y, double initial1, double initial2,
			double damping, int maximumIterations, double tolerance) {
		if (!Double.isFinite(damping) || damping <= 0 || maximumIterations < 1 || maximumIterations > 10_000 || !Double.isFinite(tolerance) || tolerance <= 0) throw new IllegalArgumentException("Numerical IK configuration is invalid");
		double q1 = initial1, q2 = initial2;
		for (int iteration = 0; iteration < maximumIterations; iteration++) {
			Pose2D pose = forward(q1, q2);
			Vector error = new Vector(x - pose.xMeters(), y - pose.yMeters());
			if (error.norm() <= tolerance) return new IkResult(true, normalize(q1), normalize(q2), iteration, "IK CONVERGED");
			Matrix jacobian = jacobian(q1, q2);
			Matrix regularized = jacobian.multiply(jacobian.transpose()).add(Matrix.identity(2).scale(damping * damping));
			Vector delta = jacobian.transpose().multiply(regularized.solve(error)).scale(0.6);
			q1 = normalize(q1 + Math.clamp(delta.get(0), -0.25, 0.25));
			q2 = normalize(q2 + Math.clamp(delta.get(1), -0.25, 0.25));
		}
		return new IkResult(false, normalize(q1), normalize(q2), maximumIterations, "IK DID NOT CONVERGE");
	}
	public double link1() { return link1; } public double link2() { return link2; }
	private static void requireAngles(double first, double second) { if (!Double.isFinite(first) || !Double.isFinite(second)) throw new IllegalArgumentException("Joint angles must be finite"); }
	private static double normalize(double angle) { return Math.atan2(Math.sin(angle), Math.cos(angle)); }
}
