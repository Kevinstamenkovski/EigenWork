package dev.eigenworks.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SignalConnectionTest {
	@Test
	void latencyDefersDeliveryWithoutChangingSourceTimestamp() {
		List<SignalSample<ScalarSignal>> received = new ArrayList<>();
		SignalConnection<ScalarSignal> connection = new SignalConnection<>(2_000, received::add);
		SignalSample<ScalarSignal> sample = scalarSample(10_000, 3.3);
		connection.transmit(sample);

		assertEquals(0, connection.deliverThrough(11_999));
		assertEquals(1, connection.pendingCount());
		assertEquals(1, connection.deliverThrough(12_000));
		assertEquals(sample, received.getFirst());
	}

	@Test
	void sourceFansOutInConnectionOrder() {
		SignalSource<BooleanSignal> source = new SignalSource<>();
		List<String> received = new ArrayList<>();
		source.connect(new SignalConnection<>(0, sample -> received.add("a:" + sample.value().value())));
		source.connect(new SignalConnection<>(0, sample -> received.add("b:" + sample.value().value())));
		SignalSample<BooleanSignal> sample = new SignalSample<>(new BooleanSignal(true), 5_000, "clock",
				EngineeringUnit.DIMENSIONLESS, 1_000, SignalValidity.VALID, false, 0.0);

		source.publish(sample);
		assertEquals(2, source.deliverThrough(5_000));
		assertEquals(List.of("a:true", "b:true"), received);
	}

	@Test
	void samplesCannotTravelBackwardsInTime() {
		SignalSource<ScalarSignal> source = new SignalSource<>();
		source.publish(scalarSample(10, 1.0));
		assertThrows(IllegalArgumentException.class, () -> source.publish(scalarSample(9, 2.0)));
	}

	@Test
	void numericalSamplesRejectNonFiniteValuesAndNoise() {
		assertThrows(IllegalArgumentException.class, () -> new ScalarSignal(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new SignalSample<>(new ScalarSignal(1.0), 0, "sensor",
				EngineeringUnit.VOLT, 1_000, SignalValidity.VALID, false, Double.POSITIVE_INFINITY));
	}

	@Test
	void digitalWordMasksValueAndChecksBits() {
		DigitalWord word = new DigitalWord(4, 0b1_1010);
		assertEquals(0b1010, word.value());
		assertFalse(word.bit(0));
		assertTrue(word.bit(1));
		assertThrows(IndexOutOfBoundsException.class, () -> word.bit(4));
	}

	private static SignalSample<ScalarSignal> scalarSample(long timestamp, double value) {
		return new SignalSample<>(new ScalarSignal(value), timestamp, "voltage_sensor",
				EngineeringUnit.VOLT, 1_000, SignalValidity.VALID, false, 0.01);
	}
}
