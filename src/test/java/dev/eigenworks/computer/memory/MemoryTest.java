package dev.eigenworks.computer.memory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MemoryTest {
	@Test
	void ramIsByteAddressedAndRestorable() {
		RamMemory ram = new RamMemory(4);
		ram.write(2, 0xFE);
		assertEquals(0xFE, ram.read(2));
		byte[] image = ram.copyBytes();
		ram.clear();
		ram.restore(image);
		assertEquals(0xFE, ram.read(2));
		assertThrows(MemoryAccessException.class, () -> ram.read(4));
	}

	@Test
	void romRejectsWritesAndBusEnforcesExplicitMappings() {
		MemoryBus bus = new MemoryBus(16);
		bus.map(0, new RomMemory(new byte[] {1, 2}));
		bus.map(8, new RamMemory(4));
		assertEquals(2, bus.read(1));
		bus.write(8, 77);
		assertEquals(77, bus.read(8));
		assertThrows(MemoryAccessException.class, () -> bus.write(0, 5));
		assertThrows(MemoryAccessException.class, () -> bus.read(4));
		assertThrows(IllegalArgumentException.class, () -> bus.map(1, new RamMemory(2)));
	}
}
