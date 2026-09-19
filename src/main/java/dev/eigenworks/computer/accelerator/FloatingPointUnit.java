package dev.eigenworks.computer.accelerator;

/** Optional logical FPU whose operations are finite-checked and cycle-accounted. */
public final class FloatingPointUnit {
	public enum Operation { ADD, SUBTRACT, MULTIPLY, DIVIDE, SQRT, SIN, COS }
	public AcceleratorResult<Double> execute(Operation operation, double left, double right) {
		if (operation == null || !Double.isFinite(left) || !Double.isFinite(right)) throw new IllegalArgumentException("FPU operands must be finite");
		double value;
		int cycles;
		switch (operation) {
			case ADD -> { value = left + right; cycles = 2; }
			case SUBTRACT -> { value = left - right; cycles = 2; }
			case MULTIPLY -> { value = left * right; cycles = 4; }
			case DIVIDE -> { if (right == 0) throw new ArithmeticException("FPU DIVIDE BY ZERO"); value = left / right; cycles = 12; }
			case SQRT -> { if (left < 0) throw new ArithmeticException("FPU DOMAIN ERROR"); value = Math.sqrt(left); cycles = 16; }
			case SIN -> { value = Math.sin(left); cycles = 24; }
			case COS -> { value = Math.cos(left); cycles = 24; }
			default -> throw new IllegalStateException("Unsupported FPU operation");
		}
		if (!Double.isFinite(value)) throw new ArithmeticException("FPU NON-FINITE RESULT");
		return new AcceleratorResult<>(value, cycles, "FPU OK");
	}
}
