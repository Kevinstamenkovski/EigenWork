package dev.eigenworks.client.screen;

import dev.eigenworks.control.MotorRigMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Engineering-style live PID editor whose controls are validated on the server. */
public final class MotorRigScreen extends AbstractContainerScreen<MotorRigMenu> {
	private static final int WIDTH = 310, HEIGHT = 230;
	private static final String[] LABELS = {"Kp", "Ki", "Kd", "Target"};
	public MotorRigScreen(MotorRigMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, WIDTH, HEIGHT); titleLabelX = 10; titleLabelY = 8; }
	@Override protected void init() {
		super.init();
		for (int index = 0; index < MotorRigMenu.PARAMETER_COUNT; index++) {
			int y = topPos + 31 + index * 25;
			addRenderableWidget(button("-", leftPos + 168, y, 25, index * 2));
			addRenderableWidget(button("+", leftPos + 197, y, 25, index * 2 + 1));
		}
		addRenderableWidget(button("Reset gains", leftPos + 10, topPos + 143, 83, MotorRigMenu.BUTTON_RESET_PARAMETERS));
		addRenderableWidget(button("Toggle mode", leftPos + 98, topPos + 143, 83, MotorRigMenu.BUTTON_TOGGLE_MODE));
		addRenderableWidget(button("Reset rig", leftPos + 186, topPos + 143, 72, MotorRigMenu.BUTTON_RESET_RIG));
	}
	private Button button(String label, int x, int y, int width, int id) {
		return Button.builder(Component.literal(label), ignored -> { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }).bounds(x, y, width, 20).build();
	}
	@Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xF00B1117); graphics.outline(leftPos, topPos, WIDTH, HEIGHT, 0xFF4DB6AC);
		graphics.fill(leftPos + 232, topPos + 25, leftPos + WIDTH - 10, topPos + 132, 0xFF111B23); graphics.outline(leftPos + 232, topPos + 25, 68, 107, 0xFF285B58);
	}
	@Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, 10, 8, 0xFF76E5D8, false);
		for (int index = 0; index < LABELS.length; index++) {
			int y = 37 + index * 25; String value = index == 3 ? "%.3f rad (%.1f deg)".formatted(menu.parameter(index), Math.toDegrees(menu.parameter(index))) : "%.3f".formatted(menu.parameter(index));
			graphics.text(font, LABELS[index], 12, y, 0xFFB4E1DC, false); graphics.text(font, value, 62, y, 0xFFE5E9F0, false);
		}
		graphics.text(font, "LIVE", 251, 30, 0xFF72E572, false);
		graphics.text(font, "q %.3f".formatted(menu.position()), 239, 49, 0xFFE5E9F0, false);
		graphics.text(font, "e %.3f".formatted(menu.error()), 239, 63, 0xFFE5E9F0, false);
		graphics.text(font, "u %.3f".formatted(menu.output()), 239, 77, 0xFFE5E9F0, false);
		graphics.text(font, "%.1f V".formatted(menu.voltage()), 239, 96, 0xFFFFD166, false);
		graphics.text(font, "%.2f A".formatted(menu.current()), 239, 110, 0xFFFFD166, false);
		graphics.text(font, "Mode: " + menu.controlMode(), 10, 128, menu.controlMode().ordinal() == 1 ? 0xFF72E572 : 0xFFFFD166, false);
		graphics.fill(10, 174, WIDTH - 10, 216, 0xFF111B23);
		graphics.text(font, "P %.3f    I %.3f    D %.3f".formatted(menu.proportional(), menu.integral(), menu.derivative()), 16, 181, 0xFFAAB4BE, false);
		graphics.text(font, "Changes are range-checked and applied by the server.", 16, 199, 0xFF76E5D8, false);
	}
}
