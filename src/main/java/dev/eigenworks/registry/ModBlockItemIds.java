package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

/** Stable identifiers for blocks that also have inventory items. */
public final class ModBlockItemIds {
	public static final BlockItemId ENGINEERING_TEST_BENCH = create("engineering_test_bench");
	public static final BlockItemId DIGITAL_CLOCK = create("digital_clock");
	public static final BlockItemId DIGITAL_COUNTER = create("digital_counter");
	public static final BlockItemId DIGITAL_GATE = create("digital_gate");
	public static final BlockItemId DIGITAL_REGISTER = create("digital_register");
	public static final BlockItemId COMPUTER = create("computer");
	public static final BlockItemId MICROCONTROLLER = create("microcontroller");
	public static final BlockItemId OSCILLOSCOPE = create("oscilloscope");
	public static final BlockItemId MOTOR_RIG = create("motor_rig");
	public static final BlockItemId MATHEMATICS_WORKSTATION = create("mathematics_workstation");
	public static final BlockItemId COMMUNICATION_HUB = create("communication_hub");
	public static final BlockItemId ROBOT_ARM = create("robot_arm");

	private ModBlockItemIds() {
	}

	private static BlockItemId create(String name) {
		Identifier identifier = EigenWorks.id(name);
		return BlockItemId.create(identifier, identifier);
	}
}
