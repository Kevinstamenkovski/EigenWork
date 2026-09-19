package dev.eigenworks.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Shared concise tooltips for standalone EigenWorks tools. */
final class EngineeringTooltips {
	private EngineeringTooltips() { }

	static void append(String itemPath, Consumer<Component> output) {
		output.accept(Component.translatable("item.eigenworks." + itemPath + ".summary").withStyle(ChatFormatting.GRAY));
		output.accept(Component.translatable("item.eigenworks." + itemPath + ".controls").withStyle(ChatFormatting.AQUA));
	}
}
