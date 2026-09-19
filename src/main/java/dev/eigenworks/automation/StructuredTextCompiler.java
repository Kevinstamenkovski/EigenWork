package dev.eigenworks.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bounded parser for Boolean IF/THEN/ELSE assignments; no arbitrary code execution. */
public final class StructuredTextCompiler {
	private List<String> tokens;
	private int index;
	public StructuredTextProgram compile(String source) {
		if (source == null || source.isBlank() || source.length() > 8_000) throw new IllegalArgumentException("PLC source must contain 1..8000 characters");
		tokens = tokenize(source); index = 0;
		List<StructuredTextProgram.Conditional> statements = new ArrayList<>();
		while (!atEnd()) { statements.add(conditional()); if (statements.size() > 128) throw error("Too many PLC statements"); }
		return new StructuredTextProgram(statements);
	}
	private StructuredTextProgram.Conditional conditional() {
		expect("IF"); var condition = expression(); expect("THEN");
		List<StructuredTextProgram.Assignment> thenAssignments = assignmentsUntil("ELSE", "END_IF");
		List<StructuredTextProgram.Assignment> elseAssignments = List.of();
		if (take("ELSE")) elseAssignments = assignmentsUntil("END_IF");
		expect("END_IF"); take(";");
		if (thenAssignments.isEmpty()) throw error("THEN branch requires an assignment");
		return new StructuredTextProgram.Conditional(condition, List.copyOf(thenAssignments), List.copyOf(elseAssignments));
	}
	private List<StructuredTextProgram.Assignment> assignmentsUntil(String... terminators) {
		List<StructuredTextProgram.Assignment> assignments = new ArrayList<>();
		while (!atEnd() && !isOneOf(peek(), terminators)) {
			String variable = identifier(); expect(":="); var value = expression(); expect(";");
			assignments.add(new StructuredTextProgram.Assignment(variable, value));
			if (assignments.size() > 128) throw error("Too many PLC assignments");
		}
		return assignments;
	}
	private StructuredTextProgram.Expression expression() {
		var value = and(); while (take("OR")) value = new StructuredTextProgram.Binary(value, and(), false); return value;
	}
	private StructuredTextProgram.Expression and() {
		var value = unary(); while (take("AND")) value = new StructuredTextProgram.Binary(value, unary(), true); return value;
	}
	private StructuredTextProgram.Expression unary() {
		if (take("NOT")) return new StructuredTextProgram.Not(unary());
		if (take("(")) { var value = expression(); expect(")"); return value; }
		if (take("TRUE")) return new StructuredTextProgram.Literal(true);
		if (take("FALSE")) return new StructuredTextProgram.Literal(false);
		return new StructuredTextProgram.Variable(identifier());
	}
	private String identifier() {
		if (atEnd() || !tokens.get(index).matches("[A-Z_][A-Z0-9_]*")) throw error("Expected identifier");
		return tokens.get(index++);
	}
	private static List<String> tokenize(String source) {
		List<String> tokens = new ArrayList<>();
		for (int cursor = 0; cursor < source.length();) {
			char current = source.charAt(cursor);
			if (Character.isWhitespace(current)) { cursor++; continue; }
			if (current == '(' || current == ')' || current == ';') { tokens.add(String.valueOf(current)); cursor++; continue; }
			if (current == ':' && cursor + 1 < source.length() && source.charAt(cursor + 1) == '=') { tokens.add(":="); cursor += 2; continue; }
			if (Character.isLetter(current) || current == '_') {
				int start = cursor++; while (cursor < source.length() && (Character.isLetterOrDigit(source.charAt(cursor)) || source.charAt(cursor) == '_')) cursor++;
				tokens.add(source.substring(start, cursor).toUpperCase(Locale.ROOT)); continue;
			}
			throw new IllegalArgumentException("Unexpected PLC character at column " + (cursor + 1));
		}
		return tokens;
	}
	private boolean take(String token) { if (!atEnd() && peek().equals(token)) { index++; return true; } return false; }
	private void expect(String token) { if (!take(token)) throw error("Expected '" + token + "'"); }
	private String peek() { return tokens.get(index); }
	private boolean atEnd() { return index >= tokens.size(); }
	private static boolean isOneOf(String token, String... options) { for (String option : options) if (token.equals(option)) return true; return false; }
	private IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at token " + (index + 1)); }
}
