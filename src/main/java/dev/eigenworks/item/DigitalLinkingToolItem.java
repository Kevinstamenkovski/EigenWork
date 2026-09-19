package dev.eigenworks.item;

import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.simulation.EngineeringSimulation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Selects an output and connects it to a compatible input without world scans. */
public final class DigitalLinkingToolItem extends Item {
	public DigitalLinkingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		Player player = context.getPlayer();
		if (player == null || !(context.getLevel() instanceof ServerLevel level)) {
			return InteractionResult.PASS;
		}
		if (!(level.getBlockEntity(context.getClickedPos()) instanceof WorldDigitalDevice device)) {
			notifyPlayer(player, Component.translatable("message.eigenworks.link_not_device"));
			return InteractionResult.FAIL;
		}
		DigitalWorldNetwork network = EngineeringSimulation.digitalNetwork(level.getServer());
		try {
			if (player.isShiftKeyDown()) {
				String port = network.disconnectFirst(device);
				notifyPlayer(player, Component.translatable("message.eigenworks.link_disconnected", port));
				return InteractionResult.SUCCESS_SERVER;
			}
			DigitalSourceEndpoint selection = network.selection(player.getUUID());
			if (selection == null) {
				DigitalSourceEndpoint selected = network.selectFirstOutput(player.getUUID(), device);
				notifyPlayer(player, Component.translatable(
						"message.eigenworks.link_selected", selected.port(), selected.width()));
			} else if (selection.device().equals(device.digitalAddress())) {
				DigitalSourceEndpoint selected = network.selectNextOutput(player.getUUID(), device);
				notifyPlayer(player, Component.translatable(
						"message.eigenworks.link_selected", selected.port(), selected.width()));
			} else {
				String inputPort = network.connectSelected(player.getUUID(), device);
				notifyPlayer(player, Component.translatable(
						"message.eigenworks.link_connected", selection.port(), inputPort, selection.width()));
			}
			return InteractionResult.SUCCESS_SERVER;
		} catch (IllegalArgumentException | IllegalStateException exception) {
			notifyPlayer(player, Component.translatable(
					"message.eigenworks.link_error", exception.getMessage()));
			return InteractionResult.FAIL;
		}
	}

	private static void notifyPlayer(Player player, Component message) {
		if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection == null) {
			return;
		}
		player.sendSystemMessage(message);
	}
}
