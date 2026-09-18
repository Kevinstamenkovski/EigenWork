package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

/** Stable identifiers for blocks that also have inventory items. */
public final class ModBlockItemIds {
	public static final BlockItemId ENGINEERING_TEST_BENCH = create("engineering_test_bench");

	private ModBlockItemIds() {
	}

	private static BlockItemId create(String name) {
		Identifier identifier = EigenWorks.id(name);
		return BlockItemId.create(identifier, identifier);
	}
}
