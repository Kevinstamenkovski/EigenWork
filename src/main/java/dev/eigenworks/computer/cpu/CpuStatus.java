package dev.eigenworks.computer.cpu;

/** Observable execution state of the Eigen-8 processor. */
public enum CpuStatus {
	PAUSED,
	RUNNING,
	HALTED,
	FAULTED
}
