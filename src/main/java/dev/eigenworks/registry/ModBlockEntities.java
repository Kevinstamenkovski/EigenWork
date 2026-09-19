package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.block.entity.DigitalClockBlockEntity;
import dev.eigenworks.block.entity.DigitalCounterBlockEntity;
import dev.eigenworks.block.entity.DigitalGateBlockEntity;
import dev.eigenworks.block.entity.DigitalRegisterBlockEntity;
import dev.eigenworks.block.entity.ComputerBlockEntity;
import dev.eigenworks.block.entity.MicrocontrollerBlockEntity;
import dev.eigenworks.block.entity.OscilloscopeBlockEntity;
import dev.eigenworks.block.entity.MotorRigBlockEntity;
import dev.eigenworks.block.entity.MathematicsWorkstationBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Registration boundary for persistent engineering block entities. */
public final class ModBlockEntities {
	public static final BlockEntityType<DigitalClockBlockEntity> DIGITAL_CLOCK = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("digital_clock"),
			FabricBlockEntityTypeBuilder.create(DigitalClockBlockEntity::new, ModBlocks.DIGITAL_CLOCK).build());
	public static final BlockEntityType<DigitalCounterBlockEntity> DIGITAL_COUNTER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("digital_counter"),
			FabricBlockEntityTypeBuilder.create(DigitalCounterBlockEntity::new, ModBlocks.DIGITAL_COUNTER).build());
	public static final BlockEntityType<DigitalGateBlockEntity> DIGITAL_GATE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("digital_gate"),
			FabricBlockEntityTypeBuilder.create(DigitalGateBlockEntity::new, ModBlocks.DIGITAL_GATE).build());
	public static final BlockEntityType<DigitalRegisterBlockEntity> DIGITAL_REGISTER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("digital_register"),
			FabricBlockEntityTypeBuilder.create(DigitalRegisterBlockEntity::new, ModBlocks.DIGITAL_REGISTER).build());
	public static final BlockEntityType<ComputerBlockEntity> COMPUTER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("computer"),
			FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, ModBlocks.COMPUTER).build());
	public static final BlockEntityType<MicrocontrollerBlockEntity> MICROCONTROLLER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("microcontroller"),
			FabricBlockEntityTypeBuilder.create(MicrocontrollerBlockEntity::new, ModBlocks.MICROCONTROLLER).build());
	public static final BlockEntityType<OscilloscopeBlockEntity> OSCILLOSCOPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EigenWorks.id("oscilloscope"),
			FabricBlockEntityTypeBuilder.create(OscilloscopeBlockEntity::new, ModBlocks.OSCILLOSCOPE).build());
	public static final BlockEntityType<MotorRigBlockEntity> MOTOR_RIG=Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,EigenWorks.id("motor_rig"),FabricBlockEntityTypeBuilder.create(MotorRigBlockEntity::new,ModBlocks.MOTOR_RIG).build());
	public static final BlockEntityType<MathematicsWorkstationBlockEntity> MATHEMATICS_WORKSTATION = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, EigenWorks.id("mathematics_workstation"),
			FabricBlockEntityTypeBuilder.create(MathematicsWorkstationBlockEntity::new, ModBlocks.MATHEMATICS_WORKSTATION).build());

	private ModBlockEntities() {
	}

	public static void initialize() {
		// Class loading performs registration.
	}
}
