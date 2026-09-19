package dev.eigenworks.mathematics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MathWorkstationEngineTest {
	private final MathWorkstationEngine engine = new MathWorkstationEngine();

	@Test void evaluatesSafeScalarFunctionsAndPrecedence() {
		assertEquals("7.000000000", engine.evaluate("1 + 2 * 3"));
		assertEquals("1.000000000", engine.evaluate("sin(pi / 2)"));
		assertThrows(IllegalArgumentException.class, () -> engine.evaluate("java.lang.Runtime"));
	}
	@Test void evaluatesMatrixAndVectorCommands() {
		assertEquals("-2.000000000", engine.evaluate("det 1,2;3,4"));
		assertEquals("[1.000000000, 2.000000000]", engine.evaluate("solve 0,2;1,3 | 4,7"));
		assertEquals("32.00000000", engine.evaluate("dot 1,2,3 | 4,5,6"));
		assertEquals("[7.000000000, 10.00000000; 15.00000000, 22.00000000]", engine.evaluate("mul 1,2;3,4 | 1,2;3,4"));
	}
	@Test void numericallyIntegratesAndDifferentiatesExpressions() {
		assertEquals(Math.PI * Math.PI / 2, Double.parseDouble(engine.evaluate("integrate 0 pi 100 : x")), 1e-8);
		assertEquals(1, Double.parseDouble(engine.evaluate("differentiate 0 0.0001 : sin(x)")), 1e-8);
	}
}
