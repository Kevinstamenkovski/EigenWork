package dev.eigenworks.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Lightweight discoverability hint shown when a player enters an EigenWorks world. */
public final class EngineeringOnboarding {
	private EngineeringOnboarding() { }

	public static void initialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				handler.player.sendSystemMessage(Component.translatable("message.eigenworks.welcome")
						.withStyle(ChatFormatting.AQUA)));
	}
}
