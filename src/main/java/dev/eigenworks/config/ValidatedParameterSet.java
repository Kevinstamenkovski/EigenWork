package dev.eigenworks.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordered server-owned values governed by immutable engineering parameter definitions. */
public final class ValidatedParameterSet {
	private final List<EngineeringParameter> definitions;
	private final Map<String, Double> values = new LinkedHashMap<>();
	public ValidatedParameterSet(List<EngineeringParameter> definitions) {
		if (definitions == null || definitions.isEmpty()) throw new IllegalArgumentException("At least one parameter is required");
		this.definitions = List.copyOf(definitions);
		for (EngineeringParameter definition : this.definitions) {
			if (values.putIfAbsent(definition.key(), definition.defaultValue()) != null) throw new IllegalArgumentException("Duplicate parameter key: " + definition.key());
		}
	}
	public List<EngineeringParameter> definitions() { return definitions; }
	public double get(String key) { Double value = values.get(key); if (value == null) throw new IllegalArgumentException("Unknown parameter: " + key); return value; }
	public double get(int index) { return get(definitions.get(index).key()); }
	public void set(String key, double value) { EngineeringParameter definition = definition(key); values.put(key, definition.requireValid(value)); }
	public double adjust(int index, int direction) {
		if (direction != -1 && direction != 1) throw new IllegalArgumentException("Parameter direction must be -1 or 1");
		EngineeringParameter definition = definitions.get(index);
		double adjusted = Math.clamp(get(definition.key()) + direction * definition.step(), definition.minimum(), definition.maximum());
		values.put(definition.key(), adjusted); return adjusted;
	}
	public void reset() { for (EngineeringParameter definition : definitions) values.put(definition.key(), definition.defaultValue()); }
	private EngineeringParameter definition(String key) { return definitions.stream().filter(value -> value.key().equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown parameter: " + key)); }
}
