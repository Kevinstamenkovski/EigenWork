package dev.eigenworks.computer.cpu;

/** Result of one bounded logical execution slice. */
public record CpuRunReport(int instructionsExecuted, int cyclesConsumed, CpuStatus status, CpuFault fault) {
}
