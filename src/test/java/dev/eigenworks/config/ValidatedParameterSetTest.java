package dev.eigenworks.config;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidatedParameterSetTest {
	@Test void validatesAdjustsClampsAndResets() {
		ValidatedParameterSet parameters = new ValidatedParameterSet(List.of(new EngineeringParameter("gain", "Gain", 0, 2, .25, 1, "")));
		assertEquals(1.25, parameters.adjust(0, 1));
		for (int i = 0; i < 10; i++) parameters.adjust(0, 1);
		assertEquals(2, parameters.get("gain"));
		assertThrows(IllegalArgumentException.class, () -> parameters.set("gain", Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> parameters.set("gain", 3));
		parameters.reset(); assertEquals(1, parameters.get(0));
	}
	@Test void rejectsDuplicateDefinitions() {
		EngineeringParameter definition = new EngineeringParameter("x", "X", 0, 1, .1, 0, "V");
		assertThrows(IllegalArgumentException.class, () -> new ValidatedParameterSet(List.of(definition, definition)));
	}
}
