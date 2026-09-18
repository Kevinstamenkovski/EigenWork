package dev.eigenworks.block.entity;

import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Primitive-field persistence for optional digital source endpoints. */
final class DigitalLinkStorage {
	private DigitalLinkStorage() {
	}

	static DigitalSourceEndpoint read(ValueInput input, String prefix) {
		if (!input.getBooleanOr(prefix + "_connected", false)) {
			return null;
		}
		String dimension = input.getStringOr(prefix + "_dimension", "");
		String port = input.getStringOr(prefix + "_port", "");
		int width = input.getIntOr(prefix + "_width", 0);
		if (dimension.isBlank() || port.isBlank() || width < 1 || width > Long.SIZE) {
			return null;
		}
		return new DigitalSourceEndpoint(
				new DigitalDeviceAddress(dimension, input.getLongOr(prefix + "_position", 0L)),
				port,
				width);
	}

	static void write(ValueOutput output, String prefix, DigitalSourceEndpoint source) {
		output.putBoolean(prefix + "_connected", source != null);
		if (source == null) {
			return;
		}
		output.putString(prefix + "_dimension", source.device().dimension());
		output.putLong(prefix + "_position", source.device().blockPosition());
		output.putString(prefix + "_port", source.port());
		output.putInt(prefix + "_width", source.width());
	}
}
