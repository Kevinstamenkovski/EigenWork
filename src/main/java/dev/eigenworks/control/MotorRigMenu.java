package dev.eigenworks.control;

import dev.eigenworks.block.entity.MotorRigBlockEntity;
import dev.eigenworks.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Slotless synchronized motor/PID configuration menu with server-applied step controls. */
public final class MotorRigMenu extends AbstractContainerMenu {
	public static final int DATA_COUNT = 13;
	public static final int PARAMETER_COUNT = 4;
	public static final int BUTTON_RESET_PARAMETERS = 20;
	public static final int BUTTON_TOGGLE_MODE = 21;
	public static final int BUTTON_RESET_RIG = 22;
	private final ContainerData data;
	private final MotorRigBlockEntity rig;
	public MotorRigMenu(int containerId, Inventory inventory) { this(containerId, new SimpleContainerData(DATA_COUNT), null); }
	public MotorRigMenu(int containerId, MotorRigBlockEntity rig) { this(containerId, rig.menuData(), rig); }
	private MotorRigMenu(int containerId, ContainerData data, MotorRigBlockEntity rig) { super(ModMenus.MOTOR_RIG, containerId); checkContainerDataCount(data, DATA_COUNT); this.data = data; this.rig = rig; addDataSlots(data); }
	@Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override public boolean stillValid(Player player) { return rig == null || rig.stillValid(player); }
	@Override public boolean clickMenuButton(Player player, int id) {
		if (rig == null || !stillValid(player)) return false;
		if (id >= 0 && id < PARAMETER_COUNT * 2) rig.adjustControllerParameter(id / 2, id % 2 == 0 ? -1 : 1);
		else if (id == BUTTON_RESET_PARAMETERS) rig.resetControllerParameters();
		else if (id == BUTTON_TOGGLE_MODE) rig.toggleControlMode();
		else if (id == BUTTON_RESET_RIG) rig.resetRig();
		else return false;
		return true;
	}
	public double parameter(int index) { return data.get(index) / 1_000.0; }
	public MotorRigBlockEntity.ControlMode controlMode() { return MotorRigBlockEntity.ControlMode.values()[Math.clamp(data.get(4), 0, MotorRigBlockEntity.ControlMode.values().length - 1)]; }
	public double position() { return data.get(5) / 1_000.0; }
	public double error() { return data.get(6) / 1_000.0; }
	public double output() { return data.get(7) / 1_000.0; }
	public double proportional() { return data.get(8) / 1_000.0; }
	public double integral() { return data.get(9) / 1_000.0; }
	public double derivative() { return data.get(10) / 1_000.0; }
	public double voltage() { return data.get(11) / 100.0; }
	public double current() { return data.get(12) / 100.0; }
}
