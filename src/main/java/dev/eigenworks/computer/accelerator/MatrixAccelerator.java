package dev.eigenworks.computer.accelerator;

import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.mathematics.Vector;

/** Size-bounded matrix/vector unit with deterministic logical performance costs. */
public final class MatrixAccelerator {
	private final int maximumDimension;
	public MatrixAccelerator(int maximumDimension) { if (maximumDimension < 1 || maximumDimension > 64) throw new IllegalArgumentException("Accelerator dimension must be 1..64"); this.maximumDimension = maximumDimension; }
	public AcceleratorResult<Double> dot(Vector left, Vector right) { require(left.size()); require(right.size()); return new AcceleratorResult<>(left.dot(right), Math.max(1, left.size()), "VECTOR DOT OK"); }
	public AcceleratorResult<Vector> multiply(Matrix matrix, Vector vector) {
		require(matrix.rows()); require(matrix.columns()); require(vector.size());
		return new AcceleratorResult<>(matrix.multiply(vector), Math.max(1, matrix.rows() * matrix.columns()), "MATRIX VECTOR OK");
	}
	public AcceleratorResult<Matrix> multiply(Matrix left, Matrix right) {
		require(left.rows()); require(left.columns()); require(right.rows()); require(right.columns());
		return new AcceleratorResult<>(left.multiply(right), Math.max(1, left.rows() * left.columns() * right.columns()), "MATRIX MULTIPLY OK");
	}
	private void require(int dimension) { if (dimension > maximumDimension) throw new IllegalArgumentException("ACCELERATOR DIMENSION LIMIT"); }
	public int maximumDimension() { return maximumDimension; }
}
