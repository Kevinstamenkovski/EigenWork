package dev.eigenworks.embedded;

/** Eight direction-controlled digital GPIO pins. A direction bit of one means output. */
public final class GpioBank8 {
	private int directionMask;
	private int outputLatch;
	private int externalInputs;

	public int directionMask() { return directionMask; }
	public int outputLatch() { return outputLatch; }
	public int externalInputs() { return externalInputs; }

	public int pinValues() {
		return (outputLatch & directionMask) | (externalInputs & (~directionMask & 0xFF));
	}

	public void setDirectionMask(int value) { directionMask = requireByte(value); }
	public void writeOutputs(int value) { outputLatch = requireByte(value); }
	public void sampleExternalInputs(int value) { externalInputs = requireByte(value); }

	public void restore(int directionMask, int outputLatch, int externalInputs) {
		setDirectionMask(directionMask);
		writeOutputs(outputLatch);
		sampleExternalInputs(externalInputs);
	}

	private static int requireByte(int value) {
		if (value < 0 || value > 0xFF) {
			throw new IllegalArgumentException("GPIO value must be an unsigned byte");
		}
		return value;
	}
}
