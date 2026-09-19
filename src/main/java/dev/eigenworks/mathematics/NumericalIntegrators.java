package dev.eigenworks.mathematics;

/** Reusable guarded Forward Euler and classical fourth-order Runge-Kutta steps. */
public final class NumericalIntegrators {
	private NumericalIntegrators() { }
	public static Vector step(IntegrationMethod method, StateDerivative derivative,
			double timeSeconds, Vector state, double deltaSeconds) {
		Vector.requireFinite(timeSeconds);
		Vector.requireFinite(deltaSeconds);
		if (deltaSeconds <= 0 || deltaSeconds > 1.0) throw new IllegalArgumentException("Integration timestep must be within (0, 1] seconds");
		return switch (method) {
			case FORWARD_EULER -> state.add(requireSize(derivative.evaluate(timeSeconds, state), state).scale(deltaSeconds));
			case RK4 -> rk4(derivative, timeSeconds, state, deltaSeconds);
		};
	}
	private static Vector rk4(StateDerivative derivative, double time, Vector state, double dt) {
		Vector k1 = requireSize(derivative.evaluate(time, state), state);
		Vector k2 = requireSize(derivative.evaluate(time + dt / 2, state.add(k1.scale(dt / 2))), state);
		Vector k3 = requireSize(derivative.evaluate(time + dt / 2, state.add(k2.scale(dt / 2))), state);
		Vector k4 = requireSize(derivative.evaluate(time + dt, state.add(k3.scale(dt))), state);
		return state.add(k1.add(k2.scale(2)).add(k3.scale(2)).add(k4).scale(dt / 6));
	}
	private static Vector requireSize(Vector candidate, Vector state) {
		if (candidate == null || candidate.size() != state.size()) throw new IllegalArgumentException("Derivative dimension does not match state");
		return candidate;
	}
}
