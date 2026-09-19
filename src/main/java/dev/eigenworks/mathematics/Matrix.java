package dev.eigenworks.mathematics;

import java.util.Arrays;

/** Immutable dense finite matrix with pivoted solving and determinant operations. */
public final class Matrix {
	private static final double PIVOT_TOLERANCE = 1.0e-12;
	private final double[][] values;
	private final int rows;
	private final int columns;

	public Matrix(double[][] values) {
		if (values.length == 0 || values[0].length == 0) throw new IllegalArgumentException("Matrix cannot be empty");
		rows = values.length;
		columns = values[0].length;
		this.values = new double[rows][columns];
		for (int row = 0; row < rows; row++) {
			if (values[row].length != columns) throw new IllegalArgumentException("Matrix rows must have equal length");
			for (int column = 0; column < columns; column++) this.values[row][column] = Vector.requireFinite(values[row][column]);
		}
	}

	public static Matrix identity(int size) {
		if (size < 1) throw new IllegalArgumentException("Matrix size must be positive");
		double[][] values = new double[size][size];
		for (int index = 0; index < size; index++) values[index][index] = 1.0;
		return new Matrix(values);
	}
	public static Matrix column(Vector vector) {
		double[][] values = new double[vector.size()][1];
		for (int row = 0; row < vector.size(); row++) values[row][0] = vector.get(row);
		return new Matrix(values);
	}
	public int rows() { return rows; }
	public int columns() { return columns; }
	public double get(int row, int column) { return values[row][column]; }
	public double[][] toArray() {
		double[][] copy = new double[rows][];
		for (int row = 0; row < rows; row++) copy[row] = Arrays.copyOf(values[row], columns);
		return copy;
	}
	public Matrix add(Matrix other) {
		requireSameShape(other);
		double[][] result = new double[rows][columns];
		for (int row = 0; row < rows; row++) for (int column = 0; column < columns; column++) result[row][column] = Vector.requireFinite(values[row][column] + other.values[row][column]);
		return new Matrix(result);
	}
	public Matrix scale(double scalar) {
		Vector.requireFinite(scalar);
		double[][] result = new double[rows][columns];
		for (int row = 0; row < rows; row++) for (int column = 0; column < columns; column++) result[row][column] = Vector.requireFinite(values[row][column] * scalar);
		return new Matrix(result);
	}
	public Matrix multiply(Matrix other) {
		if (columns != other.rows) throw new IllegalArgumentException("Matrix multiplication dimensions do not match");
		double[][] result = new double[rows][other.columns];
		for (int row = 0; row < rows; row++) for (int column = 0; column < other.columns; column++) {
			double sum = 0;
			for (int index = 0; index < columns; index++) sum += values[row][index] * other.values[index][column];
			result[row][column] = Vector.requireFinite(sum);
		}
		return new Matrix(result);
	}
	public Vector multiply(Vector vector) {
		if (columns != vector.size()) throw new IllegalArgumentException("Matrix/vector dimensions do not match");
		double[] result = new double[rows];
		for (int row = 0; row < rows; row++) {
			double sum = 0;
			for (int column = 0; column < columns; column++) sum += values[row][column] * vector.get(column);
			result[row] = Vector.requireFinite(sum);
		}
		return new Vector(result);
	}
	public Matrix transpose() {
		double[][] result = new double[columns][rows];
		for (int row = 0; row < rows; row++) for (int column = 0; column < columns; column++) result[column][row] = values[row][column];
		return new Matrix(result);
	}
	public double determinant() {
		requireSquare();
		double[][] work = toArray();
		double determinant = 1.0;
		int sign = 1;
		for (int pivot = 0; pivot < rows; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < rows; row++) if (Math.abs(work[row][pivot]) > Math.abs(work[best][pivot])) best = row;
			if (Math.abs(work[best][pivot]) <= PIVOT_TOLERANCE) return 0.0;
			if (best != pivot) { double[] swap = work[pivot]; work[pivot] = work[best]; work[best] = swap; sign = -sign; }
			double diagonal = work[pivot][pivot];
			determinant *= diagonal;
			for (int row = pivot + 1; row < rows; row++) {
				double factor = work[row][pivot] / diagonal;
				for (int column = pivot + 1; column < columns; column++) work[row][column] -= factor * work[pivot][column];
			}
		}
		return Vector.requireFinite(sign * determinant);
	}
	public Vector solve(Vector rightHandSide) {
		requireSquare();
		if (rightHandSide.size() != rows) throw new IllegalArgumentException("Right-hand side dimension does not match matrix");
		double[][] work = toArray();
		double[] result = rightHandSide.toArray();
		for (int pivot = 0; pivot < rows; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < rows; row++) if (Math.abs(work[row][pivot]) > Math.abs(work[best][pivot])) best = row;
			if (Math.abs(work[best][pivot]) <= PIVOT_TOLERANCE) throw new IllegalArgumentException("MATRIX SINGULAR");
			if (best != pivot) {
				double[] swapRow = work[pivot]; work[pivot] = work[best]; work[best] = swapRow;
				double swapValue = result[pivot]; result[pivot] = result[best]; result[best] = swapValue;
			}
			for (int row = pivot + 1; row < rows; row++) {
				double factor = work[row][pivot] / work[pivot][pivot];
				for (int column = pivot; column < columns; column++) work[row][column] -= factor * work[pivot][column];
				result[row] -= factor * result[pivot];
			}
		}
		for (int row = rows - 1; row >= 0; row--) {
			double sum = result[row];
			for (int column = row + 1; column < columns; column++) sum -= work[row][column] * result[column];
			result[row] = Vector.requireFinite(sum / work[row][row]);
		}
		return new Vector(result);
	}
	public Matrix inverse() {
		requireSquare();
		double[][] inverse = new double[rows][columns];
		for (int column = 0; column < columns; column++) {
			double[] basis = new double[rows]; basis[column] = 1.0;
			Vector solution = solve(new Vector(basis));
			for (int row = 0; row < rows; row++) inverse[row][column] = solution.get(row);
		}
		return new Matrix(inverse);
	}

	private void requireSquare() { if (rows != columns) throw new IllegalArgumentException("Matrix must be square"); }
	private void requireSameShape(Matrix other) { if (other.rows != rows || other.columns != columns) throw new IllegalArgumentException("Matrix dimensions do not match"); }
	@Override public String toString() { return Arrays.deepToString(values); }
}
