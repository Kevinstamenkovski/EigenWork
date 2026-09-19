package dev.eigenworks.mathematics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MatrixVectorTest {
	@Test void vectorAndMatrixOperationsAreDimensionallyCorrect() {
		Vector first = new Vector(1, 2, 3);
		Vector second = new Vector(4, 5, 6);
		assertEquals(32, first.dot(second), 1e-12);
		assertEquals(new Vector(5, 7, 9), first.add(second));
		Matrix matrix = new Matrix(new double[][] {{1, 2}, {3, 4}});
		assertEquals(new Vector(5, 11), matrix.multiply(new Vector(1, 2)));
		assertEquals(15, matrix.multiply(matrix).get(1, 0), 1e-12);
		assertEquals(3, matrix.transpose().get(0, 1), 1e-12);
	}

	@Test void determinantSolveAndInverseUsePivoting() {
		Matrix matrix = new Matrix(new double[][] {{0, 2}, {1, 3}});
		assertEquals(-2, matrix.determinant(), 1e-12);
		assertEquals(new Vector(1, 2), matrix.solve(new Vector(4, 7)));
		Matrix identity = matrix.multiply(matrix.inverse());
		assertEquals(1, identity.get(0, 0), 1e-10);
		assertEquals(0, identity.get(0, 1), 1e-10);
	}

	@Test void invalidAndSingularMatricesFailExplicitly() {
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[][] {{1}, {1, 2}}));
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[][] {{1, 2}, {2, 4}}).solve(new Vector(1, 2)));
		assertThrows(IllegalArgumentException.class, () -> new Vector(Double.NaN));
	}
}
