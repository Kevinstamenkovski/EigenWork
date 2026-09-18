package dev.eigenworks.digital;

import java.util.Objects;

import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.signal.EngineeringUnit;
import dev.eigenworks.signal.SignalSample;
import dev.eigenworks.signal.SignalSource;
import dev.eigenworks.signal.SignalValidity;

/** Edge-triggered parallel register with synchronous reset and load enable. */
public final class DigitalRegister implements ClockEdgeListener {
	private final String id;
	private final int width;
	private final ClockEdge activeEdge;
	private final SignalSource<DigitalWord> output = new SignalSource<>();
	private DigitalWord input;
	private DigitalWord value;
	private boolean loadEnabled = true;
	private boolean resetRequested;

	public DigitalRegister(String id, int width, ClockEdge activeEdge) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Register id cannot be blank");
		}
		this.id = id;
		this.value = new DigitalWord(width, 0L);
		this.input = value;
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
		} else if (loadEnabled) {
			value = input;
		}
		resetRequested = false;
		publish(event.timestampMicros(), event.nominalPeriodMicros());
	}

	public void setInput(DigitalWord input) {
		Objects.requireNonNull(input, "input");
		if (input.width() != width) {
			throw new IllegalArgumentException("Register input width must be " + width);
		}
		this.input = input;
	}

	public DigitalWord value() {
		return value;
	}

	public void setLoadEnabled(boolean loadEnabled) {
		this.loadEnabled = loadEnabled;
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
