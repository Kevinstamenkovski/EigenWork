package dev.eigenworks.automation;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Case-insensitive Boolean PLC variable image. */
public final class PlcRuntime {
	private final Map<String, Boolean> variables = new LinkedHashMap<>();
	public void set(String name, boolean value) { variables.put(key(name), value); }
	public boolean get(String name) { return variables.getOrDefault(key(name), false); }
	public Map<String, Boolean> snapshot() { return Map.copyOf(variables); }
	private static String key(String name) {
		if (name == null || name.isBlank() || name.length() > 64) throw new IllegalArgumentException("PLC variable name is invalid");
		return name.toUpperCase(Locale.ROOT);
	}
}
