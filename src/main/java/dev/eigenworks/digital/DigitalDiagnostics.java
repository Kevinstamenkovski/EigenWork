package dev.eigenworks.digital;

import dev.eigenworks.signal.DigitalWord;

/** Small real computations used to validate the in-game digital adapter. */
public final class DigitalDiagnostics {
	private DigitalDiagnostics() {
	}

	public static DigitalDiagnosticResult run(long timestampMicros, long nominalClockPeriodMicros) {
		DigitalWord gateResult = DigitalLogic.and(new DigitalWord(4, 0xC), new DigitalWord(4, 0xA));
		DigitalCounter counter = new DigitalCounter("diagnostic_counter", 4, ClockEdge.RISING);
		counter.onClockEdge(new ClockEdgeEvent(
				"diagnostic_clock", ClockEdge.RISING, timestampMicros, nominalClockPeriodMicros));
		return new DigitalDiagnosticResult(gateResult, counter.value());
	}
}
