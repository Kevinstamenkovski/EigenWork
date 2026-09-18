package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.eigenworks.signal.DigitalWord;
import org.junit.jupiter.api.Test;

class DigitalLogicTest {
	private static final DigitalWord LEFT = new DigitalWord(4, 0b1100);
	private static final DigitalWord RIGHT = new DigitalWord(4, 0b1010);

	@Test
	void evaluatesAllRequiredGates() {
		assertEquals(0b0011, DigitalLogic.not(LEFT).value());
		assertEquals(0b1000, DigitalLogic.and(LEFT, RIGHT).value());
		assertEquals(0b1110, DigitalLogic.or(LEFT, RIGHT).value());
		assertEquals(0b0110, DigitalLogic.xor(LEFT, RIGHT).value());
		assertEquals(0b0111, DigitalLogic.nand(LEFT, RIGHT).value());
		assertEquals(0b0001, DigitalLogic.nor(LEFT, RIGHT).value());
	}

	@Test
	void gatesPreserveConfiguredWidthIncluding64Bits() {
		DigitalWord allBits = DigitalLogic.not(new DigitalWord(64, 0L));
		assertEquals(64, allBits.width());
		assertEquals(-1L, allBits.value());
	}

	@Test
	void rejectsMismatchedWidths() {
		assertThrows(IllegalArgumentException.class,
				() -> DigitalLogic.and(new DigitalWord(4, 1), new DigitalWord(8, 1)));
	}

	@Test
	void multiplexerSelectsOneWidthCheckedInput() {
		assertSame(LEFT, DigitalLogic.multiplex(false, LEFT, RIGHT));
		assertSame(RIGHT, DigitalLogic.multiplex(true, LEFT, RIGHT));
	}
}

