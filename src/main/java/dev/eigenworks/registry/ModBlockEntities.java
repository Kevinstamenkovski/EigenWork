package dev.eigenworks.registry;

import dev.eigenworks.EigenWorks;
import dev.eigenworks.block.entity.DigitalClockBlockEntity;
import dev.eigenworks.block.entity.DigitalCounterBlockEntity;
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

	private ModBlockEntities() {
	}

	public static void initialize() {
		// Class loading performs registration.
	}
}
