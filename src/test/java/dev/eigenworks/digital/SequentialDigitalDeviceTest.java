package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.signal.SignalSample;
import dev.eigenworks.simulation.EngineeringScheduler;
import org.junit.jupiter.api.Test;

class SequentialDigitalDeviceTest {
	@Test
	void registerCapturesOnlyOnConfiguredEdge() {
		DigitalRegister register = new DigitalRegister("r0", 8, ClockEdge.RISING);
		register.setInput(new DigitalWord(8, 42));

		register.onClockEdge(edge(ClockEdge.FALLING, 5_000));
		assertEquals(0, register.value().value());
		register.onClockEdge(edge(ClockEdge.RISING, 10_000));
		assertEquals(42, register.value().value());
	}

	@Test
	void registerResetHasPriorityAndLoadCanBeDisabled() {
		DigitalRegister register = new DigitalRegister("r0", 4, ClockEdge.RISING);
		register.setInput(new DigitalWord(4, 9));
		register.onClockEdge(edge(ClockEdge.RISING, 10));
		register.setLoadEnabled(false);
		register.setInput(new DigitalWord(4, 3));
		register.onClockEdge(edge(ClockEdge.RISING, 20));
		assertEquals(9, register.value().value());

		register.requestReset();
		register.onClockEdge(edge(ClockEdge.RISING, 30));
		assertEquals(0, register.value().value());
	}

	@Test
	void registerRejectsWrongInputWidth() {
		DigitalRegister register = new DigitalRegister("r0", 8, ClockEdge.RISING);
		assertThrows(IllegalArgumentException.class, () -> register.setInput(new DigitalWord(4, 1)));
	}

	@Test
	void counterWrapsAtConfiguredWidthAndHonorsEnableAndReset() {
		DigitalCounter counter = new DigitalCounter("c0", 2, ClockEdge.RISING);
		for (int index = 0; index < 4; index++) {
			counter.onClockEdge(edge(ClockEdge.RISING, index + 1));
		}
		assertEquals(0, counter.value().value());
		counter.setCountEnabled(false);
		counter.onClockEdge(edge(ClockEdge.RISING, 5));
		assertEquals(0, counter.value().value());
		counter.setCountEnabled(true);
		counter.onClockEdge(edge(ClockEdge.RISING, 6));
		counter.requestReset();
		counter.onClockEdge(edge(ClockEdge.RISING, 7));
		assertEquals(0, counter.value().value());
	}

	@Test
	void counterRestoresPersistedValueWithoutClocking() {
		DigitalCounter counter = new DigitalCounter("counter", 8, ClockEdge.RISING);

		counter.restoreValue(new DigitalWord(8, 173));

		assertEquals(new DigitalWord(8, 173), counter.value());
		assertThrows(IllegalArgumentException.class,
				() -> counter.restoreValue(new DigitalWord(4, 3)));
	}

	@Test
	void registerRestoresPersistedInputAndOutputWithoutClocking() {
		DigitalRegister register = new DigitalRegister("register", 8, ClockEdge.RISING);

		register.restoreState(new DigitalWord(8, 91), new DigitalWord(8, 42));

		assertEquals(new DigitalWord(8, 42), register.value());
		register.onClockEdge(edge(ClockEdge.RISING, 10));
		assertEquals(new DigitalWord(8, 91), register.value());
		assertThrows(IllegalArgumentException.class,
				() -> register.restoreState(new DigitalWord(8, 1), new DigitalWord(4, 1)));
	}

	@Test
	void clockDrivesCounterAndPublishesTimestampedOutputs() {
		EngineeringScheduler scheduler = new EngineeringScheduler(1_000, 1_000);
		LogicalClock clock = new LogicalClock("clock", 1_000, 100.0, 0.5, 0.0, true);
		DigitalCounter counter = new DigitalCounter("counter", 4, ClockEdge.RISING);
		List<SignalSample<DigitalWord>> samples = new ArrayList<>();
		counter.output().connect(new dev.eigenworks.signal.SignalConnection<>(0, samples::add));
		clock.addEdgeListener(event -> {
			counter.onClockEdge(event);
			counter.output().deliverThrough(event.timestampMicros());
		});
		scheduler.register(clock);

		scheduler.tick();

		assertEquals(6, counter.value().value());
		assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L),
				samples.stream().map(sample -> sample.value().value()).toList());
		assertEquals(List.of(1_000L, 10_000L, 20_000L, 30_000L, 40_000L, 50_000L),
				samples.stream().map(SignalSample::timestampMicros).toList());
		assertEquals(10_000L, samples.getFirst().samplePeriodMicros());
	}

	private static ClockEdgeEvent edge(ClockEdge edge, long time) {
		return new ClockEdgeEvent("clock", edge, time, 10_000);
	}
}
