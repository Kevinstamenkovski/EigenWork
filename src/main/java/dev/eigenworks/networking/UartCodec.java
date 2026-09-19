package dev.eigenworks.networking;

import java.util.ArrayList;
import java.util.List;

/** UART start/data/parity/stop framing and validation. */
public final class UartCodec {
	private UartCodec() { }
	public static UartFrame encode(int value, UartConfig config) {
		int mask = (1 << config.dataBits()) - 1;
		if (value < 0 || value > mask) throw new IllegalArgumentException("UART value exceeds configured data width");
		List<Boolean> bits = new ArrayList<>(config.frameBits());
		bits.add(false);
		int ones = 0;
		for (int bit = 0; bit < config.dataBits(); bit++) { boolean high = ((value >>> bit) & 1) != 0; bits.add(high); if (high) ones++; }
		if (config.parity() != UartParity.NONE) bits.add(config.parity() == UartParity.EVEN ? (ones & 1) != 0 : (ones & 1) == 0);
		for (int stop = 0; stop < config.stopBits(); stop++) bits.add(true);
		return new UartFrame(bits, value);
	}
	public static int decode(UartFrame frame, UartConfig config) {
		if (frame.bits().size() != config.frameBits() || frame.bits().getFirst()) throw new IllegalArgumentException("UART FRAME ERROR");
		int value = 0, ones = 0;
		for (int bit = 0; bit < config.dataBits(); bit++) if (frame.bits().get(bit + 1)) { value |= 1 << bit; ones++; }
		int cursor = 1 + config.dataBits();
		if (config.parity() != UartParity.NONE) {
			boolean expected = config.parity() == UartParity.EVEN ? (ones & 1) != 0 : (ones & 1) == 0;
			if (frame.bits().get(cursor++) != expected) throw new IllegalArgumentException("UART PARITY ERROR");
		}
		while (cursor < frame.bits().size()) if (!frame.bits().get(cursor++)) throw new IllegalArgumentException("UART STOP BIT ERROR");
		return value;
	}
}
