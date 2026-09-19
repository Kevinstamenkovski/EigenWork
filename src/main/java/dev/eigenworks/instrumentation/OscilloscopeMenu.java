package dev.eigenworks.instrumentation;

import dev.eigenworks.block.entity.OscilloscopeBlockEntity;
import dev.eigenworks.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Bounded synchronized snapshot and server-validated controls for the oscilloscope. */
public final class OscilloscopeMenu extends AbstractContainerMenu {
	public static final int HISTORY = 64;
	public static final int HEADER_DATA = 12;
	public static final int DATA_COUNT = HEADER_DATA + OscilloscopeModel.CHANNEL_COUNT * HISTORY;
	public static final int BUTTON_PAUSE = 0;
	public static final int BUTTON_TIME_SCALE = 1;
	public static final int BUTTON_VERTICAL_SCALE = 2;
	public static final int BUTTON_CHANNEL_BASE = 10;

	private final ContainerData data;
	private final OscilloscopeBlockEntity oscilloscope;

	public OscilloscopeMenu(int containerId, Inventory inventory) {
		this(containerId, new SimpleContainerData(DATA_COUNT), null);
	}

	public OscilloscopeMenu(int containerId, OscilloscopeBlockEntity oscilloscope) {
		this(containerId, oscilloscope.menuData(), oscilloscope);
	}

	private OscilloscopeMenu(int containerId, ContainerData data, OscilloscopeBlockEntity oscilloscope) {
		super(ModMenus.OSCILLOSCOPE, containerId);
		checkContainerDataCount(data, DATA_COUNT);
		this.data = data;
		this.oscilloscope = oscilloscope;
		addDataSlots(data);
	}

	@Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override public boolean stillValid(Player player) { return oscilloscope == null || oscilloscope.stillValid(player); }
	@Override public boolean clickMenuButton(Player player, int id) {
		if (oscilloscope == null || !stillValid(player)) return false;
		if (id == BUTTON_PAUSE) oscilloscope.togglePaused();
		else if (id == BUTTON_TIME_SCALE) oscilloscope.nextTimeScale();
		else if (id == BUTTON_VERTICAL_SCALE) oscilloscope.nextVerticalScale();
		else if (id >= BUTTON_CHANNEL_BASE && id < BUTTON_CHANNEL_BASE + OscilloscopeModel.CHANNEL_COUNT) {
			oscilloscope.toggleChannel(id - BUTTON_CHANNEL_BASE);
		} else return false;
		return true;
	}

	public boolean paused() { return data.get(0) != 0; }
	public int timeScaleIndex() { return data.get(1); }
	public int verticalScaleIndex() { return data.get(2); }
	public int sampleCount() { return Math.clamp(data.get(3), 0, HISTORY); }
	public boolean channelEnabled(int channel) { return data.get(4 + channel) != 0; }
	public int currentValue(int channel) { return data.get(8 + channel); }
	public int sample(int channel, int index) { return data.get(HEADER_DATA + channel * HISTORY + index); }
	public int visibleSamples() { return switch (timeScaleIndex()) { case 0 -> 16; case 1 -> 32; default -> 64; }; }
	public int verticalMaximum() { return switch (verticalScaleIndex()) { case 0 -> 1; case 1 -> 15; default -> 255; }; }
}
