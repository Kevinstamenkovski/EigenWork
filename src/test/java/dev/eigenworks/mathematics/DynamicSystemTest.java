package dev.eigenworks.mathematics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DynamicSystemTest {
	@Test void rk4IsMoreAccurateThanEulerForExponentialState() {
		Vector initial = new Vector(1);
		StateDerivative exponential = (time, state) -> state;
		double expected = Math.exp(0.1);
		double euler = NumericalIntegrators.step(IntegrationMethod.FORWARD_EULER, exponential, 0, initial, 0.1).get(0);
		double rk4 = NumericalIntegrators.step(IntegrationMethod.RK4, exponential, 0, initial, 0.1).get(0);
		assertTrue(Math.abs(expected - rk4) < Math.abs(expected - euler));
		assertEquals(expected, rk4, 1e-6);
	}

	@Test void stateSpaceFirstOrderResponseMatchesAnalyticSolution() {
		StateSpaceSystem system = new StateSpaceSystem(
				new Matrix(new double[][] {{-1}}), new Matrix(new double[][] {{1}}),
				new Matrix(new double[][] {{1}}), new Matrix(new double[][] {{0}}), new Vector(0));
		double output = 0;
		for (int step = 0; step < 100; step++) output = system.step(new Vector(1), 0.01, IntegrationMethod.RK4).get(0);
		assertEquals(1 - Math.exp(-1), output, 1e-7);
	}

	@Test void transferFunctionConvertsToStateSpace() {
		TransferFunction transfer = new TransferFunction(new double[] {1}, new double[] {1, 1});
		double output = 0;
		for (int step = 0; step < 100; step++) output = transfer.step(1, 0.01, IntegrationMethod.RK4);
		assertEquals(1 - Math.exp(-1), output, 1e-7);
		assertThrows(IllegalArgumentException.class, () -> new TransferFunction(new double[] {1, 2, 3}, new double[] {1, 2}));
	}

	@Test void integrationGuardsRunawayInputs() {
		assertThrows(IllegalArgumentException.class, () -> NumericalIntegrators.step(
				IntegrationMethod.RK4, (time, state) -> new Vector(Double.POSITIVE_INFINITY), 0, new Vector(0), 0.1));
		assertThrows(IllegalArgumentException.class, () -> NumericalIntegrators.step(
				IntegrationMethod.RK4, (time, state) -> state, 0, new Vector(0), 0));
	}
}
