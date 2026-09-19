package dev.eigenworks.electrical;

/** Result of one accepted transient MNA timestep. Arrays are defensive copies. */
public record TransientCircuitSolution(
		double timeSeconds,
		double[] nodeVoltages,
		double[] voltageSourceCurrents,
		double[] inductorCurrents,
		int newtonIterations) {
	public TransientCircuitSolution {
		nodeVoltages = nodeVoltages.clone();
		voltageSourceCurrents = voltageSourceCurrents.clone();
		inductorCurrents = inductorCurrents.clone();
	}
	@Override public double[] nodeVoltages() { return nodeVoltages.clone(); }
	@Override public double[] voltageSourceCurrents() { return voltageSourceCurrents.clone(); }
	@Override public double[] inductorCurrents() { return inductorCurrents.clone(); }
	public double voltage(int node) { return nodeVoltages[node]; }
}
