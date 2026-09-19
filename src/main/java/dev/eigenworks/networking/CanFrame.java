package dev.eigenworks.networking;

import java.util.Arrays;

/** Immutable standard (11-bit identifier) CAN data frame. */
public record CanFrame(int identifier, byte[] payload) {
	public CanFrame {
		if (identifier < 0 || identifier > 0x7ff) throw new IllegalArgumentException("CAN identifier must be 0..0x7FF");
		if (payload == null || payload.length > 8) throw new IllegalArgumentException("CAN payload must contain at most 8 bytes");
		payload = Arrays.copyOf(payload, payload.length);
	}
	@Override public byte[] payload() { return Arrays.copyOf(payload, payload.length); }
}
