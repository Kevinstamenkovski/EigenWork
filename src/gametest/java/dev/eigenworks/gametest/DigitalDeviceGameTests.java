package dev.eigenworks.gametest;

import java.lang.reflect.Method;
import java.util.UUID;

import dev.eigenworks.block.entity.DigitalClockBlockEntity;
import dev.eigenworks.block.entity.DigitalCounterBlockEntity;
import dev.eigenworks.block.entity.DigitalGateBlockEntity;
import dev.eigenworks.block.entity.DigitalRegisterBlockEntity;
import dev.eigenworks.block.entity.ComputerBlockEntity;
import dev.eigenworks.block.entity.MicrocontrollerBlockEntity;
import dev.eigenworks.block.entity.OscilloscopeBlockEntity;
import dev.eigenworks.block.entity.MotorRigBlockEntity;
import dev.eigenworks.computer.cpu.CpuStatus;
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

	@GameTest
	public void computerExecutesAndPersistsDemoAOnTheServerScheduler(GameTestHelper helper) {
		BlockPos computerPos = new BlockPos(3, 1, 1);
		helper.setBlock(computerPos, ModBlocks.COMPUTER);
		ComputerBlockEntity computer = helper.getBlockEntity(computerPos, ComputerBlockEntity.class);
		String source = """
			.equ RESULT, 0x0020
			LOAD R0, 25
			LOAD R1, 17
			ADD R2, R0, R1
			STORE [RESULT], R2
			HALT
			""";
		helper.assertTrue(computer.assembleAndLoad(source).successful(), "Demo A source must assemble in Minecraft");
		computer.runCpu();
		helper.runAfterDelay(3, () -> {
			helper.assertValueEqual(CpuStatus.HALTED, computer.cpu().status(), "Scheduled CPU status");
			helper.assertValueEqual(42, computer.memoryValue(ComputerBlockEntity.DEMO_RESULT_ADDRESS),
					"Demo A result in RAM");
			ComputerBlockEntity restored = roundTrip(helper, computer, ComputerBlockEntity.class);
			helper.assertValueEqual(42, restored.memoryValue(ComputerBlockEntity.DEMO_RESULT_ADDRESS),
					"Persisted Demo A result");
			helper.assertValueEqual(source, restored.programSource(), "Persisted assembly source");
			helper.succeed();
		});
	}

	@GameTest
	public void microcontrollerRunsEmbeddedPeripheralsAndPersists(GameTestHelper helper) {
		BlockPos mcuPos = new BlockPos(3, 1, 1);
		helper.setBlock(mcuPos, ModBlocks.MICROCONTROLLER);
		MicrocontrollerBlockEntity mcu = helper.getBlockEntity(mcuPos, MicrocontrollerBlockEntity.class);
		mcu.mcu().peripherals().setAnalogInputVolts(2.5);
		String source = """
			LOAD R0, 0xFF
			OUT 0x00, R0
			LOAD R0, 0xA5
			OUT 0x01, R0
			LOAD R0, 1
			OUT 0x10, R0
			LOAD R0, 128
			OUT 0x20, R0
			LOAD R0, 1
			OUT 0x21, R0
			HALT
			""";
		helper.assertTrue(mcu.assembleAndLoad(source).successful(), "MCU source must assemble in Minecraft");
		mcu.toggleRunPause();
		helper.runAfterDelay(3, () -> {
			helper.assertValueEqual(CpuStatus.HALTED, mcu.mcu().cpu().status(), "Scheduled MCU status");
			helper.assertValueEqual(0xA5, mcu.gpioOutput(), "MCU GPIO output");
			helper.assertValueEqual(512, mcu.mcu().peripherals().adc().result(), "MCU ADC conversion");
			helper.assertTrue(mcu.mcu().peripherals().pwm().enabled(), "MCU PWM enable");
			MicrocontrollerBlockEntity restored = roundTrip(helper, mcu, MicrocontrollerBlockEntity.class);
			helper.assertValueEqual(0xA5, restored.gpioOutput(), "Restored GPIO output");
			helper.assertValueEqual(512, restored.mcu().peripherals().adc().result(), "Restored ADC result");
			helper.assertValueEqual(source, restored.mcu().programSource(), "Restored MCU source");
			helper.succeed();
		});
	}

	@GameTest
	public void oscilloscopeSamplesLinkedMcuOutputAndPersistsConfiguration(GameTestHelper helper) {
		BlockPos mcuPos = new BlockPos(3, 1, 1);
		BlockPos scopePos = new BlockPos(4, 1, 1);
		helper.setBlock(mcuPos, ModBlocks.MICROCONTROLLER);
		helper.setBlock(scopePos, ModBlocks.OSCILLOSCOPE);
		MicrocontrollerBlockEntity mcu = helper.getBlockEntity(mcuPos, MicrocontrollerBlockEntity.class);
		OscilloscopeBlockEntity scope = helper.getBlockEntity(scopePos, OscilloscopeBlockEntity.class);
		mcu.mcu().peripherals().gpio().setDirectionMask(0xFF);
		mcu.mcu().peripherals().gpio().writeOutputs(0xA5);
		DigitalWorldNetwork network = EngineeringSimulation.digitalNetwork(helper.getLevel().getServer());
		UUID player = UUID.randomUUID();
		network.selectFirstOutput(player, mcu);
		helper.assertValueEqual("ch1", network.connectSelected(player, scope), "Connected oscilloscope channel");
		scope.nextTimeScale();

		helper.runAfterDelay(3, () -> {
			helper.assertTrue(scope.model().channelHistory(0).size() > 0, "Oscilloscope must record scheduled samples");
			helper.assertValueEqual(165.0, scope.model().channelHistory(0).newest().value(), "Sampled MCU GPIO value");
			OscilloscopeBlockEntity restored = roundTrip(helper, scope, OscilloscopeBlockEntity.class);
			helper.assertTrue(restored.inputBindings().getFirst().connected(), "Restored scope connection");
			helper.succeed();
		});
	}

	@GameTest
	public void linkedPwmCommandDrivesMotorGearboxAndEncoder(GameTestHelper helper) {
		BlockPos mcuPos=new BlockPos(3,1,1); BlockPos motorPos=new BlockPos(4,1,1);
		helper.setBlock(mcuPos,ModBlocks.MICROCONTROLLER); helper.setBlock(motorPos,ModBlocks.MOTOR_RIG);
		MicrocontrollerBlockEntity mcu=helper.getBlockEntity(mcuPos,MicrocontrollerBlockEntity.class);
		MotorRigBlockEntity motor=helper.getBlockEntity(motorPos,MotorRigBlockEntity.class);
		mcu.mcu().peripherals().gpio().setDirectionMask(0xFF); mcu.mcu().peripherals().gpio().writeOutputs(0xFF);
		DigitalWorldNetwork network=EngineeringSimulation.digitalNetwork(helper.getLevel().getServer()); UUID player=UUID.randomUUID();
		network.selectFirstOutput(player,mcu); helper.assertValueEqual("pwm_command",network.connectSelected(player,motor),"Connected motor command");
		helper.runAfterDelay(20,()->{
			helper.assertTrue(motor.assembly().motor().angularVelocityRadPerSec()>1,"Motor must accelerate from linked command");
			helper.assertTrue(motor.encoderCounts()>0,"Output encoder must measure motion");
			MotorRigBlockEntity restored=roundTrip(helper,motor,MotorRigBlockEntity.class);
			helper.assertTrue(restored.inputBindings().getFirst().connected(),"Restored motor command link");
			helper.assertValueEqual(motor.encoderCounts(),restored.encoderCounts(),"Restored encoder state");
			helper.succeed();
		});
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
