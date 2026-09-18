package dev.eigenworks;

import dev.eigenworks.config.EngineeringConfig;
import dev.eigenworks.registry.ModBlocks;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.registry.ModItems;
import dev.eigenworks.simulation.EngineeringSimulation;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main server-safe entry point for EigenWorks. */
public final class EigenWorks implements ModInitializer {
	public static final String MOD_ID = "eigenworks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EngineeringConfig.validateDefaults();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		EngineeringSimulation.initialize();
		LOGGER.info("EigenWorks {} initialized", BuildInfo.VERSION);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
