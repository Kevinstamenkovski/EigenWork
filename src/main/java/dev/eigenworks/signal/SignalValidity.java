package dev.eigenworks.signal;

/** Quality state attached to an engineering signal sample. */
public enum SignalValidity {
	VALID,
	STALE,
	DISCONNECTED,
	FAULT
}

