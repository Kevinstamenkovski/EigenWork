package dev.eigenworks.client;

import dev.eigenworks.client.screen.ComputerDebuggerScreen;
import dev.eigenworks.client.screen.OscilloscopeScreen;
import dev.eigenworks.client.screen.MotorRigScreen;
import dev.eigenworks.client.screen.MathematicsWorkstationScreen;
import dev.eigenworks.client.screen.FactoryCellScreen;
import dev.eigenworks.client.screen.AdvancedConsoleScreen;
import dev.eigenworks.registry.ModMenus;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.client.render.RobotJointModuleRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only rendering and GUI registration. */
public final class EigenWorksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.COMPUTER, ComputerDebuggerScreen::new);
		MenuScreens.register(ModMenus.OSCILLOSCOPE, OscilloscopeScreen::new);
		MenuScreens.register(ModMenus.MOTOR_RIG, MotorRigScreen::new);
		MenuScreens.register(ModMenus.MATHEMATICS_WORKSTATION,MathematicsWorkstationScreen::new);
		MenuScreens.register(ModMenus.FACTORY_CELL,FactoryCellScreen::new);
		MenuScreens.register(ModMenus.ADVANCED_CONSOLE,AdvancedConsoleScreen::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.ROBOT_JOINT_MODULE, RobotJointModuleRenderer::new);
	}
}
