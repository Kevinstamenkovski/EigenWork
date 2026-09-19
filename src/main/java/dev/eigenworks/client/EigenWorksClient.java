package dev.eigenworks.client;

import dev.eigenworks.client.screen.ComputerDebuggerScreen;
import dev.eigenworks.client.screen.OscilloscopeScreen;
import dev.eigenworks.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only rendering and GUI registration. */
public final class EigenWorksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.COMPUTER, ComputerDebuggerScreen::new);
		MenuScreens.register(ModMenus.OSCILLOSCOPE, OscilloscopeScreen::new);
	}
}
