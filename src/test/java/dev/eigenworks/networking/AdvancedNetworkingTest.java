package dev.eigenworks.networking;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AdvancedNetworkingTest {
	@Test void canArbitratesLowestIdentifierThenRetriesLoser() {
		CanBus bus = new CanBus(500_000); CanNode a = new CanNode("controller"), b = new CanNode("joint"), monitor = new CanNode("monitor");
		bus.attach(a); bus.attach(b); bus.attach(monitor);
		bus.transmit(a, new CanFrame(0x200, new byte[] {2}), 0); bus.transmit(b, new CanFrame(0x100, new byte[] {1}), 0);
		bus.advance(0); assertTrue(bus.advance(1_000).isPresent());
		assertEquals(0x100, monitor.poll().orElseThrow().identifier()); assertEquals(1, bus.arbitrationLosses());
		bus.advance(1_000); bus.advance(2_000);
		assertEquals(0x200, monitor.poll().orElseThrow().identifier());
	}
	@Test void impairedLinkModelsLatencyLossAndTimeoutDeterministically() {
		ImpairedLink delayed = new ImpairedLink(1_000, 0, 0, 1_000, 10_000, 7);
		assertTrue(delayed.send(new byte[] {1, 2}, 0)); assertTrue(delayed.receive(2_999).isEmpty());
		assertArrayEquals(new byte[] {1, 2}, delayed.receive(3_000).orElseThrow());
		ImpairedLink lost = new ImpairedLink(0, 0, 1, 1_000, 1_000, 7);
		assertFalse(lost.send(new byte[] {1}, 0)); assertEquals(1, lost.droppedCount());
		ImpairedLink timeout = new ImpairedLink(10_000, 0, 0, 1_000, 100, 7);
		timeout.send(new byte[] {1}, 0); assertTrue(timeout.receive(101).isEmpty()); assertEquals(1, timeout.timedOutCount());
	}
}
