package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import dev.eigenworks.signal.DigitalWord;
import org.junit.jupiter.api.Test;

class DigitalNetworkTest {
	@Test
	void topologyCacheRebuildsOnlyAfterMutation() {
		DigitalNetwork network = new DigitalNetwork();
		DigitalPort output = output("source", 8);
		DigitalPort inputA = input("sink_a", 8);
		DigitalPort inputB = input("sink_b", 8);
		network.addPort(output);
		network.addPort(inputA);
		network.addPort(inputB);
		network.connect(output, inputA);

		assertEquals(List.of(inputA), network.connectionsFrom(output));
		assertEquals(1, network.cacheBuildCount());
		network.connectionsFrom(output);
		assertEquals(1, network.cacheBuildCount());

		network.connect(output, inputB);
		assertEquals(List.of(inputA, inputB), network.connectionsFrom(output));
		assertEquals(2, network.cacheBuildCount());
	}

	@Test
	void propagationFansOutInConnectionOrder() {
		DigitalNetwork network = new DigitalNetwork();
		DigitalPort output = output("source", 4);
		DigitalPort inputA = input("a", 4);
		DigitalPort inputB = input("b", 4);
		network.addPort(output);
		network.addPort(inputA);
		network.addPort(inputB);
		network.connect(output, inputA);
		network.connect(output, inputB);
		List<String> deliveries = new ArrayList<>();

		int delivered = network.propagate(output, new DigitalWord(4, 0xD),
				(port, value) -> deliveries.add(port.deviceId() + "=" + value.value()));

		assertEquals(2, delivered);
		assertEquals(List.of("a=13", "b=13"), deliveries);
	}

	@Test
	void rejectsDirectionWidthAndMultipleDriverErrors() {
		DigitalNetwork network = new DigitalNetwork();
		DigitalPort outputA = output("source_a", 8);
		DigitalPort outputB = output("source_b", 8);
		DigitalPort narrowInput = input("narrow", 4);
		DigitalPort input = input("sink", 8);
		for (DigitalPort port : List.of(outputA, outputB, narrowInput, input)) {
			network.addPort(port);
		}

		assertThrows(IllegalArgumentException.class, () -> network.connect(outputA, narrowInput));
		assertThrows(IllegalArgumentException.class, () -> network.connect(input, outputA));
		network.connect(outputA, input);
		assertThrows(IllegalArgumentException.class, () -> network.connect(outputB, input));
		assertThrows(IllegalArgumentException.class,
				() -> network.propagate(outputA, new DigitalWord(4, 1), (port, value) -> { }));
	}

	@Test
	void removingPortRemovesAttachedConnections() {
		DigitalNetwork network = new DigitalNetwork();
		DigitalPort output = output("source", 1);
		DigitalPort input = input("sink", 1);
		network.addPort(output);
		network.addPort(input);
		network.connect(output, input);

		network.removePort(input);

		assertEquals(1, network.portCount());
		assertEquals(0, network.connectionCount());
		assertEquals(List.of(), network.connectionsFrom(output));
	}

	private static DigitalPort output(String deviceId, int width) {
		return new DigitalPort(deviceId, "out", width, DigitalPortDirection.OUTPUT);
	}

	private static DigitalPort input(String deviceId, int width) {
		return new DigitalPort(deviceId, "in", width, DigitalPortDirection.INPUT);
	}
}
