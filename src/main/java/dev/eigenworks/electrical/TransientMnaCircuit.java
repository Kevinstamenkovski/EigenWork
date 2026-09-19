package dev.eigenworks.electrical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stateful transient Modified Nodal Analysis solver. Capacitors and inductors
 * use backward Euler companion models; Shockley diodes use guarded Newton iteration.
 */
public final class TransientMnaCircuit {
	private static final double MIN_TIMESTEP = 1.0e-9;
	private static final double NEWTON_TOLERANCE = 1.0e-9;
	private static final int MAX_NEWTON_ITERATIONS = 80;
	private final int nodeCount;
	private final List<Resistor> resistors = new ArrayList<>();
	private final List<VoltageSource> voltageSources = new ArrayList<>();
	private final List<CurrentSource> currentSources = new ArrayList<>();
	private final List<Capacitor> capacitors = new ArrayList<>();
	private final List<Inductor> inductors = new ArrayList<>();
	private final List<Diode> diodes = new ArrayList<>();
	private double[] previousVoltages;
	private double timeSeconds;

	public TransientMnaCircuit(int nonGroundNodes) {
		if (nonGroundNodes < 1 || nonGroundNodes > 128) throw new IllegalArgumentException("Circuit requires 1..128 non-ground nodes");
		nodeCount = nonGroundNodes;
		previousVoltages = new double[nonGroundNodes + 1];
	}

	public TransientMnaCircuit resistor(int positive, int negative, double ohms) {
		validateNodes(positive, negative); positiveFinite(ohms, "Resistance");
		if (ohms < 1.0e-6) throw new IllegalArgumentException("Resistance must be at least 1 micro-ohm");
		resistors.add(new Resistor(positive, negative, ohms)); return this;
	}
	public TransientMnaCircuit voltageSource(int positive, int negative, double volts) {
		validateNodes(positive, negative); finite(volts); voltageSources.add(new VoltageSource(positive, negative, volts)); return this;
	}
	public TransientMnaCircuit currentSource(int positive, int negative, double amperes) {
		validateNodes(positive, negative); finite(amperes); currentSources.add(new CurrentSource(positive, negative, amperes)); return this;
	}
	public TransientMnaCircuit capacitor(int positive, int negative, double farads) {
		validateNodes(positive, negative); positiveFinite(farads, "Capacitance"); capacitors.add(new Capacitor(positive, negative, farads)); return this;
	}
	public TransientMnaCircuit inductor(int positive, int negative, double henries) {
		validateNodes(positive, negative); positiveFinite(henries, "Inductance"); inductors.add(new Inductor(positive, negative, henries)); return this;
	}
	public TransientMnaCircuit diode(int anode, int cathode, double saturationAmps, double ideality, double thermalVolts) {
		validateNodes(anode, cathode); positiveFinite(saturationAmps, "Diode saturation current");
		positiveFinite(ideality, "Diode ideality"); positiveFinite(thermalVolts, "Thermal voltage");
		diodes.add(new Diode(anode, cathode, saturationAmps, ideality, thermalVolts)); return this;
	}
	public TransientMnaCircuit diode(int anode, int cathode) { return diode(anode, cathode, 1.0e-12, 1.0, 0.02585); }

	/** Advances and commits exactly one timestep. Failed solves leave all state unchanged. */
	public TransientCircuitSolution step(double timestepSeconds) {
		if (!Double.isFinite(timestepSeconds) || timestepSeconds < MIN_TIMESTEP || timestepSeconds > 1.0)
			throw new IllegalArgumentException("Timestep must be finite and in [1 ns, 1 s]");
		int branchCount = voltageSources.size() + inductors.size();
		int size = nodeCount + branchCount;
		double[] guess = new double[size];
		System.arraycopy(previousVoltages, 1, guess, 0, nodeCount);
		for (int i = 0; i < inductors.size(); i++) guess[nodeCount + voltageSources.size() + i] = inductors.get(i).previousCurrent;
		double[] diodeLinearization = new double[diodes.size()];
		for (int i = 0; i < diodes.size(); i++) {
			Diode diode = diodes.get(i);
			diodeLinearization[i] = voltage(guess, diode.positive, diode.negative);
		}
		double[] solved = null;
		int iterations = 0;
		for (; iterations < MAX_NEWTON_ITERATIONS; iterations++) {
			double[][] matrix = new double[size][size];
			double[] rhs = new double[size];
			stampLinear(matrix, rhs, timestepSeconds);
			for (int index = 0; index < diodes.size(); index++) {
				Diode diode = diodes.get(index);
				double requested = voltage(guess, diode.positive, diode.negative);
				double vd = clamp(requested, diodeLinearization[index] - 0.2, diodeLinearization[index] + 0.2);
				diodeLinearization[index] = vd;
				double exponent = clamp(vd / (diode.ideality * diode.thermalVolts), -40.0, 40.0);
				double exponential = Math.exp(exponent);
				double current = diode.saturationAmps * (exponential - 1.0);
				double conductance = diode.saturationAmps * exponential / (diode.ideality * diode.thermalVolts);
				stampConductance(matrix, diode.positive, diode.negative, conductance);
				stampCurrent(rhs, diode.positive, diode.negative, current - conductance * vd);
			}
			solved = DenseLinearSolver.solve(matrix, rhs);
			ensureFinite(solved);
			double largestChange = 0;
			for (int i = 0; i < solved.length; i++) largestChange = Math.max(largestChange, Math.abs(solved[i] - guess[i]));
			guess = solved;
			boolean diodePointsReached = true;
			for (int i = 0; i < diodes.size(); i++) {
				Diode diode = diodes.get(i);
				if (Math.abs(voltage(guess, diode.positive, diode.negative) - diodeLinearization[i]) > NEWTON_TOLERANCE) diodePointsReached = false;
			}
			if (largestChange <= NEWTON_TOLERANCE && diodePointsReached) { iterations++; break; }
		}
		if (solved == null || iterations >= MAX_NEWTON_ITERATIONS) throw new LinearSolveException("NONLINEAR CIRCUIT DID NOT CONVERGE");

		double[] acceptedVoltages = new double[nodeCount + 1];
		System.arraycopy(solved, 0, acceptedVoltages, 1, nodeCount);
		for (Inductor inductor : inductors) {
			int index = inductors.indexOf(inductor);
			inductor.previousCurrent = solved[nodeCount + voltageSources.size() + index];
		}
		previousVoltages = acceptedVoltages;
		timeSeconds += timestepSeconds;
		double[] sourceCurrents = Arrays.copyOfRange(solved, nodeCount, nodeCount + voltageSources.size());
		double[] inductorCurrents = Arrays.copyOfRange(solved, nodeCount + voltageSources.size(), size);
		return new TransientCircuitSolution(timeSeconds, acceptedVoltages, sourceCurrents, inductorCurrents, iterations);
	}

	private void stampLinear(double[][] matrix, double[] rhs, double dt) {
		for (Resistor r : resistors) stampConductance(matrix, r.positive, r.negative, 1.0 / r.ohms);
		for (CurrentSource source : currentSources) stampCurrent(rhs, source.positive, source.negative, source.amperes);
		for (int i = 0; i < voltageSources.size(); i++) {
			VoltageSource source = voltageSources.get(i); stampBranch(matrix, source.positive, source.negative, nodeCount + i);
			rhs[nodeCount + i] = source.volts;
		}
		for (Capacitor capacitor : capacitors) {
			double conductance = capacitor.farads / dt;
			double oldVoltage = previousVoltages[capacitor.positive] - previousVoltages[capacitor.negative];
			stampConductance(matrix, capacitor.positive, capacitor.negative, conductance);
			stampCurrent(rhs, capacitor.positive, capacitor.negative, -conductance * oldVoltage);
		}
		for (int i = 0; i < inductors.size(); i++) {
			Inductor inductor = inductors.get(i); int branch = nodeCount + voltageSources.size() + i;
			stampBranch(matrix, inductor.positive, inductor.negative, branch);
			double companionResistance = inductor.henries / dt;
			matrix[branch][branch] -= companionResistance;
			rhs[branch] = -companionResistance * inductor.previousCurrent;
		}
	}
	private static void stampBranch(double[][] matrix, int positive, int negative, int branch) {
		if (positive != 0) { matrix[positive - 1][branch] += 1; matrix[branch][positive - 1] += 1; }
		if (negative != 0) { matrix[negative - 1][branch] -= 1; matrix[branch][negative - 1] -= 1; }
	}
	private static void stampConductance(double[][] matrix, int positive, int negative, double conductance) {
		if (positive != 0) matrix[positive - 1][positive - 1] += conductance;
		if (negative != 0) matrix[negative - 1][negative - 1] += conductance;
		if (positive != 0 && negative != 0) { matrix[positive - 1][negative - 1] -= conductance; matrix[negative - 1][positive - 1] -= conductance; }
	}
	private static void stampCurrent(double[] rhs, int positive, int negative, double amperes) {
		if (positive != 0) rhs[positive - 1] -= amperes;
		if (negative != 0) rhs[negative - 1] += amperes;
	}
	private static double voltage(double[] vector, int positive, int negative) {
		return (positive == 0 ? 0 : vector[positive - 1]) - (negative == 0 ? 0 : vector[negative - 1]);
	}
	private void validateNodes(int positive, int negative) {
		if (positive < 0 || positive > nodeCount || negative < 0 || negative > nodeCount || positive == negative)
			throw new IllegalArgumentException("Component nodes are invalid");
	}
	private static void positiveFinite(double value, String name) { if (!Double.isFinite(value) || value <= 0) throw new IllegalArgumentException(name + " must be positive and finite"); }
	private static void finite(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("Circuit value must be finite"); }
	private static double clamp(double value, double minimum, double maximum) { return Math.max(minimum, Math.min(maximum, value)); }
	private static void ensureFinite(double[] values) { for (double value : values) if (!Double.isFinite(value)) throw new LinearSolveException("NON-FINITE CIRCUIT STATE"); }

	private record Resistor(int positive, int negative, double ohms) { }
	private record VoltageSource(int positive, int negative, double volts) { }
	private record CurrentSource(int positive, int negative, double amperes) { }
	private record Capacitor(int positive, int negative, double farads) { }
	private static final class Inductor { private final int positive, negative; private final double henries; private double previousCurrent; private Inductor(int p, int n, double h) { positive=p; negative=n; henries=h; } }
	private record Diode(int positive, int negative, double saturationAmps, double ideality, double thermalVolts) { }
}
