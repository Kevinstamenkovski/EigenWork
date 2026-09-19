package dev.eigenworks.automation;

/** IEC-style non-retentive on-delay timer. */
public final class PlcOnDelayTimer {
	private final long presetMicros; private long elapsedMicros;
	public PlcOnDelayTimer(long presetMicros) { if (presetMicros <= 0) throw new IllegalArgumentException("Timer preset must be positive"); this.presetMicros = presetMicros; }
	public boolean update(boolean enabled, long deltaMicros) { if (deltaMicros <= 0) throw new IllegalArgumentException("Timer timestep must be positive"); elapsedMicros = enabled ? Math.min(presetMicros, Math.addExact(elapsedMicros, deltaMicros)) : 0; return done(); }
	public boolean done() { return elapsedMicros >= presetMicros; } public long elapsedMicros() { return elapsedMicros; }
}
