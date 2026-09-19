package dev.eigenworks.electrical;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TransientCircuitTest {
	@Test void rcChargesMonotonicallyTowardSource() {
		RcTransient rc = new RcTransient(1_000, 0.001, 0); double prior = 0;
		for (int i = 0; i < 100; i++) { double value = rc.step(5, 0.01); assertTrue(value > prior); prior = value; }
		assertTrue(prior > 3 && prior < 5);
	}
	@Test void rlCurrentApproachesOhmsLawSteadyState() {
		RlTransient rl = new RlTransient(10, 0.1, 0);
		for (int i = 0; i < 100; i++) rl.step(20, 0.01);
		assertEquals(2, rl.current(), 1e-3);
	}
}
