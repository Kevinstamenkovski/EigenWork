package dev.eigenworks.instrumentation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/** Safe bounded CSV exporter that cannot escape its caller-provided export directory. */
public final class CsvDataLogger {
	private CsvDataLogger() { }

	public static Path export(Path exportDirectory, String fileStem, List<List<InstrumentSample>> channels) throws IOException {
		if (exportDirectory == null || channels == null || channels.isEmpty()) throw new IllegalArgumentException("Export directory and channels are required");
		String safeStem = sanitize(fileStem);
		Path root = exportDirectory.toAbsolutePath().normalize();
		Files.createDirectories(root);
		Path target = root.resolve(safeStem + ".csv").normalize();
		if (!target.getParent().equals(root)) throw new IllegalArgumentException("CSV export escaped its directory");

		StringBuilder csv = new StringBuilder("time_us");
		for (int channel = 0; channel < channels.size(); channel++) csv.append(",ch").append(channel + 1);
		csv.append('\n');
		int rows = channels.stream().mapToInt(List::size).max().orElse(0);
		for (int row = 0; row < rows; row++) {
			long timestamp = 0;
			for (List<InstrumentSample> channel : channels) {
				if (row < channel.size()) { timestamp = channel.get(row).timestampMicros(); break; }
			}
			csv.append(timestamp);
			for (List<InstrumentSample> channel : channels) {
				csv.append(',');
				if (row < channel.size() && channel.get(row).valid()) csv.append(Double.toString(channel.get(row).value()));
			}
			csv.append('\n');
		}
		Files.writeString(target, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		return target;
	}

	private static String sanitize(String value) {
		String safe = value == null ? "engineering_log" : value.replaceAll("[^A-Za-z0-9._-]", "_");
		if (safe.isBlank() || safe.equals(".") || safe.equals("..")) return "engineering_log";
		return safe.length() > 80 ? safe.substring(0, 80) : safe;
	}
}
