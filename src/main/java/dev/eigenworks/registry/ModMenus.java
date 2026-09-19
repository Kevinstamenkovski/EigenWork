package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.computer.ComputerMenu;
import dev.eigenworks.instrumentation.OscilloscopeMenu;
import dev.eigenworks.control.MotorRigMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

/** Registration boundary for server-authoritative device menus. */
public final class ModMenus {
	public static final MenuType<ComputerMenu> COMPUTER = Registry.register(
			BuiltInRegistries.MENU,
			EigenWorks.id("computer"),
			new MenuType<>(ComputerMenu::new, FeatureFlagSet.of()));
	public static final MenuType<OscilloscopeMenu> OSCILLOSCOPE = Registry.register(
			BuiltInRegistries.MENU,
			EigenWorks.id("oscilloscope"),
			new MenuType<>(OscilloscopeMenu::new, FeatureFlagSet.of()));
	public static final MenuType<MotorRigMenu> MOTOR_RIG = Registry.register(
			BuiltInRegistries.MENU, EigenWorks.id("motor_rig"),
			new MenuType<>(MotorRigMenu::new, FeatureFlagSet.of()));

	private ModMenus() { }
	public static void initialize() { }
}
