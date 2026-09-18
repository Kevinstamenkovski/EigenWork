package dev.eigenworks.digital;

import java.util.Objects;

import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.signal.EngineeringUnit;
import dev.eigenworks.signal.SignalSample;
import dev.eigenworks.signal.SignalSource;
import dev.eigenworks.signal.SignalValidity;

/** Unsigned wrapping counter with synchronous reset and count enable. */
public final class DigitalCounter implements ClockEdgeListener {
	private final String id;
	private final int width;
	private final ClockEdge activeEdge;
	private final SignalSource<DigitalWord> output = new SignalSource<>();
	private DigitalWord value;
	private boolean countEnabled = true;
	private boolean resetRequested;

	public DigitalCounter(String id, int width, ClockEdge activeEdge) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Counter id cannot be blank");
		}
		this.id = id;
		this.value = new DigitalWord(width, 0L);
		this.width = width;
		this.activeEdge = Objects.requireNonNull(activeEdge, "activeEdge");
	}

	@Override
	public void onClockEdge(ClockEdgeEvent event) {
		Objects.requireNonNull(event, "event");
		if (event.edge() != activeEdge) {
			return;
		}
		if (resetRequested) {
			value = new DigitalWord(width, 0L);
		} else if (countEnabled) {
			value = new DigitalWord(width, value.value() + 1L);
		}
		resetRequested = false;
		publish(event.timestampMicros(), event.nominalPeriodMicros());
	}

	public DigitalWord value() {
		return value;
	}

	public void setCountEnabled(boolean countEnabled) {
		this.countEnabled = countEnabled;
	}

	public void requestReset() {
		resetRequested = true;
	}

	public SignalSource<DigitalWord> output() {
		return output;
	}

	private void publish(long timestampMicros, long samplePeriodMicros) {
		output.publish(new SignalSample<>(value, timestampMicros, id, EngineeringUnit.DIMENSIONLESS,
				samplePeriodMicros, SignalValidity.VALID, false, 0.0));
	}
}
