package dev.eigenworks.electrical;

/** Guarded circuit-solver failure with a player-facing engineering diagnostic. */
public final class LinearSolveException extends RuntimeException {
	public LinearSolveException(String message) { super(message); }
}
