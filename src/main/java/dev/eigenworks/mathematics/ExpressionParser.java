package dev.eigenworks.mathematics;

import java.util.Locale;

/** Recursive-descent scalar parser; it never evaluates Java or invokes reflection. */
public final class ExpressionParser {
	private final String source;
	private final double x;
	private int index;

	public ExpressionParser(String source) { this(source, 0); }
	public ExpressionParser(String source, double x) {
		if (source == null || source.length() > 2_000) throw new IllegalArgumentException("Expression must contain at most 2000 characters");
		this.source = source;
		this.x = Vector.requireFinite(x);
	}
	public double evaluate() {
		double value = expression();
		skipSpaces();
		if (index != source.length()) throw error("Unexpected token");
		return Vector.requireFinite(value);
	}
	private double expression() {
		double value = term();
		while (true) {
			skipSpaces();
			if (take('+')) value += term();
			else if (take('-')) value -= term();
			else return Vector.requireFinite(value);
		}
	}
	private double term() {
		double value = power();
		while (true) {
			skipSpaces();
			if (take('*')) value *= power();
			else if (take('/')) {
				double divisor = power();
				if (divisor == 0.0) throw error("Division by zero");
				value /= divisor;
			} else return Vector.requireFinite(value);
		}
	}
	private double power() {
		double base = unary();
		skipSpaces();
		return take('^') ? Vector.requireFinite(Math.pow(base, power())) : base;
	}
	private double unary() {
		skipSpaces();
		if (take('+')) return unary();
		if (take('-')) return -unary();
		return primary();
	}
	private double primary() {
		skipSpaces();
		if (take('(')) {
			double value = expression();
			if (!take(')')) throw error("Expected ')'");
			return value;
		}
		if (index < source.length() && (Character.isLetter(source.charAt(index)))) {
			String name = identifier().toLowerCase(Locale.ROOT);
			if (name.equals("pi")) return Math.PI;
			if (name.equals("e")) return Math.E;
			if (name.equals("x")) return x;
			if (!take('(')) throw error("Expected '(' after function");
			double argument = expression();
			if (!take(')')) throw error("Expected ')' after function argument");
			return switch (name) {
				case "sin" -> Math.sin(argument);
				case "cos" -> Math.cos(argument);
				case "tan" -> Math.tan(argument);
				case "sqrt" -> Math.sqrt(argument);
				case "exp" -> Math.exp(argument);
				case "log" -> Math.log(argument);
				default -> throw error("Unknown function '" + name + "'");
			};
		}
		int start = index;
		while (index < source.length() && (Character.isDigit(source.charAt(index)) || source.charAt(index) == '.')) index++;
		if (start == index) throw error("Expected number or expression");
		try { return Double.parseDouble(source.substring(start, index)); }
		catch (NumberFormatException exception) { throw error("Invalid number"); }
	}
	private String identifier() { int start = index; while (index < source.length() && Character.isLetter(source.charAt(index))) index++; return source.substring(start, index); }
	private void skipSpaces() { while (index < source.length() && Character.isWhitespace(source.charAt(index))) index++; }
	private boolean take(char expected) { skipSpaces(); if (index < source.length() && source.charAt(index) == expected) { index++; return true; } return false; }
	private IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at column " + (index + 1)); }
}
