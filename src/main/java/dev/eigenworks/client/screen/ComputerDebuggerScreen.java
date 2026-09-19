package dev.eigenworks.client.screen;

import dev.eigenworks.computer.ComputerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact engineering-style live debugger with authoritative run controls. */
public final class ComputerDebuggerScreen extends AbstractContainerScreen<ComputerMenu> {
	private static final int WIDTH = 252;
	private static final int HEIGHT = 178;

	public ComputerDebuggerScreen(ComputerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
		titleLabelX = 10;
		titleLabelY = 8;
	}

	@Override
	protected void init() {
		super.init();
		int y = topPos + HEIGHT - 29;
		addRenderableWidget(button("Run", leftPos + 10, y, ComputerMenu.BUTTON_RUN));
		addRenderableWidget(button("Pause", leftPos + 69, y, ComputerMenu.BUTTON_PAUSE));
		addRenderableWidget(button("Reset", leftPos + 128, y, ComputerMenu.BUTTON_RESET));
		addRenderableWidget(button("Step", leftPos + 187, y, ComputerMenu.BUTTON_STEP));
	}

	private Button button(String label, int x, int y, int id) {
		return Button.builder(Component.literal(label), ignored -> {
			if (minecraft != null && minecraft.gameMode != null) {
				minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
			}
		}).bounds(x, y, 55, 20).build();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xF0121720);
		graphics.outline(leftPos, topPos, WIDTH, HEIGHT, 0xFF4DB6AC);
		graphics.fill(leftPos + 8, topPos + 22, leftPos + WIDTH - 8, topPos + 23, 0xFF285B58);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, titleLabelX, titleLabelY, 0xFF76E5D8, false);
		graphics.text(font, "Status: " + menu.status(), 10, 28, statusColor(), false);
		graphics.text(font, "PC  0x%04X".formatted(menu.programCounter()), 10, 42, 0xFFE5E9F0, false);
		graphics.text(font, "SP  0x%04X".formatted(menu.stackPointer()), 10, 54, 0xFFE5E9F0, false);
		String opcode = menu.currentOpcode() > 0xFF ? "ILLEGAL" : "0x%02X".formatted(menu.currentOpcode());
		graphics.text(font, "Next opcode  " + opcode, 10, 66, 0xFFE5E9F0, false);
		graphics.text(font, "Cycles  " + menu.totalCycles(), 10, 78, 0xFFE5E9F0, false);
		graphics.text(font, "Flags  " + flagText(), 10, 90, 0xFFE5E9F0, false);

		for (int index = 0; index < 8; index++) {
			int x = 130 + (index % 2) * 56;
			int y = 30 + (index / 2) * 14;
			graphics.text(font, "R%d  %02X".formatted(index, menu.register(index)), x, y, 0xFFB4E1DC, false);
		}

		graphics.fill(10, 108, WIDTH - 10, 136, 0xFF1C2630);
		graphics.text(font, "Demo A memory[0x0020]", 16, 113, 0xFFAAB4BE, false);
		graphics.text(font, "%d (0x%02X)".formatted(menu.demoResult(), menu.demoResult()), 169, 113,
				menu.demoResult() == 42 ? 0xFF72E572 : 0xFFFFD166, false);
	}

	private String flagText() {
		int flags = menu.flags();
		return "%s %s %s %s".formatted(
				(flags & 1) != 0 ? "Z" : "-",
				(flags & 2) != 0 ? "N" : "-",
				(flags & 4) != 0 ? "C" : "-",
				(flags & 8) != 0 ? "V" : "-");
	}

	private int statusColor() {
		return switch (menu.status()) {
			case RUNNING -> 0xFF72E572;
			case PAUSED -> 0xFFFFD166;
			case HALTED -> 0xFF76E5D8;
			case FAULTED -> 0xFFFF6868;
		};
	}
}
