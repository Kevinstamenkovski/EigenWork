package dev.eigenworks.digital.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.signal.DigitalWord;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class DigitalWorldNetworkTest {
	@Test
	void selectedOutputConnectsAndPropagatesThroughCachedTopology() {
		DigitalWorldNetwork network = new DigitalWorldNetwork();
		FakeDevice source = FakeDevice.source("source", 8, 42);
		FakeDevice sink = FakeDevice.sink("sink", 8);
		network.register(source);
		network.register(sink);
		UUID player = UUID.randomUUID();

		network.selectFirstOutput(player, source);
		assertEquals("input", network.connectSelected(player, sink));
		assertEquals(new DigitalWord(8, 42), sink.received.getLast());
		long buildsAfterConnect = network.cacheBuildCount();

		network.publish(source, "output", new DigitalWord(8, 99), 1_000, 1_000);
		network.publish(source, "output", new DigitalWord(8, 100), 2_000, 1_000);

		assertEquals(List.of(new DigitalWord(8, 42), new DigitalWord(8, 99), new DigitalWord(8, 100)),
				sink.received);
		assertEquals(buildsAfterConnect + 1, network.cacheBuildCount());
	}

	@Test
	void rejectsIncompatibleWidthAndSupportsDisconnect() {
		DigitalWorldNetwork network = new DigitalWorldNetwork();
		FakeDevice source = FakeDevice.source("source", 1, 1);
		FakeDevice sink = FakeDevice.sink("sink", 8);
		network.register(source);
		network.register(sink);
		UUID player = UUID.randomUUID();

		network.selectFirstOutput(player, source);
		assertThrows(IllegalArgumentException.class, () -> network.connectSelected(player, sink));

		FakeDevice compatibleSink = FakeDevice.sink("compatible", 1);
		network.register(compatibleSink);
		network.selectFirstOutput(player, source);
		network.connectSelected(player, compatibleSink);
		assertEquals("input", network.disconnectFirst(compatibleSink));
		assertEquals(0, network.publish(source, "output", new DigitalWord(1, 0), 1, 1));
	}

	@Test
	void cyclicPropagationIsBoundedInsteadOfRecursing() {
		DigitalWorldNetwork network = new DigitalWorldNetwork();
		OscillatingDevice first = new OscillatingDevice("first", network);
		OscillatingDevice second = new OscillatingDevice("second", network);
		network.register(first);
		network.register(second);
		UUID player = UUID.randomUUID();

		network.selectFirstOutput(player, first);
		network.connectSelected(player, second);
		network.selectFirstOutput(player, second);
		network.connectSelected(player, first);

		assertEquals(1, network.unstableCascadeCount());
	}

	@Test
	void repeatedSourceSelectionCyclesAcrossOutputs() {
		DigitalWorldNetwork network = new DigitalWorldNetwork();
		FakeDevice source = new FakeDevice("multi",
				List.of(new DigitalPortSpec("first", 8, DigitalPortDirection.OUTPUT),
						new DigitalPortSpec("second", 16, DigitalPortDirection.OUTPUT)),
				List.of(), new DigitalWord(8, 0));
		UUID player = UUID.randomUUID();

		assertEquals("first", network.selectFirstOutput(player, source).port());
		assertEquals("second", network.selectNextOutput(player, source).port());
		assertEquals("first", network.selectNextOutput(player, source).port());
	}

	private static final class FakeDevice implements WorldDigitalDevice {
		private final DigitalDeviceAddress address;
		private final List<DigitalPortSpec> outputs;
		private final List<DigitalPortSpec> inputs;
		private final List<DigitalWord> received = new ArrayList<>();
		private DigitalSourceEndpoint source;
		private DigitalWord output;

		private FakeDevice(String id, List<DigitalPortSpec> outputs, List<DigitalPortSpec> inputs, DigitalWord output) {
			this.address = new DigitalDeviceAddress("test", id.hashCode());
			this.outputs = outputs;
			this.inputs = inputs;
			this.output = output;
		}

		static FakeDevice source(String id, int width, long value) {
			return new FakeDevice(id,
					List.of(new DigitalPortSpec("output", width, DigitalPortDirection.OUTPUT)),
					List.of(), new DigitalWord(width, value));
		}

		static FakeDevice sink(String id, int width) {
			return new FakeDevice(id, List.of(),
					List.of(new DigitalPortSpec("input", width, DigitalPortDirection.INPUT)), null);
		}

		@Override
		public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		}

		@Override
		public void unbindDigitalNetwork() {
		}

		@Override
		public DigitalDeviceAddress digitalAddress() {
			return address;
		}

		@Override
		public List<DigitalPortSpec> outputPorts() {
			return outputs;
		}

		@Override
		public List<DigitalInputBinding> inputBindings() {
			return inputs.stream().map(input -> new DigitalInputBinding(input, source)).toList();
		}

		@Override
		public DigitalWord outputValue(String port) {
			return output;
		}

		@Override
		public void connectInput(String inputPort, DigitalSourceEndpoint selectedSource) {
			source = selectedSource;
		}

		@Override
		public void disconnectInput(String inputPort) {
			source = null;
		}

		@Override
		public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
			received.add(value);
		}
	}

	private static final class OscillatingDevice implements WorldDigitalDevice {
		private static final DigitalPortSpec INPUT = new DigitalPortSpec("input", 1, DigitalPortDirection.INPUT);
		private static final DigitalPortSpec OUTPUT = new DigitalPortSpec("output", 1, DigitalPortDirection.OUTPUT);
		private final DigitalDeviceAddress address;
		private final DigitalWorldNetwork network;
		private DigitalSourceEndpoint source;
		private DigitalWord output = new DigitalWord(1, 0);

		private OscillatingDevice(String id, DigitalWorldNetwork network) {
			this.address = new DigitalDeviceAddress("test", id.hashCode());
			this.network = network;
		}

		@Override
		public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		}

		@Override
		public void unbindDigitalNetwork() {
		}

		@Override
		public DigitalDeviceAddress digitalAddress() {
			return address;
		}

		@Override
		public List<DigitalPortSpec> outputPorts() {
			return List.of(OUTPUT);
		}

		@Override
		public List<DigitalInputBinding> inputBindings() {
			return List.of(new DigitalInputBinding(INPUT, source));
		}

		@Override
		public DigitalWord outputValue(String port) {
			return output;
		}

		@Override
		public void connectInput(String inputPort, DigitalSourceEndpoint source) {
			this.source = source;
		}

		@Override
		public void disconnectInput(String inputPort) {
			source = null;
		}

		@Override
		public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
			output = new DigitalWord(1, value.value() == 0 ? 1 : 0);
			network.publish(this, OUTPUT.name(), output, timestampMicros, samplePeriodMicros);
		}
	}
}
