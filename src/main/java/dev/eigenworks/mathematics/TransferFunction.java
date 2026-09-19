package dev.eigenworks.mathematics;

import java.util.Arrays;

/** Proper SISO transfer function converted to controllable canonical state-space form. */
public final class TransferFunction {
	private final StateSpaceSystem system;

	/** Coefficients are supplied in descending powers of s. */
	public TransferFunction(double[] numerator, double[] denominator) {
		if (denominator.length < 2) throw new IllegalArgumentException("Transfer function denominator must have order at least one");
		if (numerator.length == 0 || numerator.length > denominator.length) throw new IllegalArgumentException("Transfer function must be proper and have a numerator");
		for (double value : numerator) Vector.requireFinite(value);
		for (double value : denominator) Vector.requireFinite(value);
		if (Math.abs(denominator[0]) <= 1.0e-12) throw new IllegalArgumentException("Leading denominator coefficient cannot be zero");

		int order = denominator.length - 1;
		double leading = denominator[0];
		double[] paddedNumerator = new double[order + 1];
		System.arraycopy(numerator, 0, paddedNumerator, paddedNumerator.length - numerator.length, numerator.length);
		double direct = paddedNumerator[0] / leading;
		double[][] a = new double[order][order];
		for (int row = 0; row < order - 1; row++) a[row][row + 1] = 1.0;
		for (int column = 0; column < order; column++) a[order - 1][column] = -denominator[order - column] / leading;
		double[][] b = new double[order][1]; b[order - 1][0] = 1.0;
		double[][] c = new double[1][order];
		for (int column = 0; column < order; column++) {
			int coefficient = order - column;
			c[0][column] = paddedNumerator[coefficient] / leading - direct * denominator[coefficient] / leading;
		}
		system = new StateSpaceSystem(new Matrix(a), new Matrix(b), new Matrix(c),
				new Matrix(new double[][] {{direct}}), Vector.zeros(order));
	}

	public double step(double input, double deltaSeconds, IntegrationMethod method) {
		return system.step(new Vector(input), deltaSeconds, method).get(0);
	}
	public double output(double input) { return system.output(new Vector(input)).get(0); }
	public Vector state() { return system.state(); }
	@Override public String toString() { return "TransferFunction[state=" + Arrays.toString(state().toArray()) + "]"; }
}
