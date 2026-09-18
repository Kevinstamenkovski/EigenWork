package dev.eigenworks.digital;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.eigenworks.signal.BooleanSignal;
import dev.eigenworks.signal.EngineeringUnit;
import dev.eigenworks.signal.SignalSample;
import dev.eigenworks.signal.SignalSource;
import dev.eigenworks.signal.SignalValidity;
import dev.eigenworks.simulation.SimulationContext;
import dev.eigenworks.simulation.SimulationDevice;

/**
 * Efficient logical clock sampled by the engineering scheduler. Frequency is
 * represented mathematically; no physical CPU-cycle loop is performed.
 */
public final class LogicalClock implements SimulationDevice {
	private static final double MICROS_PER_SECOND = 1_000_000.0;

	private final String id;
	private final long updatePeriodMicros;
	private final List<ClockEdgeListener> listeners = new ArrayList<>();
	private final SignalSource<BooleanSignal> output = new SignalSource<>();
	private double frequencyHertz;
	private double dutyCycle;
	private double phaseCycles;
	private boolean enabled;
	private boolean level;
	private boolean initialized;

	public LogicalClock(
			String id,
			long updatePeriodMicros,
			double frequencyHertz,
			double dutyCycle,
			double phaseCycles,
			boolean enabled) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Clock id cannot be blank");
		}
		if (updatePeriodMicros <= 0) {
			throw new IllegalArgumentException("Clock update period must be positive");
		}
		this.id = id;
		this.updatePeriodMicros = updatePeriodMicros;
		this.frequencyHertz = requireFinitePositive(frequencyHertz, "frequency");
		this.dutyCycle = requireDutyCycle(dutyCycle);
		this.phaseCycles = normalizePhase(phaseCycles);
		this.enabled = enabled;
		validateResolution();
	}

	@Override
	public String simulationId() {
		return id;
	}

	@Override
	public long updatePeriodMicros() {
		return updatePeriodMicros;
	}

	@Override
	public void simulate(SimulationContext context) {
		boolean newLevel = enabled && levelAt(context.simulationTimeMicros());
		if (!initialized || newLevel != level) {
			if (newLevel != level) {
				emitEdge(newLevel ? ClockEdge.RISING : ClockEdge.FALLING, context.simulationTimeMicros());
			}
			initialized = true;
		}
		level = newLevel;
		output.publish(new SignalSample<>(
				new BooleanSignal(level),
				context.simulationTimeMicros(),
				id,
				EngineeringUnit.DIMENSIONLESS,
				updatePeriodMicros,
				SignalValidity.VALID,
				false,
				0.0));
	}

	public void addEdgeListener(ClockEdgeListener listener) {
		listeners.add(Objects.requireNonNull(listener, "listener"));
	}

	public boolean removeEdgeListener(ClockEdgeListener listener) {
		return listeners.remove(listener);
	}

	public SignalSource<BooleanSignal> output() {
		return output;
	}

	public boolean level() {
		return level;
	}

	public double frequencyHertz() {
		return frequencyHertz;
	}

	public void setFrequencyHertz(double frequencyHertz) {
		double previous = this.frequencyHertz;
		this.frequencyHertz = requireFinitePositive(frequencyHertz, "frequency");
		try {
			validateResolution();
		} catch (RuntimeException exception) {
			this.frequencyHertz = previous;
			throw exception;
		}
	}

	public double dutyCycle() {
		return dutyCycle;
	}

	public void setDutyCycle(double dutyCycle) {
		double previous = this.dutyCycle;
		this.dutyCycle = requireDutyCycle(dutyCycle);
		try {
			validateResolution();
		} catch (RuntimeException exception) {
			this.dutyCycle = previous;
			throw exception;
		}
	}

	public boolean enabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean levelAt(long timestampMicros) {
		double cycles = timestampMicros * frequencyHertz / MICROS_PER_SECOND + phaseCycles;
		double fractionalCycle = cycles - Math.floor(cycles);
		return fractionalCycle < dutyCycle;
	}

	private void emitEdge(ClockEdge edge, long timestampMicros) {
		ClockEdgeEvent event = new ClockEdgeEvent(
				id, edge, timestampMicros, Math.max(1L, Math.round(MICROS_PER_SECOND / frequencyHertz)));
		for (ClockEdgeListener listener : List.copyOf(listeners)) {
			listener.onClockEdge(event);
		}
	}

	private void validateResolution() {
		double periodMicros = MICROS_PER_SECOND / frequencyHertz;
		double highMicros = periodMicros * dutyCycle;
		double lowMicros = periodMicros * (1.0 - dutyCycle);
		if (highMicros + 1.0e-9 < updatePeriodMicros || lowMicros + 1.0e-9 < updatePeriodMicros) {
			throw new IllegalArgumentException("Clock high and low phases must each span at least one update period");
		}
	}

	private static double requireFinitePositive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0.0) {
			throw new IllegalArgumentException(name + " must be finite and positive");
		}
		return value;
	}

	private static double requireDutyCycle(double value) {
		if (!Double.isFinite(value) || value <= 0.0 || value >= 1.0) {
			throw new IllegalArgumentException("Duty cycle must be finite and strictly between 0 and 1");
		}
		return value;
	}

	private static double normalizePhase(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Clock phase must be finite");
		}
		return value - Math.floor(value);
	}
}
