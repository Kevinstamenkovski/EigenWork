package dev.eigenworks.control;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.eigenworks.mechanical.MotorAssembly;

class ControlBlockTest {
	@Test void algebraicBlocksComputeAndClamp() {
		assertEquals(5.0, new SumBlock(1, -1, 2).apply(4, 3, 2), 1e-12);
		assertEquals(6.0, new GainBlock(3).apply(2), 1e-12);
		assertEquals(1.0, new SaturationBlock(-1, 1).apply(3), 1e-12);
	}

	@Test void dynamicBlocksRetainDeterministicState() {
		IntegratorBlock integrator = new IntegratorBlock(-10, 10);
		assertEquals(1.0, integrator.update(2, 0.5), 1e-12);
		DerivativeBlock derivative = new DerivativeBlock(0);
		assertEquals(0.0, derivative.update(1, 0.1), 1e-12);
		assertEquals(20.0, derivative.update(3, 0.1), 1e-12);
		DelayBlock delay = new DelayBlock(2, 0);
		assertEquals(0, delay.update(1));
		assertEquals(0, delay.update(2));
		assertEquals(1, delay.update(3));
		LowPassFilterBlock filter = new LowPassFilterBlock(0.1);
		assertEquals(0, filter.update(0, 0.01));
		assertTrue(filter.update(1, 0.01) > 0 && filter.output() < 1);
	}

	@Test void pidPreventsIntegralWindupAndFiltersDerivative() {
		PidController pid = new PidController(2, 4, 0.1, 0.01, -1, 1, 0.05);
		for (int index = 0; index < 500; index++) pid.update(10, 0);
		PidSnapshot saturated = pid.snapshot();
		assertEquals(1, saturated.output());
		assertEquals(0, saturated.integral(), 1e-12);
		PidSnapshot recovering = pid.update(0, 1);
		assertTrue(recovering.output() < 0);
		assertTrue(Double.isFinite(recovering.derivative()));
	}

	@Test void closedLoopMotorReachesNinetyDegrees() {
		MotorAssembly assembly = new MotorAssembly();
		MotorPositionController controller = new MotorPositionController();
		for (int step = 0; step < 900; step++) {
			if (step % 2 == 0) controller.update(assembly);
			assembly.step(0.005);
		}
		assertEquals(Math.PI / 2, assembly.outputAngle(), 0.08);
		assertEquals(0, controller.snapshot().error(), 0.08);
	}

	@Test void invalidNumericsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new GainBlock(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new DelayBlock(0, 0));
		assertThrows(IllegalArgumentException.class, () -> new PidController(1, 0, 0, 0, -1, 1, 0));
	}
}
