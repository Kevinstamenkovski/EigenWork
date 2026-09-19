package dev.eigenworks.instrumentation;

import java.util.List;

/** Four-channel bounded time-series sampler independent of Minecraft rendering. */
public final class OscilloscopeModel {
	public static final int CHANNEL_COUNT = 4;
	private final SampleRingBuffer[] histories;
	private final double[] inputs = new double[CHANNEL_COUNT];
	private final boolean[] inputValid = new boolean[CHANNEL_COUNT];
	private final boolean[] channelEnabled = {true, true, true, true};
	private boolean paused;

	public OscilloscopeModel(int historyCapacity) {
		histories = new SampleRingBuffer[CHANNEL_COUNT];
		for (int index = 0; index < CHANNEL_COUNT; index++) histories[index] = new SampleRingBuffer(historyCapacity);
	}

	public void setInput(int channel, double value, boolean valid) {
		requireChannel(channel);
		if (!Double.isFinite(value)) throw new IllegalArgumentException("Oscilloscope input must be finite");
		inputs[channel] = value;
		inputValid[channel] = valid;
	}

	public void sample(long timestampMicros) {
		if (paused) return;
		for (int channel = 0; channel < CHANNEL_COUNT; channel++) {
			if (channelEnabled[channel]) histories[channel].add(new InstrumentSample(timestampMicros, inputs[channel], inputValid[channel]));
		}
	}

	public void setPaused(boolean paused) { this.paused = paused; }
	public boolean paused() { return paused; }
	public void setChannelEnabled(int channel, boolean enabled) { requireChannel(channel); channelEnabled[channel] = enabled; }
	public boolean channelEnabled(int channel) { requireChannel(channel); return channelEnabled[channel]; }
	public List<InstrumentSample> channelSnapshot(int channel) { requireChannel(channel); return histories[channel].snapshot(); }
	public SampleRingBuffer channelHistory(int channel) { requireChannel(channel); return histories[channel]; }
	public double currentValue(int channel) { requireChannel(channel); return inputs[channel]; }
	public boolean currentValid(int channel) { requireChannel(channel); return inputValid[channel]; }

	private static void requireChannel(int channel) {
		if (channel < 0 || channel >= CHANNEL_COUNT) throw new IllegalArgumentException("Channel must be 0..3");
	}
}
