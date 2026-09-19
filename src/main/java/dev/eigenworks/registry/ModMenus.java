package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.computer.ComputerMenu;
import dev.eigenworks.instrumentation.OscilloscopeMenu;
import dev.eigenworks.control.MotorRigMenu;
import dev.eigenworks.mathematics.MathematicsWorkstationMenu;
import dev.eigenworks.automation.FactoryCellMenu;
import dev.eigenworks.computer.accelerator.AdvancedConsoleMenu;
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
	public static final MenuType<MathematicsWorkstationMenu> MATHEMATICS_WORKSTATION=Registry.register(BuiltInRegistries.MENU,EigenWorks.id("mathematics_workstation"),new MenuType<>(MathematicsWorkstationMenu::new,FeatureFlagSet.of()));
	public static final MenuType<FactoryCellMenu> FACTORY_CELL=Registry.register(BuiltInRegistries.MENU,EigenWorks.id("factory_cell"),new MenuType<>(FactoryCellMenu::new,FeatureFlagSet.of()));
	public static final MenuType<AdvancedConsoleMenu> ADVANCED_CONSOLE=Registry.register(BuiltInRegistries.MENU,EigenWorks.id("advanced_console"),new MenuType<>(AdvancedConsoleMenu::new,FeatureFlagSet.of()));

	private ModMenus() { }
	public static void initialize() { }
}
