package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.eigenworks.signal.DigitalWord;
import org.junit.jupiter.api.Test;

class DigitalGateOperationTest {
	@Test
	void configurableOperationsUseWidthSafeLogic() {
		DigitalWord a = new DigitalWord(4, 0b1100);
		DigitalWord b = new DigitalWord(4, 0b1010);

		assertEquals(new DigitalWord(4, 0b0011), DigitalGateOperation.NOT.apply(a, b));
		assertEquals(new DigitalWord(4, 0b1000), DigitalGateOperation.AND.apply(a, b));
		assertEquals(new DigitalWord(4, 0b1110), DigitalGateOperation.OR.apply(a, b));
		assertEquals(new DigitalWord(4, 0b0110), DigitalGateOperation.XOR.apply(a, b));
		assertEquals(new DigitalWord(4, 0b0111), DigitalGateOperation.NAND.apply(a, b));
		assertEquals(new DigitalWord(4, 0b0001), DigitalGateOperation.NOR.apply(a, b));
	}

	@Test
	void operationCycleWrapsDeterministically() {
		DigitalGateOperation operation = DigitalGateOperation.NOT;
		for (int index = 0; index < DigitalGateOperation.values().length; index++) {
			operation = operation.next();
		}
		assertEquals(DigitalGateOperation.NOT, operation);
	}
}
