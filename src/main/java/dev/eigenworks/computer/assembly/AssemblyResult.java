package dev.eigenworks.computer.assembly;

import java.util.List;
import java.util.Optional;

/** Result of assembling source; failed results never contain partial bytecode. */
public record AssemblyResult(AssembledProgram program, List<AssemblyDiagnostic> diagnostics) {
	public AssemblyResult {
		diagnostics = List.copyOf(diagnostics);
		if ((program == null) == diagnostics.isEmpty()) {
			throw new IllegalArgumentException("Assembly result must contain either a program or diagnostics");
		}
	}

	public boolean successful() {
		return program != null;
	}

	public Optional<AssembledProgram> assembledProgram() {
		return Optional.ofNullable(program);
	}
}
