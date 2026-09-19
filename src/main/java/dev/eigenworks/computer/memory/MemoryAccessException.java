package dev.eigenworks.computer.memory;

/** Explicit memory fault converted into a CPU diagnostic by the processor. */
public final class MemoryAccessException extends RuntimeException {
	public MemoryAccessException(String message) {
		super(message);
	}
}
