package dev.eigenworks.electrical;

/** Immutable MNA node voltages (including ground node 0) and source currents. */
public record CircuitSolution(double[] nodeVoltages, double[] voltageSourceCurrents) {
	public CircuitSolution { nodeVoltages = nodeVoltages.clone(); voltageSourceCurrents = voltageSourceCurrents.clone(); }
	@Override public double[] nodeVoltages() { return nodeVoltages.clone(); }
	@Override public double[] voltageSourceCurrents() { return voltageSourceCurrents.clone(); }
	public double voltage(int node) { return nodeVoltages[node]; }
}
