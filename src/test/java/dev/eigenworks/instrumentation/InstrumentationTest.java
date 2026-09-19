package dev.eigenworks.instrumentation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

class InstrumentationTest {
	@Test
	void ringBufferEvictsOldestSampleDeterministically() {
		SampleRingBuffer buffer = new SampleRingBuffer(3);
		for (int index = 0; index < 5; index++) buffer.add(new InstrumentSample(index, index * 2.0, true));
		assertEquals(List.of(2L, 3L, 4L), buffer.snapshot().stream().map(InstrumentSample::timestampMicros).toList());
	}

	@Test
	void oscilloscopePauseAndChannelEnableControlSampling() {
		OscilloscopeModel scope = new OscilloscopeModel(8);
		scope.setInput(0, 42, true);
		scope.setChannelEnabled(1, false);
		scope.sample(1_000);
		scope.setPaused(true);
		scope.sample(2_000);
		assertEquals(1, scope.channelHistory(0).size());
		assertEquals(0, scope.channelHistory(1).size());
		assertEquals(42, scope.channelHistory(0).newest().value());
	}

	@Test
	void csvLoggerWritesBoundedColumnsAndSanitizesName(@TempDir Path directory) throws Exception {
		Path exported = CsvDataLogger.export(directory, "../scope demo", List.of(
				List.of(new InstrumentSample(100, 1.5, true)),
				List.of(new InstrumentSample(100, 2.5, true))));
		assertEquals(directory.toAbsolutePath().normalize(), exported.getParent());
		assertEquals(".._scope_demo.csv", exported.getFileName().toString());
		assertEquals("time_us,ch1,ch2\n100,1.5,2.5\n", Files.readString(exported));
	}
}
