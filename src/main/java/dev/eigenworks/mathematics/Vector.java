package dev.eigenworks.mathematics;

import java.util.Arrays;

/** Immutable finite real vector. */
public final class Vector {
	private final double[] values;

	public Vector(double... values) {
		if (values.length == 0) throw new IllegalArgumentException("Vector cannot be empty");
		this.values = Arrays.copyOf(values, values.length);
		for (double value : this.values) requireFinite(value);
	}

	public static Vector zeros(int size) {
		if (size < 1) throw new IllegalArgumentException("Vector size must be positive");
		return new Vector(new double[size]);
	}
	public int size() { return values.length; }
	public double get(int index) { return values[index]; }
	public double[] toArray() { return Arrays.copyOf(values, values.length); }
	public Vector add(Vector other) { return combine(other, 1.0); }
	public Vector subtract(Vector other) { return combine(other, -1.0); }
	public Vector scale(double scalar) {
		requireFinite(scalar);
		double[] result = new double[size()];
		for (int index = 0; index < size(); index++) result[index] = requireFinite(values[index] * scalar);
		return new Vector(result);
	}
	public double dot(Vector other) {
		requireSameSize(other);
		double result = 0;
		for (int index = 0; index < size(); index++) result += values[index] * other.values[index];
		return requireFinite(result);
	}
	public double norm() { return Math.sqrt(dot(this)); }

	private Vector combine(Vector other, double sign) {
		requireSameSize(other);
		double[] result = new double[size()];
		for (int index = 0; index < size(); index++) result[index] = requireFinite(values[index] + sign * other.values[index]);
		return new Vector(result);
	}
	private void requireSameSize(Vector other) {
		if (other == null || other.size() != size()) throw new IllegalArgumentException("Vector dimensions do not match");
	}
	static double requireFinite(double value) {
		if (!Double.isFinite(value)) throw new IllegalArgumentException("Numeric value must be finite");
		return value;
	}

	@Override public boolean equals(Object other) { return other instanceof Vector vector && Arrays.equals(values, vector.values); }
	@Override public int hashCode() { return Arrays.hashCode(values); }
	@Override public String toString() { return Arrays.toString(values); }
}
