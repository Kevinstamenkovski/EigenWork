package dev.eigenworks.computer.assembly;

import java.util.Map;

/** Immutable bytecode, source, and address-to-line debug mapping. */
public record AssembledProgram(byte[] bytecode, Map<Integer, Integer> addressToLine, String source) {
	public AssembledProgram {
		bytecode = bytecode.clone();
		addressToLine = Map.copyOf(addressToLine);
		source = source == null ? "" : source;
	}

	@Override
	public byte[] bytecode() {
		return bytecode.clone();
	}
}
