package dev.eigenworks.computer.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Explicit non-overlapping address map for RAM, ROM, and future mapped devices. */
public final class MemoryBus implements ByteMemory {
	private final int addressSpaceSize;
	private final List<Region> regions = new ArrayList<>();

	public MemoryBus(int addressSpaceSize) {
		if (addressSpaceSize <= 0 || addressSpaceSize > 65_536) {
			throw new IllegalArgumentException("Address space must be between 1 and 65536 bytes");
		}
		this.addressSpaceSize = addressSpaceSize;
	}

	public void map(int baseAddress, ByteMemory memory) {
		if (memory == null) {
			throw new NullPointerException("memory");
		}
		long endExclusive = (long) baseAddress + memory.size();
		if (baseAddress < 0 || endExclusive > addressSpaceSize) {
			throw new IllegalArgumentException("Memory region lies outside address space");
		}
		Region added = new Region(baseAddress, (int) endExclusive, memory);
		if (regions.stream().anyMatch(existing -> existing.overlaps(added))) {
			throw new IllegalArgumentException("Memory region overlaps an existing mapping");
		}
		regions.add(added);
		regions.sort(Comparator.comparingInt(Region::baseAddress));
	}

	@Override
	public int size() {
		return addressSpaceSize;
	}

	@Override
	public int read(int address) {
		Region region = find(address);
		return region.memory().read(address - region.baseAddress());
	}

	@Override
	public void write(int address, int value) {
		Region region = find(address);
		region.memory().write(address - region.baseAddress(), value);
	}

	private Region find(int address) {
		if (address < 0 || address >= addressSpaceSize) {
			throw new MemoryAccessException("Bus address out of range: " + address);
		}
		return regions.stream()
				.filter(region -> region.contains(address))
				.findFirst()
				.orElseThrow(() -> new MemoryAccessException("UNMAPPED MEMORY ADDRESS: " + address));
	}

	private record Region(int baseAddress, int endExclusive, ByteMemory memory) {
		boolean contains(int address) {
			return address >= baseAddress && address < endExclusive;
		}

		boolean overlaps(Region other) {
			return baseAddress < other.endExclusive && other.baseAddress < endExclusive;
		}
	}
}
