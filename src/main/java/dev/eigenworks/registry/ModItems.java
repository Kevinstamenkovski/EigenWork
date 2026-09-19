package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.item.DigitalLinkingToolItem;
import dev.eigenworks.item.EngineeringInspectorItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Registration boundary for standalone engineering items. */
public final class ModItems {
	private static final ResourceKey<Item> DIGITAL_LINKING_TOOL_KEY = ResourceKey.create(
			Registries.ITEM, EigenWorks.id("digital_linking_tool"));
	private static final ResourceKey<Item> ENGINEERING_INSPECTOR_KEY = ResourceKey.create(
			Registries.ITEM, EigenWorks.id("engineering_inspector"));

	public static final Item DIGITAL_LINKING_TOOL = Registry.register(
			BuiltInRegistries.ITEM,
			DIGITAL_LINKING_TOOL_KEY,
			new DigitalLinkingToolItem(new Item.Properties().stacksTo(1).setId(DIGITAL_LINKING_TOOL_KEY)));
	public static final Item ENGINEERING_INSPECTOR = Registry.register(
			BuiltInRegistries.ITEM,
			ENGINEERING_INSPECTOR_KEY,
			new EngineeringInspectorItem(new Item.Properties().stacksTo(1).setId(ENGINEERING_INSPECTOR_KEY)));

	private ModItems() {
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register(entries -> {
					entries.accept(DIGITAL_LINKING_TOOL);
					entries.accept(ENGINEERING_INSPECTOR);
				});
	}
}
