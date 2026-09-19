package dev.eigenworks.client.screen;

import dev.eigenworks.instrumentation.OscilloscopeMenu;
import dev.eigenworks.instrumentation.OscilloscopeModel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Multi-channel time-domain plot rendered from a bounded server snapshot. */
public final class OscilloscopeScreen extends AbstractContainerScreen<OscilloscopeMenu> {
	private static final int WIDTH = 300;
	private static final int HEIGHT = 220;
	private static final int[] COLORS = {0xFF6BFF6B, 0xFFFFD35A, 0xFF64C8FF, 0xFFFF6BE4};

	public OscilloscopeScreen(OscilloscopeMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
		titleLabelX = 10;
		titleLabelY = 8;
	}

	@Override protected void init() {
		super.init();
		int controlsY = topPos + HEIGHT - 27;
		addRenderableWidget(button("Pause", leftPos + 8, controlsY, 54, OscilloscopeMenu.BUTTON_PAUSE));
		addRenderableWidget(button("Time", leftPos + 65, controlsY, 48, OscilloscopeMenu.BUTTON_TIME_SCALE));
		addRenderableWidget(button("Scale", leftPos + 116, controlsY, 48, OscilloscopeMenu.BUTTON_VERTICAL_SCALE));
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) {
			addRenderableWidget(button("C" + (channel + 1), leftPos + 168 + channel * 31, controlsY, 28,
					OscilloscopeMenu.BUTTON_CHANNEL_BASE + channel));
		}
	}

	private Button button(String label, int x, int y, int width, int id) {
		return Button.builder(Component.literal(label), ignored -> {
			if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
		}).bounds(x, y, width, 20).build();
	}

	@Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xF00B1117);
		graphics.outline(leftPos, topPos, WIDTH, HEIGHT, 0xFF4DB6AC);
	}

	@Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, titleLabelX, titleLabelY, 0xFF76E5D8, false);
		graphics.text(font, menu.paused() ? "PAUSED" : "SAMPLING", 226, 8, menu.paused() ? 0xFFFFD166 : 0xFF72E572, false);
		int graphLeft = 12;
		int graphTop = 28;
		int graphWidth = 276;
		int graphHeight = 130;
		graphics.fill(graphLeft, graphTop, graphLeft + graphWidth, graphTop + graphHeight, 0xFF03080C);
		graphics.outline(graphLeft, graphTop, graphWidth, graphHeight, 0xFF285B58);
		for (int division = 1; division < 4; division++) {
			int y = graphTop + division * graphHeight / 4;
			graphics.horizontalLine(graphLeft + 1, graphLeft + graphWidth - 2, y, 0xFF13252C);
		}

		int visible = Math.min(menu.visibleSamples(), menu.sampleCount());
		int first = Math.max(0, menu.sampleCount() - visible);
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) {
			if (!menu.channelEnabled(channel) || visible < 2) continue;
			int previousX = 0;
			int previousY = 0;
			boolean havePrevious = false;
			for (int point = 0; point < visible; point++) {
				int value = menu.sample(channel, first + point);
				if (value < 0) { havePrevious = false; continue; }
				int x = graphLeft + 2 + point * (graphWidth - 4) / Math.max(1, visible - 1);
				int y = graphTop + graphHeight - 2 - Math.min(value, menu.verticalMaximum()) * (graphHeight - 4) / menu.verticalMaximum();
				if (havePrevious) {
					graphics.horizontalLine(Math.min(previousX, x), Math.max(previousX, x), previousY, COLORS[channel]);
					graphics.verticalLine(x, Math.min(previousY, y), Math.max(previousY, y), COLORS[channel]);
				}
				previousX = x;
				previousY = y;
				havePrevious = true;
			}
		}

		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) {
			int x = 12 + channel * 70;
			int[] range = visibleRange(channel, first, visible);
			graphics.text(font, "C%d %d".formatted(channel + 1, menu.currentValue(channel)), x, 164, COLORS[channel], false);
			graphics.text(font, "%d..%d".formatted(range[0], range[1]), x, 175, 0xFF9AA7B2, false);
		}
		graphics.text(font, "%d samples | vertical 0..%d".formatted(menu.visibleSamples(), menu.verticalMaximum()),
				12, 188, 0xFF9AA7B2, false);
	}

	private int[] visibleRange(int channel, int first, int visible) {
		int minimum = Integer.MAX_VALUE;
		int maximum = Integer.MIN_VALUE;
		for (int point = 0; point < visible; point++) {
			int value = menu.sample(channel, first + point);
			if (value >= 0) { minimum = Math.min(minimum, value); maximum = Math.max(maximum, value); }
		}
		return minimum == Integer.MAX_VALUE ? new int[] {0, 0} : new int[] {minimum, maximum};
	}
}
