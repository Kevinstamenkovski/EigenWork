package dev.eigenworks.automation;

import java.util.List;

/** Immutable compiled subset of IEC 61131-3 Structured Text. */
public final class StructuredTextProgram {
	interface Expression { boolean evaluate(PlcRuntime runtime); }
	record Literal(boolean value) implements Expression { @Override public boolean evaluate(PlcRuntime runtime) { return value; } }
	record Variable(String name) implements Expression { @Override public boolean evaluate(PlcRuntime runtime) { return runtime.get(name); } }
	record Not(Expression operand) implements Expression { @Override public boolean evaluate(PlcRuntime runtime) { return !operand.evaluate(runtime); } }
	record Binary(Expression left, Expression right, boolean and) implements Expression { @Override public boolean evaluate(PlcRuntime runtime) { return and ? left.evaluate(runtime) && right.evaluate(runtime) : left.evaluate(runtime) || right.evaluate(runtime); } }
	record Assignment(String variable, Expression expression) { void execute(PlcRuntime runtime) { runtime.set(variable, expression.evaluate(runtime)); } }
	record Conditional(Expression condition, List<Assignment> thenAssignments, List<Assignment> elseAssignments) {
		void execute(PlcRuntime runtime) { (condition.evaluate(runtime) ? thenAssignments : elseAssignments).forEach(assignment -> assignment.execute(runtime)); }
	}
	private final List<Conditional> statements;
	StructuredTextProgram(List<Conditional> statements) { this.statements = List.copyOf(statements); }
	public void execute(PlcRuntime runtime) { statements.forEach(statement -> statement.execute(runtime)); }
	public int statementCount() { return statements.size(); }
}
