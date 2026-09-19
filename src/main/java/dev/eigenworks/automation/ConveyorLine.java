package dev.eigenworks.automation;

/** One-metre conveyor plant with photoelectric sensor zone and terminal diverter. */
public final class ConveyorLine {
	private FactoryItem item;
	private double positionMeters;
	private RouteOutcome lastOutcome = RouteOutcome.NONE;
	public boolean spawn(FactoryItem value) { if (item != null) return false; item = value; positionMeters = 0; lastOutcome = RouteOutcome.NONE; return true; }
	public void step(boolean enabled, boolean diverter, double deltaSeconds) {
		if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0 || deltaSeconds > 0.1) throw new IllegalArgumentException("Conveyor timestep must be within (0, 0.1]");
		if (item == null || !enabled) return;
		positionMeters += deltaSeconds;
		if (positionMeters >= 1.0) { lastOutcome = diverter ? RouteOutcome.DIVERTED : RouteOutcome.STRAIGHT; item = null; positionMeters = 0; }
	}
	public boolean photoelectricSensor() { return item != null && positionMeters >= 0.35 && positionMeters <= 0.55; }
	public boolean proximitySensor() { return item != null && item.metallic() && positionMeters >= 0.35 && positionMeters <= 0.55; }
	public FactoryItem item() { return item; } public double positionMeters() { return positionMeters; } public RouteOutcome lastOutcome() { return lastOutcome; }
	public void restore(FactoryItem item, double position, RouteOutcome outcome) { this.item = item; positionMeters = Math.clamp(position, 0, 1); lastOutcome = outcome; }
}
