package dev.eigenworks.robotics;

import dev.eigenworks.mathematics.Matrix;

/** Immutable four-by-four homogeneous transform. */
public final class HomogeneousTransform {
	private final Matrix matrix;
	public HomogeneousTransform(Matrix matrix) {
		if (matrix.rows() != 4 || matrix.columns() != 4) throw new IllegalArgumentException("Homogeneous transform must be 4x4");
		this.matrix = matrix;
	}
	public static HomogeneousTransform identity() { return new HomogeneousTransform(Matrix.identity(4)); }
	public static HomogeneousTransform revoluteLink(double angle, double length) {
		double c = Math.cos(angle), s = Math.sin(angle);
		return new HomogeneousTransform(new Matrix(new double[][] {
			{c, -s, 0, length * c}, {s, c, 0, length * s}, {0, 0, 1, 0}, {0, 0, 0, 1}
		}));
	}
	public static HomogeneousTransform prismaticLink(double displacement, double length) {
		return new HomogeneousTransform(new Matrix(new double[][] {
			{1, 0, 0, length + displacement}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}
		}));
	}
	public HomogeneousTransform multiply(HomogeneousTransform other) { return new HomogeneousTransform(matrix.multiply(other.matrix)); }
	public Pose2D planarPose() { return new Pose2D(matrix.get(0, 3), matrix.get(1, 3), Math.atan2(matrix.get(1, 0), matrix.get(0, 0))); }
	public Matrix matrix() { return new Matrix(matrix.toArray()); }
}
