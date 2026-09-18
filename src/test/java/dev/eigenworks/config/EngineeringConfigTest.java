package dev.eigenworks.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EngineeringConfigTest {
	@Test
	void defaultsAreValid() {
		assertDoesNotThrow(EngineeringConfig::validateDefaults);
	}

	@Test
	void simulationStepMustDivideOneMinecraftTick() {
		assertThrows(IllegalArgumentException.class,
				() -> new EngineeringConfig(3_000, 100, 1, false, false));
	}

	@Test
	void updateBudgetMustBePositive() {
		assertThrows(IllegalArgumentException.class,
				() -> new EngineeringConfig(1_000, 0, 1, false, false));
	}
}

