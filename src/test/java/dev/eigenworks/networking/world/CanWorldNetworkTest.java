package dev.eigenworks.networking.world;

import static org.junit.jupiter.api.Assertions.*;

import dev.eigenworks.networking.CanFrame;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class CanWorldNetworkTest {
	@Test void cableComponentDeliversWithoutWorldScanningAndRebuildsOnlyWhenDirty() {
		CanWorldNetwork network = new CanWorldNetwork();
		FakeNode left = new FakeNode("left", 0, 64, 0); FakeCable cable = new FakeCable(1, 64, 0); FakeNode right = new FakeNode("right", 2, 64, 0);
		register(network, left, cable, right);
		assertTrue(network.connected(left)); long builds = network.cacheBuildCount();
		network.transmit(left, new CanFrame(0x120, new byte[] {42})); network.advance(2_000);
		assertNotNull(right.received); assertEquals(42, Byte.toUnsignedInt(right.received.payload()[0]));
		assertEquals(builds, network.cacheBuildCount(), "Steady-state transmission must use cached topology");
		network.unregister(cable); assertFalse(network.connected(left)); assertTrue(network.cacheBuildCount() > builds);
		assertThrows(IllegalStateException.class, () -> network.transmit(left, new CanFrame(1, new byte[0])));
	}
	@Test void adjacentNodesRequireARealCableElement() {
		CanWorldNetwork network = new CanWorldNetwork(); FakeNode first = new FakeNode("first", 0, 0, 0), second = new FakeNode("second", 1, 0, 0);
		register(network, first, second); assertFalse(network.connected(first)); assertEquals(2, network.componentCount());
	}
	private static void register(CanWorldNetwork network, FakeElement... elements) { for (FakeElement element : elements) network.register(element); }
	private abstract static class FakeElement implements WorldCanElement {
		private final CanWorldAddress address;
		FakeElement(int x, int y, int z) { address = new CanWorldAddress("test", net.minecraft.core.BlockPos.asLong(x, y, z)); }
		@Override public void bindCanNetwork(ServerLevel level, CanWorldNetwork network) {}
		@Override public void unbindCanNetwork() {}
		@Override public CanWorldAddress canAddress() { return address; }
	}
	private static final class FakeCable extends FakeElement { FakeCable(int x, int y, int z) { super(x, y, z); } }
	private static final class FakeNode extends FakeElement implements WorldCanNode {
		private final String name; private CanFrame received;
		FakeNode(String name, int x, int y, int z) { super(x, y, z); this.name = name; }
		@Override public String canNodeName() { return name; }
		@Override public void receiveCanFrame(CanFrame frame) { received = frame; }
	}
}
