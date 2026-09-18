package dev.eigenworks.digital;

/** Immutable logical clock transition. */
public record ClockEdgeEvent(String clockId, ClockEdge edge, long timestampMicros, long nominalPeriodMicros) {
	public ClockEdgeEvent {
		if (clockId == null || clockId.isBlank()) {
			throw new IllegalArgumentException("Clock id cannot be blank");
		}
		if (edge == null) {
			throw new NullPointerException("edge");
		}
		if (timestampMicros < 0) {
			throw new IllegalArgumentException("Clock timestamp cannot be negative");
		}
		if (nominalPeriodMicros <= 0) {
			throw new IllegalArgumentException("Clock nominal period must be positive");
		}
	}
}
