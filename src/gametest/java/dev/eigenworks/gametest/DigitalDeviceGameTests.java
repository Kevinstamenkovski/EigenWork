package dev.eigenworks.gametest;

import java.lang.reflect.Method;
import java.util.UUID;

import dev.eigenworks.block.entity.DigitalClockBlockEntity;
import dev.eigenworks.block.entity.DigitalCounterBlockEntity;
import dev.eigenworks.block.entity.DigitalGateBlockEntity;
import dev.eigenworks.block.entity.DigitalRegisterBlockEntity;
import dev.eigenworks.digital.DigitalGateOperation;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.registry.ModBlocks;
import dev.eigenworks.registry.ModItems;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.EngineeringSimulation;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Minecraft-level verification for digital placement, propagation, and persistence. */
public final class DigitalDeviceGameTests implements CustomTestMethodInvoker {
	private static final BlockPos CLOCK_POS = new BlockPos(1, 1, 1);
	private static final BlockPos COUNTER_POS = new BlockPos(2, 1, 1);

	@GameTest
	public void clockToCounterConnectionPropagatesAndPersists(GameTestHelper helper) {
		DigitalClockBlockEntity clock = helper.getBlockEntity(CLOCK_POS, DigitalClockBlockEntity.class);
		DigitalCounterBlockEntity counter = helper.getBlockEntity(COUNTER_POS, DigitalCounterBlockEntity.class);
		DigitalWorldNetwork network = EngineeringSimulation.digitalNetwork(helper.getLevel().getServer());
		helper.assertTrue(network.deviceCount() >= 2, "Placed digital block entities must register with the world network");

		UUID player = UUID.randomUUID();
		network.selectFirstOutput(player, clock);
		helper.assertValueEqual("clock", network.connectSelected(player, counter), "Connected counter input");
		network.publish(clock, "output", new DigitalWord(1, 0), 1_000L, 1_000L);
		network.publish(clock, "output", new DigitalWord(1, 1), 2_000L, 1_000L);
		helper.assertValueEqual(1L, counter.value(), "Counter value after propagated rising edge");

		CompoundTag saved = counter.saveWithFullMetadata(helper.getLevel().registryAccess());
		BlockEntity restoredBase = BlockEntity.loadStatic(
				counter.getBlockPos(), counter.getBlockState(), saved, helper.getLevel().registryAccess());
		helper.assertTrue(restoredBase instanceof DigitalCounterBlockEntity,
				"Saved counter must deserialize as the registered block entity type");
		DigitalCounterBlockEntity restored = (DigitalCounterBlockEntity) restoredBase;
		helper.assertValueEqual(1L, restored.value(), "Restored counter value");
		helper.assertTrue(restored.inputBindings().getFirst().connected(),
				"Restored counter must retain its clock connection");
		helper.succeed();
	}

	@GameTest
	public void allMilestoneThreeBlocksCreateExpectedBlockEntities(GameTestHelper helper) {
		helper.setBlock(new BlockPos(3, 1, 1), ModBlocks.DIGITAL_GATE);
		helper.setBlock(new BlockPos(4, 1, 1), ModBlocks.DIGITAL_REGISTER);
		helper.assertBlockPresent(ModBlocks.DIGITAL_CLOCK, CLOCK_POS);
		helper.assertBlockPresent(ModBlocks.DIGITAL_COUNTER, COUNTER_POS);
		helper.assertBlockPresent(ModBlocks.DIGITAL_GATE, new BlockPos(3, 1, 1));
		helper.assertBlockPresent(ModBlocks.DIGITAL_REGISTER, new BlockPos(4, 1, 1));
		helper.succeed();
	}

	@GameTest
	public void linkingToolCreatesPlayerAuthoredConnection(GameTestHelper helper) {
		var player = helper.makeMockServerPlayer(GameType.CREATIVE);
		ItemStack tool = new ItemStack(ModItems.DIGITAL_LINKING_TOOL);
		player.setItemInHand(InteractionHand.MAIN_HAND, tool);
		helper.placeAt(player, tool, CLOCK_POS.below(), Direction.UP);
		helper.placeAt(player, tool, COUNTER_POS.below(), Direction.UP);

		DigitalCounterBlockEntity counter = helper.getBlockEntity(COUNTER_POS, DigitalCounterBlockEntity.class);
		helper.assertTrue(counter.inputBindings().getFirst().connected(),
				"Digital Linking Tool must persist a player-created clock connection");
		helper.discard(player);
		helper.succeed();
	}

	@GameTest
	public void allDigitalDeviceStateRoundTripsThroughMinecraftSerialization(GameTestHelper helper) {
		BlockPos gatePos = new BlockPos(3, 1, 1);
		BlockPos registerPos = new BlockPos(4, 1, 1);
		helper.setBlock(gatePos, ModBlocks.DIGITAL_GATE);
		helper.setBlock(registerPos, ModBlocks.DIGITAL_REGISTER);
		DigitalClockBlockEntity clock = helper.getBlockEntity(CLOCK_POS, DigitalClockBlockEntity.class);
		DigitalCounterBlockEntity counter = helper.getBlockEntity(COUNTER_POS, DigitalCounterBlockEntity.class);
		DigitalGateBlockEntity gate = helper.getBlockEntity(gatePos, DigitalGateBlockEntity.class);
		DigitalRegisterBlockEntity register = helper.getBlockEntity(registerPos, DigitalRegisterBlockEntity.class);

		clock.selectNextFrequency();
		clock.toggleEnabled();
		counter.pulse();
		gate.selectNextOperation();
		gate.toggleManualInputA();
		register.acceptInput("data", new DigitalWord(8, 77), 1L, 1L);
		register.pulse();

		DigitalClockBlockEntity restoredClock = roundTrip(helper, clock, DigitalClockBlockEntity.class);
		DigitalCounterBlockEntity restoredCounter = roundTrip(helper, counter, DigitalCounterBlockEntity.class);
		DigitalGateBlockEntity restoredGate = roundTrip(helper, gate, DigitalGateBlockEntity.class);
		DigitalRegisterBlockEntity restoredRegister = roundTrip(helper, register, DigitalRegisterBlockEntity.class);

		helper.assertValueEqual(5.0, restoredClock.frequencyHertz(), "Restored clock frequency");
		helper.assertFalse(restoredClock.enabled(), "Restored clock enabled state");
		helper.assertValueEqual(1L, restoredCounter.value(), "Restored counter value");
		helper.assertValueEqual(DigitalGateOperation.OR, restoredGate.operation(), "Restored gate operation");
		helper.assertValueEqual(0xFFL, restoredGate.outputValue(), "Restored gate output");
		helper.assertValueEqual(77L, restoredRegister.value(), "Restored register value");
		helper.succeed();
	}

	@Override
	public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
		helper.setBlock(CLOCK_POS, ModBlocks.DIGITAL_CLOCK);
		helper.setBlock(COUNTER_POS, ModBlocks.DIGITAL_COUNTER);
		method.invoke(this, helper);
	}

	private static <T extends BlockEntity> T roundTrip(
			GameTestHelper helper, T original, Class<T> expectedType) {
		CompoundTag saved = original.saveWithFullMetadata(helper.getLevel().registryAccess());
		BlockEntity restored = BlockEntity.loadStatic(
				original.getBlockPos(), original.getBlockState(), saved, helper.getLevel().registryAccess());
		helper.assertTrue(expectedType.isInstance(restored),
				"Serialized block entity must restore as " + expectedType.getSimpleName());
		return expectedType.cast(restored);
	}
}
