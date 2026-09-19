package dev.eigenworks.computer;

import dev.eigenworks.block.entity.ComputerBlockEntity;
import dev.eigenworks.computer.cpu.CpuStatus;
import dev.eigenworks.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Server-synchronized, slotless debugger menu for an Eigen-8 computer. */
public final class ComputerMenu extends AbstractContainerMenu {
	public static final int DATA_COUNT = 16;
	public static final int BUTTON_RUN = 0;
	public static final int BUTTON_PAUSE = 1;
	public static final int BUTTON_RESET = 2;
	public static final int BUTTON_STEP = 3;

	private final ContainerData data;
	private final ComputerBlockEntity computer;

	public ComputerMenu(int containerId, Inventory inventory) {
		this(containerId, new SimpleContainerData(DATA_COUNT), null);
	}

	public ComputerMenu(int containerId, ComputerBlockEntity computer) {
		this(containerId, computer.debugData(), computer);
	}

	private ComputerMenu(int containerId, ContainerData data, ComputerBlockEntity computer) {
		super(ModMenus.COMPUTER, containerId);
		checkContainerDataCount(data, DATA_COUNT);
		this.data = data;
		this.computer = computer;
		addDataSlots(data);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return computer == null || computer.stillValid(player);
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (computer == null || !stillValid(player)) {
			return false;
		}
		return switch (id) {
			case BUTTON_RUN -> { computer.runCpu(); yield true; }
			case BUTTON_PAUSE -> { computer.pauseCpu(); yield true; }
			case BUTTON_RESET -> { computer.resetCpu(); yield true; }
			case BUTTON_STEP -> { computer.stepCpu(); yield true; }
			default -> false;
		};
	}

	public int programCounter() { return data.get(0); }
	public int stackPointer() { return data.get(1); }
	public int register(int index) { return data.get(2 + index); }
	public int flags() { return data.get(10); }
	public CpuStatus status() { return CpuStatus.values()[Math.clamp(data.get(11), 0, CpuStatus.values().length - 1)]; }
	public int currentOpcode() { return data.get(12); }
	public int demoResult() { return data.get(13); }
	public long totalCycles() { return Integer.toUnsignedLong(data.get(14)) | (Integer.toUnsignedLong(data.get(15)) << 16); }
}
