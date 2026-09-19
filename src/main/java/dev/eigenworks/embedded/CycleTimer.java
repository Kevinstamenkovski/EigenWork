package dev.eigenworks.embedded;

/** Periodic MCU timer advanced by executed logical CPU cycles. */
public final class CycleTimer {
	private int reloadCycles = 1_000;
	private int remainingCycles = reloadCycles;
	private int interruptVector = 8;
	private boolean enabled;
	private boolean interruptEnabled;
	private boolean overflowPending;

	public int advance(int cycles) {
		if (cycles < 0) {
			throw new IllegalArgumentException("Timer cycles cannot be negative");
		}
		if (!enabled || cycles == 0) {
			return 0;
		}
		int overflows = 0;
		int left = cycles;
		while (left >= remainingCycles) {
			left -= remainingCycles;
			remainingCycles = reloadCycles;
			overflows++;
			overflowPending = true;
		}
		remainingCycles -= left;
		return overflows;
	}

	public void setReloadCycles(int value) {
		if (value < 1 || value > 0xFFFF) {
			throw new IllegalArgumentException("Timer reload must be 1..65535 cycles");
		}
		reloadCycles = value;
		remainingCycles = Math.min(remainingCycles, reloadCycles);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled && remainingCycles <= 0) remainingCycles = reloadCycles;
	}
	public void setInterruptEnabled(boolean enabled) { interruptEnabled = enabled; }
	public void setInterruptVector(int vector) {
		if (vector < 0 || vector > 0xFF) throw new IllegalArgumentException("Timer vector must be 0..255");
		interruptVector = vector;
	}
	public void clearOverflow() { overflowPending = false; }
	public void restore(int reload, int remaining, int vector, boolean enabled, boolean interruptEnabled, boolean pending) {
		setReloadCycles(reload);
		remainingCycles = Math.clamp(remaining, 1, reloadCycles);
		setInterruptVector(vector);
		this.enabled = enabled;
		this.interruptEnabled = interruptEnabled;
		overflowPending = pending;
	}

	public int reloadCycles() { return reloadCycles; }
	public int remainingCycles() { return remainingCycles; }
	public int interruptVector() { return interruptVector; }
	public boolean enabled() { return enabled; }
	public boolean interruptEnabled() { return interruptEnabled; }
	public boolean overflowPending() { return overflowPending; }
}
