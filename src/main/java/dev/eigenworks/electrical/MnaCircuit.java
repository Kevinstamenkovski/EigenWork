package dev.eigenworks.electrical;

import java.util.ArrayList;
import java.util.List;

/** Guarded DC Modified Nodal Analysis for resistors and independent sources. */
public final class MnaCircuit {
	private final int nodeCount;
	private final List<Resistor> resistors = new ArrayList<>();
	private final List<VoltageSource> voltageSources = new ArrayList<>();
	private final List<CurrentSource> currentSources = new ArrayList<>();

	public MnaCircuit(int nonGroundNodes) {
		if (nonGroundNodes < 1 || nonGroundNodes > 128) throw new IllegalArgumentException("Circuit requires 1..128 non-ground nodes");
		nodeCount = nonGroundNodes;
	}
	public MnaCircuit resistor(int positive, int negative, double ohms) {
		validateNodes(positive, negative);
		if (!Double.isFinite(ohms) || ohms < 1.0e-6) throw new IllegalArgumentException("Resistance must be finite and at least 1 micro-ohm");
		resistors.add(new Resistor(positive, negative, ohms)); return this;
	}
	public MnaCircuit voltageSource(int positive, int negative, double volts) {
		validateNodes(positive, negative); finite(volts);
		voltageSources.add(new VoltageSource(positive, negative, volts)); return this;
	}
	public MnaCircuit currentSource(int positive, int negative, double amperes) {
		validateNodes(positive, negative); finite(amperes);
		currentSources.add(new CurrentSource(positive, negative, amperes)); return this;
	}

	public CircuitSolution solve() {
		int size = nodeCount + voltageSources.size();
		double[][] a = new double[size][size];
		double[] z = new double[size];
		for (Resistor resistor : resistors) {
			double conductance = 1.0 / resistor.ohms();
			stampConductance(a, resistor.positive(), resistor.negative(), conductance);
		}
		for (CurrentSource source : currentSources) {
			if (source.positive() != 0) z[source.positive() - 1] -= source.amperes();
			if (source.negative() != 0) z[source.negative() - 1] += source.amperes();
		}
		for (int index = 0; index < voltageSources.size(); index++) {
			VoltageSource source = voltageSources.get(index);
			int branch = nodeCount + index;
			if (source.positive() != 0) { int node = source.positive() - 1; a[node][branch] += 1; a[branch][node] += 1; }
			if (source.negative() != 0) { int node = source.negative() - 1; a[node][branch] -= 1; a[branch][node] -= 1; }
			z[branch] = source.volts();
		}
		double[] solved = DenseLinearSolver.solve(a, z);
		double[] voltages = new double[nodeCount + 1];
		System.arraycopy(solved, 0, voltages, 1, nodeCount);
		double[] sourceCurrents = new double[voltageSources.size()];
		System.arraycopy(solved, nodeCount, sourceCurrents, 0, sourceCurrents.length);
		return new CircuitSolution(voltages, sourceCurrents);
	}

	private void stampConductance(double[][] a, int p, int n, double g) {
		if (p != 0) a[p - 1][p - 1] += g;
		if (n != 0) a[n - 1][n - 1] += g;
		if (p != 0 && n != 0) { a[p - 1][n - 1] -= g; a[n - 1][p - 1] -= g; }
	}
	private void validateNodes(int p, int n) {
		if (p < 0 || p > nodeCount || n < 0 || n > nodeCount || p == n) throw new IllegalArgumentException("Component nodes are invalid");
	}
	private static double finite(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("Circuit value must be finite"); return value; }
	private record Resistor(int positive, int negative, double ohms) { }
	private record VoltageSource(int positive, int negative, double volts) { }
	private record CurrentSource(int positive, int negative, double amperes) { }
}
