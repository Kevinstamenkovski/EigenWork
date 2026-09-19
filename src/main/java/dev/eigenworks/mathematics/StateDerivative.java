package dev.eigenworks.mathematics;

/** Time/state derivative used by numerical integration algorithms. */
@FunctionalInterface
public interface StateDerivative {
	Vector evaluate(double timeSeconds, Vector state);
}
