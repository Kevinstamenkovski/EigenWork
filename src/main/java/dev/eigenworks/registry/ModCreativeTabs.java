package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Dedicated, subsystem-oriented Creative inventory sections for every EigenWorks item. */
public final class ModCreativeTabs {
	public static final CreativeModeTab TOOLS_AND_DIGITAL = register(
			"tools_and_digital", "itemGroup.eigenworks.tools_and_digital", ModBlocks.ENGINEERING_TEST_BENCH.asItem(),
			(output) -> {
				output.accept(ModItems.ENGINEERS_HANDBOOK);
				output.accept(ModBlocks.ENGINEERING_TEST_BENCH);
				output.accept(ModItems.ENGINEERING_INSPECTOR);
				output.accept(ModItems.DIGITAL_LINKING_TOOL);
				output.accept(ModBlocks.DIGITAL_CLOCK);
				output.accept(ModBlocks.DIGITAL_COUNTER);
				output.accept(ModBlocks.DIGITAL_GATE);
				output.accept(ModBlocks.DIGITAL_REGISTER);
			});

	public static final CreativeModeTab COMPUTING_AND_ANALYSIS = register(
			"computing_and_analysis", "itemGroup.eigenworks.computing_and_analysis", ModBlocks.COMPUTER.asItem(),
			(output) -> {
				output.accept(ModBlocks.COMPUTER);
				output.accept(ModBlocks.MICROCONTROLLER);
				output.accept(ModBlocks.OSCILLOSCOPE);
				output.accept(ModBlocks.MATHEMATICS_WORKSTATION);
				output.accept(ModBlocks.ADVANCED_ENGINEERING_CONSOLE);
			});

	public static final CreativeModeTab CONTROL_AND_ROBOTICS = register(
			"control_and_robotics", "itemGroup.eigenworks.control_and_robotics", ModBlocks.MOTOR_RIG.asItem(),
			(output) -> {
				output.accept(ModBlocks.MOTOR_RIG);
				output.accept(ModBlocks.ROBOT_ARM);
				output.accept(ModBlocks.ROBOT_JOINT_MODULE);
				output.accept(ModBlocks.FACTORY_CELL);
			});

	public static final CreativeModeTab COMMUNICATIONS = register(
			"communications", "itemGroup.eigenworks.communications", ModBlocks.COMMUNICATION_HUB.asItem(),
			(output) -> {
				output.accept(ModBlocks.COMMUNICATION_HUB);
				output.accept(ModBlocks.CAN_NODE);
				output.accept(ModBlocks.CAN_CABLE);
			});

	private ModCreativeTabs() { }

	public static void initialize() {
		// Class loading performs registration after blocks and standalone items exist.
	}

	private static CreativeModeTab register(
			String id,
			String translationKey,
			net.minecraft.world.level.ItemLike icon,
			java.util.function.Consumer<CreativeModeTab.Output> entries) {
		CreativeModeTab tab = FabricCreativeModeTab.builder()
				.title(Component.translatable(translationKey))
				.icon(() -> new ItemStack(icon))
				.displayItems((parameters, output) -> entries.accept(output))
				.build();
		return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, EigenWorks.id(id), tab);
	}
}
