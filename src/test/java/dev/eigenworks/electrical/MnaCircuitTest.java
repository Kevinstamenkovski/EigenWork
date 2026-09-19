package dev.eigenworks.electrical;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MnaCircuitTest {
	@Test void solvesSourceAndResistiveDivider() {
		CircuitSolution solution = new MnaCircuit(2)
				.voltageSource(1,0,10).resistor(1,2,1_000).resistor(2,0,1_000).solve();
		assertEquals(10,solution.voltage(1),1e-9);
		assertEquals(5,solution.voltage(2),1e-9);
		assertEquals(-0.005,solution.voltageSourceCurrents()[0],1e-9);
	}
	@Test void reportsFloatingSingularNetwork() {
		MnaCircuit circuit = new MnaCircuit(2).resistor(1,2,100);
		LinearSolveException error=assertThrows(LinearSolveException.class,circuit::solve);
		assertTrue(error.getMessage().contains("SINGULAR"));
	}
	@Test void rejectsPathologicalComponentValues() {
		assertThrows(IllegalArgumentException.class,()->new MnaCircuit(1).resistor(1,0,0));
		assertThrows(IllegalArgumentException.class,()->new MnaCircuit(1).voltageSource(1,0,Double.NaN));
	}
}
