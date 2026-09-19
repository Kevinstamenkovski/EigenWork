package dev.eigenworks.computer.accelerator;

import static org.junit.jupiter.api.Assertions.*;
import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.mathematics.Vector;
import org.junit.jupiter.api.Test;

class AcceleratorTest {
	@Test void fpuComputesAndChargesCyclesWithFaultGuards() {
		FloatingPointUnit fpu = new FloatingPointUnit();
		assertEquals(5.0, fpu.execute(FloatingPointUnit.Operation.ADD, 2, 3).value());
		assertEquals(12, fpu.execute(FloatingPointUnit.Operation.DIVIDE, 6, 2).cycles());
		assertThrows(ArithmeticException.class, () -> fpu.execute(FloatingPointUnit.Operation.DIVIDE, 1, 0));
		assertThrows(ArithmeticException.class, () -> fpu.execute(FloatingPointUnit.Operation.SQRT, -1, 0));
	}
	@Test void matrixUnitComputesRealOperationsAndEnforcesBounds() {
		MatrixAccelerator accelerator = new MatrixAccelerator(2);
		var result = accelerator.multiply(new Matrix(new double[][] {{1, 2}, {3, 4}}), new Vector(5, 6));
		assertArrayEquals(new double[] {17, 39}, result.value().toArray(), 1e-12); assertEquals(4, result.cycles());
		assertThrows(IllegalArgumentException.class, () -> accelerator.dot(new Vector(1, 2, 3), new Vector(1, 2, 3)));
	}
}
