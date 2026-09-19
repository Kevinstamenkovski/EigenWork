package dev.eigenworks.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Block item with concise, always-visible purpose and control hints. */
public final class EngineeringBlockItem extends BlockItem {
	public EngineeringBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> output,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, output, flag);
		output.accept(Component.translatable(getDescriptionId() + ".summary").withStyle(ChatFormatting.GRAY));
		output.accept(Component.translatable(getDescriptionId() + ".controls").withStyle(ChatFormatting.AQUA));
		output.accept(Component.translatable("tooltip.eigenworks.handbook_hint").withStyle(ChatFormatting.DARK_GRAY));
	}
}
