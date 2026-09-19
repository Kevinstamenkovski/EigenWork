package dev.eigenworks.electrical;

/** Partial-pivot Gaussian elimination used by the initial MNA solver. */
public final class DenseLinearSolver {
	private static final double TOLERANCE = 1.0e-12;
	private DenseLinearSolver() { }

	public static double[] solve(double[][] matrix, double[] rhs) {
		if (matrix == null || rhs == null || matrix.length == 0 || matrix.length != rhs.length) {
			throw new IllegalArgumentException("Linear system dimensions are invalid");
		}
		int size = matrix.length;
		double[][] a = new double[size][size];
		double[] b = rhs.clone();
		for (int row = 0; row < size; row++) {
			if (matrix[row] == null || matrix[row].length != size) throw new IllegalArgumentException("Matrix must be square");
			for (int column = 0; column < size; column++) a[row][column] = finite(matrix[row][column]);
			b[row] = finite(b[row]);
		}
		for (int pivot = 0; pivot < size; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < size; row++) if (Math.abs(a[row][pivot]) > Math.abs(a[best][pivot])) best = row;
			if (Math.abs(a[best][pivot]) < TOLERANCE) throw new LinearSolveException("MATRIX SINGULAR OR FLOATING NODE");
			double[] tempRow = a[pivot]; a[pivot] = a[best]; a[best] = tempRow;
			double temp = b[pivot]; b[pivot] = b[best]; b[best] = temp;
			for (int row = pivot + 1; row < size; row++) {
				double factor = a[row][pivot] / a[pivot][pivot];
				a[row][pivot] = 0.0;
				for (int column = pivot + 1; column < size; column++) a[row][column] -= factor * a[pivot][column];
				b[row] -= factor * b[pivot];
			}
		}
		double[] result = new double[size];
		for (int row = size - 1; row >= 0; row--) {
			double sum = b[row];
			for (int column = row + 1; column < size; column++) sum -= a[row][column] * result[column];
			result[row] = finite(sum / a[row][row]);
		}
		return result;
	}

	private static double finite(double value) {
		if (!Double.isFinite(value)) throw new LinearSolveException("CIRCUIT NON-FINITE VALUE");
		return value;
	}
}
