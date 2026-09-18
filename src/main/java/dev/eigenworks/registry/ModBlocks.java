package dev.eigenworks.registry;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registration boundary for EigenWorks blocks and their block items. */
public final class ModBlocks {
	public static final Block ENGINEERING_TEST_BENCH = register(
			ModBlockItemIds.ENGINEERING_TEST_BENCH,
			Block::new,
			BlockBehaviour.Properties.of()
					.strength(3.0F, 6.0F)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops());

	private ModBlocks() {
	}

	private static Block register(
			BlockItemId id,
			Function<BlockBehaviour.Properties, Block> blockFactory,
			BlockBehaviour.Properties properties) {
		Block block = register(id.block(), blockFactory, properties);
		BlockItem item = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(id.item()));
		Registry.register(BuiltInRegistries.ITEM, id.item(), item);
		return block;
	}

	private static Block register(
			ResourceKey<Block> key,
			Function<BlockBehaviour.Properties, Block> blockFactory,
			BlockBehaviour.Properties properties) {
		Block block = blockFactory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
				.register(entries -> entries.accept(ENGINEERING_TEST_BENCH.asItem()));
	}
}
