package dev.eigenworks.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EngineeringSchedulerTest {
	@Test
	void schedulesPeriodsAgainstOneMinecraftTick() {
		EngineeringScheduler scheduler = new EngineeringScheduler(1_000, 1_000);
		RecordingDevice fast = new RecordingDevice("fast", 5_000);
		RecordingDevice slow = new RecordingDevice("slow", 20_000);
		scheduler.register(fast);
		scheduler.register(slow);

		SchedulerReport report = scheduler.tick();

		assertEquals(10, fast.times.size());
		assertEquals(List.of(20_000L, 40_000L), slow.times);
		assertEquals(12, report.executedUpdates());
		assertEquals(50_000L, report.endTimeMicros());
		assertFalse(report.overloaded());
	}

	@Test
	void registrationOrderIsDeterministic() {
		EngineeringScheduler scheduler = new EngineeringScheduler(10_000, 100);
		List<String> order = new ArrayList<>();
		scheduler.register(callbackDevice("first", 10_000, context -> order.add("first@" + context.simulationTimeMicros())));
		scheduler.register(callbackDevice("second", 10_000, context -> order.add("second@" + context.simulationTimeMicros())));

		scheduler.tick();

		assertEquals(List.of("first@10000", "second@10000", "first@20000", "second@20000",
				"first@30000", "second@30000", "first@40000", "second@40000",
				"first@50000", "second@50000"), order);
	}

	@Test
	void updateBudgetSkipsWorkButAdvancesClock() {
		EngineeringScheduler scheduler = new EngineeringScheduler(10_000, 3);
		RecordingDevice device = new RecordingDevice("limited", 10_000);
		scheduler.register(device);

		SchedulerReport report = scheduler.tick();

		assertEquals(3, report.executedUpdates());
		assertEquals(2, report.skippedUpdates());
		assertEquals(50_000, scheduler.simulationTimeMicros());
		assertTrue(report.overloaded());
	}

	@Test
	void deviceFailureBecomesDiagnosticAndDoesNotStopOthers() {
		EngineeringScheduler scheduler = new EngineeringScheduler(50_000, 10);
		scheduler.register(callbackDevice("broken", 50_000, context -> {
			throw new IllegalStateException("bad state");
		}));
		RecordingDevice healthy = new RecordingDevice("healthy", 50_000);
		scheduler.register(healthy);

		SchedulerReport report = scheduler.tick();

		assertEquals(1, report.faults().size());
		assertEquals("DEVICE_UPDATE_FAILED", report.faults().getFirst().code());
		assertEquals(List.of(50_000L), healthy.times);
	}

	@Test
	void rejectsInvalidPeriodsAndDuplicateIds() {
		EngineeringScheduler scheduler = new EngineeringScheduler(1_000, 10);
		scheduler.register(new RecordingDevice("same", 1_000));
		assertThrows(IllegalArgumentException.class, () -> scheduler.register(new RecordingDevice("same", 1_000)));
		assertThrows(IllegalArgumentException.class, () -> scheduler.register(new RecordingDevice("off-grid", 1_500)));
	}

	private static SimulationDevice callbackDevice(String id, long period, java.util.function.Consumer<SimulationContext> callback) {
		return new SimulationDevice() {
			@Override
			public String simulationId() {
				return id;
			}

			@Override
			public long updatePeriodMicros() {
				return period;
			}

			@Override
			public void simulate(SimulationContext context) {
				callback.accept(context);
			}
		};
	}

	private static final class RecordingDevice implements SimulationDevice {
		private final String id;
		private final long period;
		private final List<Long> times = new ArrayList<>();

		private RecordingDevice(String id, long period) {
			this.id = id;
			this.period = period;
		}

		@Override
		public String simulationId() {
			return id;
		}

		@Override
		public long updatePeriodMicros() {
			return period;
		}

		@Override
		public void simulate(SimulationContext context) {
			times.add(context.simulationTimeMicros());
		}
	}
}

