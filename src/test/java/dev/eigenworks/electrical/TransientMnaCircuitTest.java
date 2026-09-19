package dev.eigenworks.electrical;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TransientMnaCircuitTest {
	@Test void rcStepUsesBackwardEulerCompanionModel() {
		TransientMnaCircuit circuit = new TransientMnaCircuit(2)
				.voltageSource(1, 0, 10).resistor(1, 2, 1_000).capacitor(2, 0, 1.0e-6);
		TransientCircuitSolution first = circuit.step(0.001);
		assertEquals(5.0, first.voltage(2), 1.0e-9);
		assertEquals(7.5, circuit.step(0.001).voltage(2), 1.0e-9);
	}

	@Test void rlStepMaintainsInductorCurrentState() {
		TransientMnaCircuit circuit = new TransientMnaCircuit(2)
				.voltageSource(1, 0, 10).resistor(1, 2, 10).inductor(2, 0, 0.01);
		TransientCircuitSolution first = circuit.step(0.001);
		assertEquals(0.5, first.inductorCurrents()[0], 1.0e-9);
		assertEquals(0.75, circuit.step(0.001).inductorCurrents()[0], 1.0e-9);
	}

	@Test void shockleyDiodeConvergesToForwardVoltage() {
		TransientCircuitSolution solution = new TransientMnaCircuit(2)
				.voltageSource(1, 0, 5).resistor(1, 2, 1_000).diode(2, 0).step(0.001);
		assertTrue(solution.voltage(2) > 0.5 && solution.voltage(2) < 0.8, "forward voltage=" + solution.voltage(2));
		assertTrue(solution.newtonIterations() > 1);
	}

	@Test void reverseBiasedDiodeDoesNotConductMeaningfully() {
		TransientCircuitSolution solution = new TransientMnaCircuit(2)
				.voltageSource(1, 0, -5).resistor(1, 2, 1_000).diode(2, 0).step(0.001);
		assertEquals(-5, solution.voltage(2), 1.0e-6);
	}

	@Test void rejectsInvalidTimestepsAndFloatingNetworks() {
		TransientMnaCircuit floating = new TransientMnaCircuit(2).capacitor(1, 2, 1e-6);
		assertThrows(LinearSolveException.class, () -> floating.step(0.001));
		assertThrows(IllegalArgumentException.class, () -> new TransientMnaCircuit(1).step(0));
	}
}
