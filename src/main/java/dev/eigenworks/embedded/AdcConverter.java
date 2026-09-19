package dev.eigenworks.embedded;

/** Sample-and-hold ADC with finite reference, quantization, and conversion delay. */
public final class AdcConverter {
	private final int bitDepth;
	private final double referenceVolts;
	private final long conversionDelayMicros;
	private final long samplePeriodMicros;
	private int result;
	private int pendingResult;
	private long completionTimeMicros;
	private boolean busy;
	private long nextSampleTimeMicros;

	public AdcConverter(int bitDepth, double referenceVolts, long conversionDelayMicros) {
		this(bitDepth, referenceVolts, conversionDelayMicros, Math.max(1L, conversionDelayMicros));
	}

	public AdcConverter(int bitDepth, double referenceVolts, long conversionDelayMicros, long samplePeriodMicros) {
		if (bitDepth < 1 || bitDepth > 16) {
			throw new IllegalArgumentException("ADC bit depth must be between 1 and 16");
		}
		if (!Double.isFinite(referenceVolts) || referenceVolts <= 0.0) {
			throw new IllegalArgumentException("ADC reference voltage must be finite and positive");
		}
		if (conversionDelayMicros < 0) {
			throw new IllegalArgumentException("ADC conversion delay cannot be negative");
		}
		if (samplePeriodMicros <= 0) {
			throw new IllegalArgumentException("ADC sample period must be positive");
		}
		this.bitDepth = bitDepth;
		this.referenceVolts = referenceVolts;
		this.conversionDelayMicros = conversionDelayMicros;
		this.samplePeriodMicros = samplePeriodMicros;
	}

	public boolean start(double inputVolts, long nowMicros) {
		if (busy || nowMicros < nextSampleTimeMicros) {
			return false;
		}
		pendingResult = quantize(inputVolts);
		completionTimeMicros = Math.addExact(nowMicros, conversionDelayMicros);
		busy = true;
		nextSampleTimeMicros = Math.addExact(nowMicros, samplePeriodMicros);
		if (conversionDelayMicros == 0) {
			advance(nowMicros);
		}
		return true;
	}

	public boolean advance(long nowMicros) {
		if (!busy || nowMicros < completionTimeMicros) {
			return false;
		}
		result = pendingResult;
		busy = false;
		return true;
	}

	public int quantize(double inputVolts) {
		if (!Double.isFinite(inputVolts)) {
			throw new IllegalArgumentException("ADC input must be finite");
		}
		double clamped = Math.clamp(inputVolts, 0.0, referenceVolts);
		return (int) Math.round(clamped / referenceVolts * maximumCode());
	}

	public int result() { return result; }
	public boolean busy() { return busy; }
	public int bitDepth() { return bitDepth; }
	public int levels() { return 1 << bitDepth; }
	public int maximumCode() { return levels() - 1; }
	public double referenceVolts() { return referenceVolts; }
	public long conversionDelayMicros() { return conversionDelayMicros; }
	public long samplePeriodMicros() { return samplePeriodMicros; }
	public double samplingFrequencyHertz() { return 1_000_000.0 / samplePeriodMicros; }

	public void restoreResult(int result) {
		if (result < 0 || result > maximumCode()) {
			throw new IllegalArgumentException("ADC result is outside converter range");
		}
		this.result = result;
		busy = false;
		nextSampleTimeMicros = 0;
	}
}
