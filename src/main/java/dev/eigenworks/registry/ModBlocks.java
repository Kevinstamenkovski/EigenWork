package dev.eigenworks.registry;

import java.util.function.Function;

import dev.eigenworks.block.EngineeringTestBenchBlock;
import dev.eigenworks.block.DigitalClockBlock;
import dev.eigenworks.block.DigitalCounterBlock;
import dev.eigenworks.block.DigitalGateBlock;
import dev.eigenworks.block.DigitalRegisterBlock;
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
			EngineeringTestBenchBlock::new,
			BlockBehaviour.Properties.of()
					.strength(3.0F, 6.0F)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops());
	public static final Block DIGITAL_CLOCK = register(
			ModBlockItemIds.DIGITAL_CLOCK,
			DigitalClockBlock::new,
			deviceProperties());
	public static final Block DIGITAL_COUNTER = register(
			ModBlockItemIds.DIGITAL_COUNTER,
			DigitalCounterBlock::new,
			deviceProperties());
	public static final Block DIGITAL_GATE = register(
			ModBlockItemIds.DIGITAL_GATE,
			DigitalGateBlock::new,
			deviceProperties());
	public static final Block DIGITAL_REGISTER = register(
			ModBlockItemIds.DIGITAL_REGISTER,
			DigitalRegisterBlock::new,
			deviceProperties());

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
				.register(entries -> {
					entries.accept(ENGINEERING_TEST_BENCH.asItem());
					entries.accept(DIGITAL_CLOCK.asItem());
					entries.accept(DIGITAL_COUNTER.asItem());
					entries.accept(DIGITAL_GATE.asItem());
					entries.accept(DIGITAL_REGISTER.asItem());
				});
	}

	private static BlockBehaviour.Properties deviceProperties() {
		return BlockBehaviour.Properties.of()
				.strength(2.0F, 6.0F)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops();
	}
}
