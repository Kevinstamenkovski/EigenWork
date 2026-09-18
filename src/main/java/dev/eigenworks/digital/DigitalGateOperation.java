package dev.eigenworks.digital;

import dev.eigenworks.signal.DigitalWord;

/** Supported operations for the configurable placeable digital gate. */
public enum DigitalGateOperation {
	NOT,
	AND,
	OR,
	XOR,
	NAND,
	NOR;

	public DigitalWord apply(DigitalWord inputA, DigitalWord inputB) {
		return switch (this) {
			case NOT -> DigitalLogic.not(inputA);
			case AND -> DigitalLogic.and(inputA, inputB);
			case OR -> DigitalLogic.or(inputA, inputB);
			case XOR -> DigitalLogic.xor(inputA, inputB);
			case NAND -> DigitalLogic.nand(inputA, inputB);
			case NOR -> DigitalLogic.nor(inputA, inputB);
		};
	}

	public DigitalGateOperation next() {
		DigitalGateOperation[] operations = values();
		return operations[(ordinal() + 1) % operations.length];
	}
}
