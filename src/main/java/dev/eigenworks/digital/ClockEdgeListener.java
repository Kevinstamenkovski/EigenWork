package dev.eigenworks.digital;

/** Sequential digital device driven by logical clock transitions. */
@FunctionalInterface
public interface ClockEdgeListener {
	void onClockEdge(ClockEdgeEvent event);
}

