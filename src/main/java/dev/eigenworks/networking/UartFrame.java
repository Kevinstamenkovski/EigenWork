package dev.eigenworks.networking;

import java.util.List;

/** Explicit low-first UART bit frame. */
public record UartFrame(List<Boolean> bits, int value) {
	public UartFrame { bits = List.copyOf(bits); }
}
