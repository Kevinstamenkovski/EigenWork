package dev.eigenworks.item;

import java.util.Set;
import java.util.function.Consumer;

import dev.eigenworks.EigenWorks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** In-game quick start and context help for every placeable EigenWorks device. */
public final class EngineersHandbookItem extends Item {
	private static final Set<String> DEVICE_PATHS = Set.of(
			"engineering_test_bench", "digital_clock", "digital_counter", "digital_gate", "digital_register",
			"computer", "microcontroller", "oscilloscope", "motor_rig", "mathematics_workstation",
			"communication_hub", "robot_arm", "factory_cell", "advanced_engineering_console",
			"can_cable", "can_node", "robot_joint_module");

	public EngineersHandbookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) sendQuickStart(player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		Identifier id = BuiltInRegistries.BLOCK.getKey(context.getLevel().getBlockState(context.getClickedPos()).getBlock());
		if (!hasEntry(context.getLevel().getBlockState(context.getClickedPos()).getBlock())) {
			notifyPlayer(player, Component.translatable("guide.eigenworks.no_entry").withStyle(ChatFormatting.RED));
			return InteractionResult.SUCCESS_SERVER;
		}
		notifyPlayer(player, Component.translatable("guide.eigenworks.device_header",
				Component.translatable("block.eigenworks." + id.getPath())).withStyle(ChatFormatting.GOLD));
		for (int line = 1; line <= 3; line++) {
			notifyPlayer(player, Component.translatable("guide.eigenworks." + id.getPath() + "." + line));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	public static boolean hasEntry(Block block) {
		Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
		return EigenWorks.MOD_ID.equals(blockId.getNamespace()) && DEVICE_PATHS.contains(blockId.getPath());
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> output,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, output, flag);
		EngineeringTooltips.append("engineers_handbook", output);
	}

	private static void notifyPlayer(Player player, Component message) {
		if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection == null) return;
		player.sendSystemMessage(message);
	}

	public static void sendQuickStart(Player player) {
		notifyPlayer(player, Component.translatable("guide.eigenworks.quick_start_header").withStyle(ChatFormatting.GOLD));
		for (int line = 1; line <= 8; line++) {
			notifyPlayer(player, Component.translatable("guide.eigenworks.quick_start." + line));
		}
	}
}
