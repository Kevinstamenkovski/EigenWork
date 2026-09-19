package dev.eigenworks.mathematics;

import java.util.Locale;

/** Safe command evaluator used by the in-game mathematics workstation. */
public final class MathWorkstationEngine {
	private static final int MAX_DIMENSION = 8;
	private static final int MAX_INTEGRATION_STEPS = 10_000;

	public String evaluate(String source) {
		if (source == null || source.isBlank()) throw new IllegalArgumentException("Calculation is empty");
		if (source.length() > 2_000) throw new IllegalArgumentException("Calculation must contain at most 2000 characters");
		String trimmed = source.strip();
		int separator = trimmed.indexOf(' ');
		String command = (separator < 0 ? trimmed : trimmed.substring(0, separator)).toLowerCase(Locale.ROOT);
		String argument = separator < 0 ? "" : trimmed.substring(separator + 1).strip();
		return switch (command) {
			case "scalar" -> format(new ExpressionParser(argument).evaluate());
			case "det" -> format(parseMatrix(argument).determinant());
			case "inv" -> format(parseMatrix(argument).inverse());
			case "transpose" -> format(parseMatrix(argument).transpose());
			case "solve" -> solve(argument);
			case "mul" -> multiply(argument);
			case "dot" -> dot(argument);
			case "vadd" -> vectorAdd(argument);
			case "integrate" -> integrate(argument);
			case "differentiate" -> differentiate(argument);
			default -> format(new ExpressionParser(trimmed).evaluate());
		};
	}

	private static String solve(String argument) {
		String[] parts = split(argument, "|");
		return format(parseMatrix(parts[0]).solve(parseVector(parts[1])));
	}
	private static String multiply(String argument) {
		String[] parts = split(argument, "|");
		return format(parseMatrix(parts[0]).multiply(parseMatrix(parts[1])));
	}
	private static String dot(String argument) {
		String[] parts = split(argument, "|");
		return format(parseVector(parts[0]).dot(parseVector(parts[1])));
	}
	private static String vectorAdd(String argument) {
		String[] parts = split(argument, "|");
		return format(parseVector(parts[0]).add(parseVector(parts[1])));
	}
	private static String integrate(String argument) {
		String[] sides = split(argument, ":");
		String[] settings = sides[0].strip().split("\\s+");
		if (settings.length != 3) throw new IllegalArgumentException("Integrate syntax: lower upper even_steps : expression");
		double lower = scalar(settings[0]);
		double upper = scalar(settings[1]);
		int steps;
		try { steps = Integer.parseInt(settings[2]); }
		catch (NumberFormatException exception) { throw new IllegalArgumentException("Integration step count must be an integer"); }
		if (steps < 2 || steps > MAX_INTEGRATION_STEPS || (steps & 1) != 0) throw new IllegalArgumentException("Integration steps must be even and within 2..10000");
		double step = (upper - lower) / steps;
		double sum = evaluateAt(sides[1], lower) + evaluateAt(sides[1], upper);
		for (int index = 1; index < steps; index++) sum += (index % 2 == 0 ? 2 : 4) * evaluateAt(sides[1], lower + index * step);
		return format(Vector.requireFinite(sum * step / 3));
	}
	private static String differentiate(String argument) {
		String[] sides = split(argument, ":");
		String[] settings = sides[0].strip().split("\\s+");
		if (settings.length != 2) throw new IllegalArgumentException("Differentiate syntax: x h : expression");
		double point = scalar(settings[0]);
		double step = scalar(settings[1]);
		if (step <= 0) throw new IllegalArgumentException("Differentiation step must be positive");
		return format((evaluateAt(sides[1], point + step) - evaluateAt(sides[1], point - step)) / (2 * step));
	}
	private static double evaluateAt(String expression, double x) { return new ExpressionParser(expression, x).evaluate(); }
	private static double scalar(String expression) { return new ExpressionParser(expression).evaluate(); }
	private static Vector parseVector(String source) {
		String[] fields = source.strip().split(",");
		if (fields.length == 0 || fields.length > MAX_DIMENSION) throw new IllegalArgumentException("Vector dimension must be within 1..8");
		double[] values = new double[fields.length];
		for (int index = 0; index < fields.length; index++) values[index] = scalar(fields[index]);
		return new Vector(values);
	}
	private static Matrix parseMatrix(String source) {
		String[] rows = source.strip().split(";");
		if (rows.length == 0 || rows.length > MAX_DIMENSION) throw new IllegalArgumentException("Matrix row count must be within 1..8");
		double[][] values = new double[rows.length][];
		for (int row = 0; row < rows.length; row++) {
			values[row] = parseVector(rows[row]).toArray();
			if (row > 0 && values[row].length != values[0].length) throw new IllegalArgumentException("Matrix rows must have equal length");
		}
		return new Matrix(values);
	}
	private static String[] split(String source, String delimiter) {
		String[] parts = source.split("\\Q" + delimiter + "\\E", -1);
		if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) throw new IllegalArgumentException("Command requires two arguments separated by '" + delimiter + "'");
		return parts;
	}
	private static String format(double value) { return String.format(Locale.ROOT, "%.10g", Vector.requireFinite(value)); }
	private static String format(Vector vector) {
		StringBuilder result = new StringBuilder("[");
		for (int index = 0; index < vector.size(); index++) { if (index > 0) result.append(", "); result.append(format(vector.get(index))); }
		return result.append(']').toString();
	}
	private static String format(Matrix matrix) {
		StringBuilder result = new StringBuilder("[");
		for (int row = 0; row < matrix.rows(); row++) {
			if (row > 0) result.append("; ");
			for (int column = 0; column < matrix.columns(); column++) { if (column > 0) result.append(", "); result.append(format(matrix.get(row, column))); }
		}
		return result.append(']').toString();
	}
}
