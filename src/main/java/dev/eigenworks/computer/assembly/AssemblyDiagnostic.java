package dev.eigenworks.computer.assembly;

/** Source-located assembler error suitable for an in-game editor. */
public record AssemblyDiagnostic(int line, int column, String message, String sourceLine) {
	public AssemblyDiagnostic {
		if (line < 1 || column < 1) {
			throw new IllegalArgumentException("Diagnostic locations are one-based");
		}
		message = message == null ? "Unknown assembly error" : message;
		sourceLine = sourceLine == null ? "" : sourceLine;
	}

	public String displayMessage() {
		return "Line %d:%d: %s".formatted(line, column, message);
	}
}
