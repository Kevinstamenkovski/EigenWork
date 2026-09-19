package dev.eigenworks.embedded;

/** Deterministic logical PWM channel with an eight-bit duty register. */
public final class PwmChannel {
	private static final double[] FREQUENCIES = {10.0, 50.0, 100.0, 500.0};
	private int dutyCode;
	private int frequencyIndex = 2;
	private boolean enabled;

	public boolean levelAt(long timeMicros) {
		if (!enabled || dutyCode == 0) {
			return false;
		}
		if (dutyCode == 0xFF) {
			return true;
		}
		long periodMicros = Math.max(1L, Math.round(1_000_000.0 / frequencyHertz()));
		long highMicros = Math.round(periodMicros * dutyFraction());
		return Math.floorMod(timeMicros, periodMicros) < highMicros;
	}

	public void setDutyCode(int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("PWM duty code must be an unsigned byte");
		}
		dutyCode = value;
	}

	public void setFrequencyIndex(int index) {
		if (index < 0 || index >= FREQUENCIES.length) {
			throw new IllegalArgumentException("PWM frequency selector must be 0..3");
		}
		frequencyIndex = index;
	}

	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public int dutyCode() { return dutyCode; }
	public double dutyFraction() { return dutyCode / 255.0; }
	public int frequencyIndex() { return frequencyIndex; }
	public double frequencyHertz() { return FREQUENCIES[frequencyIndex]; }
	public boolean enabled() { return enabled; }
}
