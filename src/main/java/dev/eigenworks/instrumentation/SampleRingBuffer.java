package dev.eigenworks.instrumentation;

import java.util.ArrayList;
import java.util.List;

/** Fixed-capacity, allocation-free-on-append sample history ordered oldest to newest. */
public final class SampleRingBuffer {
	private final InstrumentSample[] samples;
	private int start;
	private int size;

	public SampleRingBuffer(int capacity) {
		if (capacity < 2 || capacity > 65_536) throw new IllegalArgumentException("History capacity must be 2..65536");
		samples = new InstrumentSample[capacity];
	}

	public void add(InstrumentSample sample) {
		if (sample == null) throw new NullPointerException("sample");
		int index = (start + size) % samples.length;
		if (size == samples.length) {
			index = start;
			start = (start + 1) % samples.length;
		} else {
			size++;
		}
		samples[index] = sample;
	}

	public InstrumentSample get(int chronologicalIndex) {
		if (chronologicalIndex < 0 || chronologicalIndex >= size) throw new IndexOutOfBoundsException(chronologicalIndex);
		return samples[(start + chronologicalIndex) % samples.length];
	}

	public InstrumentSample newest() { return size == 0 ? null : get(size - 1); }
	public int size() { return size; }
	public int capacity() { return samples.length; }
	public void clear() { start = 0; size = 0; java.util.Arrays.fill(samples, null); }
	public List<InstrumentSample> snapshot() {
		List<InstrumentSample> copy = new ArrayList<>(size);
		for (int index = 0; index < size; index++) copy.add(get(index));
		return List.copyOf(copy);
	}
}
