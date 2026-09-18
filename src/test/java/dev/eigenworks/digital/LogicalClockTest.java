package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import dev.eigenworks.simulation.EngineeringScheduler;
import org.junit.jupiter.api.Test;

class LogicalClockTest {
	@Test
	void producesDeterministicEdgesFromAbsoluteSimulationTime() {
		EngineeringScheduler scheduler = new EngineeringScheduler(1_000, 1_000);
		LogicalClock clock = new LogicalClock("clock", 1_000, 100.0, 0.5, 0.0, true);
		List<ClockEdgeEvent> edges = new ArrayList<>();
		clock.addEdgeListener(edges::add);
		scheduler.register(clock);

		scheduler.tick();

		assertEquals(List.of(1_000L, 5_000L, 10_000L, 15_000L, 20_000L, 25_000L,
				30_000L, 35_000L, 40_000L, 45_000L, 50_000L),
				edges.stream().map(ClockEdgeEvent::timestampMicros).toList());
		assertEquals(List.of(ClockEdge.RISING, ClockEdge.FALLING, ClockEdge.RISING, ClockEdge.FALLING,
				ClockEdge.RISING, ClockEdge.FALLING, ClockEdge.RISING, ClockEdge.FALLING,
				ClockEdge.RISING, ClockEdge.FALLING, ClockEdge.RISING),
				edges.stream().map(ClockEdgeEvent::edge).toList());
		assertEquals(10_000L, edges.getFirst().nominalPeriodMicros());
	}

	@Test
	void disablingHighClockEmitsOneFallingEdge() {
		EngineeringScheduler scheduler = new EngineeringScheduler(1_000, 1_000);
		LogicalClock clock = new LogicalClock("clock", 1_000, 10.0, 0.5, 0.0, true);
		List<ClockEdgeEvent> edges = new ArrayList<>();
		clock.addEdgeListener(edges::add);
		scheduler.register(clock);
		scheduler.tick();
		clock.setEnabled(false);
		scheduler.tick();

		assertEquals(List.of(ClockEdge.RISING, ClockEdge.FALLING),
				edges.stream().map(ClockEdgeEvent::edge).toList());
		assertFalse(clock.level());
	}

	@Test
	void rejectsUnresolvableClockPhases() {
		assertThrows(IllegalArgumentException.class,
				() -> new LogicalClock("too-fast", 1_000, 1_000.0, 0.5, 0.0, true));
		assertThrows(IllegalArgumentException.class,
				() -> new LogicalClock("bad-duty", 1_000, 10.0, 1.0, 0.0, true));
	}
}

