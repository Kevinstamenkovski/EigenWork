package dev.eigenworks.automation;

/** Rising-edge PLC counter. */
public final class PlcCounter {
	private final int preset; private int count; private boolean previous;
	public PlcCounter(int preset) { if (preset < 1) throw new IllegalArgumentException("Counter preset must be positive"); this.preset = preset; }
	public boolean update(boolean input, boolean reset) { if (reset) count = 0; else if (input && !previous && count < Integer.MAX_VALUE) count++; previous = input; return done(); }
	public boolean done() { return count >= preset; } public int count() { return count; }
}
