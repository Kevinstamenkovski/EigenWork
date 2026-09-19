package dev.eigenworks.registry;

import java.util.function.Function;

import dev.eigenworks.block.EngineeringTestBenchBlock;
import dev.eigenworks.block.DigitalClockBlock;
import dev.eigenworks.block.DigitalCounterBlock;
import dev.eigenworks.block.DigitalGateBlock;
import dev.eigenworks.block.DigitalRegisterBlock;
import dev.eigenworks.block.ComputerBlock;
import dev.eigenworks.block.MicrocontrollerBlock;
import dev.eigenworks.block.OscilloscopeBlock;
import dev.eigenworks.block.MotorRigBlock;
import dev.eigenworks.block.MathematicsWorkstationBlock;
import dev.eigenworks.block.CommunicationHubBlock;
import dev.eigenworks.block.RobotArmBlock;
import dev.eigenworks.block.FactoryCellBlock;
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
	public static final Block COMPUTER = register(
			ModBlockItemIds.COMPUTER,
			ComputerBlock::new,
			deviceProperties());
	public static final Block MICROCONTROLLER = register(
			ModBlockItemIds.MICROCONTROLLER,
			MicrocontrollerBlock::new,
			deviceProperties());
	public static final Block OSCILLOSCOPE = register(
			ModBlockItemIds.OSCILLOSCOPE,
			OscilloscopeBlock::new,
			deviceProperties());
	public static final Block MOTOR_RIG = register(ModBlockItemIds.MOTOR_RIG,MotorRigBlock::new,deviceProperties());
	public static final Block MATHEMATICS_WORKSTATION = register(
			ModBlockItemIds.MATHEMATICS_WORKSTATION, MathematicsWorkstationBlock::new, deviceProperties());
	public static final Block COMMUNICATION_HUB = register(
			ModBlockItemIds.COMMUNICATION_HUB, CommunicationHubBlock::new, deviceProperties());
	public static final Block ROBOT_ARM = register(ModBlockItemIds.ROBOT_ARM, RobotArmBlock::new, deviceProperties());
	public static final Block FACTORY_CELL = register(ModBlockItemIds.FACTORY_CELL, FactoryCellBlock::new, deviceProperties());

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
					entries.accept(COMPUTER.asItem());
					entries.accept(MICROCONTROLLER.asItem());
					entries.accept(OSCILLOSCOPE.asItem());
					entries.accept(MOTOR_RIG.asItem());
					entries.accept(MATHEMATICS_WORKSTATION.asItem());
					entries.accept(COMMUNICATION_HUB.asItem());
					entries.accept(ROBOT_ARM.asItem());
					entries.accept(FACTORY_CELL.asItem());
				});
	}

	private static BlockBehaviour.Properties deviceProperties() {
		return BlockBehaviour.Properties.of()
				.strength(2.0F, 6.0F)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops();
	}
}
