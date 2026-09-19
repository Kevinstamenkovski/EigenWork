package dev.eigenworks.networking;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CommunicationBusTest {
	@Test void uartFramesParityAndHonorsBaudTiming() {
		UartConfig config = new UartConfig(9_600, 8, 1, UartParity.EVEN);
		UartFrame frame = UartCodec.encode(0xA5, config);
		assertEquals(11, frame.bits().size());
		assertEquals(0xA5, UartCodec.decode(frame, config));
		TimedUartLink link = new TimedUartLink(config);
		link.transmit(0x5A, 1_000);
		assertTrue(link.receive(link.completionMicros() - 1).isEmpty());
		assertEquals(0x5A, link.receive(link.completionMicros()).orElseThrow());
	}

	@Test void i2cAddressesPeripheralAndReportsNackAndConflict() {
		I2cBus bus = new I2cBus(100_000);
		I2cMemoryPeripheral memory = new I2cMemoryPeripheral(0x50, 16);
		bus.attach(memory);
		assertThrows(IllegalArgumentException.class, () -> bus.attach(new I2cMemoryPeripheral(0x50, 8)));
		bus.beginWrite(0x50, new byte[] {3, 0x66}, 0);
		assertTrue(bus.advance(1_000).orElseThrow().acknowledged());
		bus.beginWrite(0x50, new byte[] {3}, 1_000);
		bus.advance(2_000).orElseThrow();
		bus.beginRead(0x50, 1, 2_000);
		assertEquals(0x66, Byte.toUnsignedInt(bus.advance(3_000).orElseThrow().data()[0]));
		bus.beginRead(0x51, 1, 3_000);
		assertFalse(bus.advance(4_000).orElseThrow().acknowledged());
	}

	@Test void spiRequiresChipSelectAndIsFullDuplexAfterClockTime() {
		SpiBus bus = new SpiBus(1_000_000);
		bus.attach(2, data -> new byte[] {(byte) (data[0] + 1)});
		bus.beginTransfer(2, new byte[] {41}, 100);
		assertTrue(bus.advance(107).isEmpty());
		assertEquals(42, Byte.toUnsignedInt(bus.advance(108).orElseThrow()[0]));
		assertThrows(IllegalArgumentException.class, () -> bus.beginTransfer(1, new byte[] {0}, 200));
	}
}
