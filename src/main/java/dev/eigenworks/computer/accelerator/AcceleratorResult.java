package dev.eigenworks.computer.accelerator;

/** Result plus logical cycle charge for a bounded accelerator operation. */
public record AcceleratorResult<T>(T value, int cycles, String diagnostic) {
	public AcceleratorResult { if (cycles < 1 || diagnostic == null) throw new IllegalArgumentException("Invalid accelerator result"); }
}
