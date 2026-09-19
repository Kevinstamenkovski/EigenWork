package dev.eigenworks.computer.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Alu8Test {
	@Test
	void additionSetsUnsignedCarryAndSignedOverflowIndependently() {
		AluResult unsigned = Alu8.add(0xFF, 1);
		assertEquals(0, unsigned.value());
		assertTrue(unsigned.flags().zero());
		assertTrue(unsigned.flags().carry());
		assertFalse(unsigned.flags().overflow());

		AluResult signed = Alu8.add(0x7F, 1);
		assertEquals(0x80, signed.value());
		assertFalse(signed.flags().carry());
		assertTrue(signed.flags().overflow());
		assertTrue(signed.flags().negative());
	}

	@Test
	void subtractionUsesCarryAsNoBorrowAndDetectsSignedOverflow() {
		AluResult borrow = Alu8.subtract(0, 1);
		assertEquals(0xFF, borrow.value());
		assertFalse(borrow.flags().carry());

		AluResult signed = Alu8.subtract(0x80, 1);
		assertEquals(0x7F, signed.value());
		assertTrue(signed.flags().carry());
		assertTrue(signed.flags().overflow());
	}
}
