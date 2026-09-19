package dev.eigenworks.mathematics;

/** Finite-dimensional continuous system x_dot=A*x+B*u, y=C*x+D*u. */
public final class StateSpaceSystem {
	private final Matrix a;
	private final Matrix b;
	private final Matrix c;
	private final Matrix d;
	private Vector state;
	private double timeSeconds;

	public StateSpaceSystem(Matrix a, Matrix b, Matrix c, Matrix d, Vector initialState) {
		if (a.rows() != a.columns()) throw new IllegalArgumentException("A must be square");
		if (b.rows() != a.rows()) throw new IllegalArgumentException("B row count must match A");
		if (c.columns() != a.columns()) throw new IllegalArgumentException("C column count must match A");
		if (d.rows() != c.rows() || d.columns() != b.columns()) throw new IllegalArgumentException("D dimensions must match outputs and inputs");
		if (initialState.size() != a.rows()) throw new IllegalArgumentException("Initial state dimension must match A");
		this.a = a; this.b = b; this.c = c; this.d = d; this.state = initialState;
	}

	public Vector derivative(Vector state, Vector input) {
		if (input.size() != b.columns()) throw new IllegalArgumentException("Input dimension does not match B");
		return a.multiply(state).add(b.multiply(input));
	}
	public Vector output(Vector input) {
		if (input.size() != b.columns()) throw new IllegalArgumentException("Input dimension does not match D");
		return c.multiply(state).add(d.multiply(input));
	}
	public Vector step(Vector input, double deltaSeconds, IntegrationMethod method) {
		if (input.size() != b.columns()) throw new IllegalArgumentException("Input dimension does not match B");
		state = NumericalIntegrators.step(method, (time, candidate) -> derivative(candidate, input), timeSeconds, state, deltaSeconds);
		timeSeconds += deltaSeconds;
		return output(input);
	}
	public Vector state() { return new Vector(state.toArray()); }
	public double timeSeconds() { return timeSeconds; }
	public void reset(Vector newState) {
		if (newState.size() != a.rows()) throw new IllegalArgumentException("Reset state dimension must match A");
		state = new Vector(newState.toArray()); timeSeconds = 0;
	}
}
