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
import dev.eigenworks.block.entity.MathematicsWorkstationBlockEntity;
import dev.eigenworks.block.entity.CommunicationHubBlockEntity;
import dev.eigenworks.block.entity.RobotArmBlockEntity;
import dev.eigenworks.block.entity.FactoryCellBlockEntity;
import dev.eigenworks.block.entity.AdvancedEngineeringConsoleBlockEntity;
import dev.eigenworks.block.entity.CanNodeBlockEntity;
import dev.eigenworks.automation.RouteOutcome;
import dev.eigenworks.computer.cpu.CpuStatus;
import dev.eigenworks.control.MotorRigMenu;
import dev.eigenworks.mathematics.MathematicsWorkstationMenu;
import dev.eigenworks.automation.FactoryCellMenu;
import dev.eigenworks.computer.accelerator.AdvancedConsoleMenu;
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

	@GameTest
	public void motorRigMenuAppliesValidatedPidConfigurationAndPersists(GameTestHelper helper) {
		BlockPos motorPos = new BlockPos(3, 1, 1);
		helper.setBlock(motorPos, ModBlocks.MOTOR_RIG);
		MotorRigBlockEntity motor = helper.getBlockEntity(motorPos, MotorRigBlockEntity.class);
		var player = helper.makeMockServerPlayer(GameType.CREATIVE);
		BlockPos absoluteMotorPos = helper.absolutePos(motorPos);
		player.setPos(absoluteMotorPos.getX() + .5, absoluteMotorPos.getY() + .5, absoluteMotorPos.getZ() + .5);
		MotorRigMenu menu = new MotorRigMenu(7, motor);
		helper.assertTrue(menu.clickMenuButton(player, 1), "Server menu must accept Kp increment control");
		helper.assertValueEqual(2.5, motor.positionController().kp(), "Server-adjusted Kp");
		helper.assertTrue(menu.clickMenuButton(player, 7), "Server menu must accept target increment control");
		helper.assertTrue(motor.positionController().targetRadians() > Math.PI / 2, "Target must increase by configured step");
		helper.assertTrue(menu.clickMenuButton(player, MotorRigMenu.BUTTON_TOGGLE_MODE), "Server menu must toggle control mode");
		helper.assertValueEqual(MotorRigBlockEntity.ControlMode.POSITION_90_DEGREES, motor.controlMode(), "Configured control mode");
		MotorRigBlockEntity restored = roundTrip(helper, motor, MotorRigBlockEntity.class);
		helper.assertValueEqual(2.5, restored.positionController().kp(), "Persisted Kp");
		helper.assertValueEqual(motor.positionController().targetRadians(), restored.positionController().targetRadians(), "Persisted target");
		helper.discard(player); helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void protectedPidControlsMotorToNinetyDegreesAndFeedsScope(GameTestHelper helper) {
		BlockPos motorPos = new BlockPos(3, 1, 1);
		BlockPos scopePos = new BlockPos(4, 1, 1);
		helper.setBlock(motorPos, ModBlocks.MOTOR_RIG);
		helper.setBlock(scopePos, ModBlocks.OSCILLOSCOPE);
		MotorRigBlockEntity motor = helper.getBlockEntity(motorPos, MotorRigBlockEntity.class);
		OscilloscopeBlockEntity scope = helper.getBlockEntity(scopePos, OscilloscopeBlockEntity.class);
		motor.toggleControlMode();

		DigitalWorldNetwork network = EngineeringSimulation.digitalNetwork(helper.getLevel().getServer());
		UUID player = UUID.randomUUID();
		for (int channel = 0; channel < 4; channel++) {
			if (channel == 0) network.selectFirstOutput(player, motor);
			else network.selectNextOutput(player, motor);
			helper.assertValueEqual("ch" + (channel + 1), network.connectSelected(player, scope),
					"Connected control diagnostic channel");
		}

		helper.runAfterDelay(100, () -> {
			helper.assertValueEqual(MotorRigBlockEntity.ControlMode.POSITION_90_DEGREES, motor.controlMode(),
					"Motor control mode");
			helper.assertTrue(Math.abs(motor.assembly().outputAngle() - Math.PI / 2) < 0.1,
					"Protected PID must settle the motor near 90 degrees");
			helper.assertTrue(Math.abs(motor.controllerSnapshot().error()) < 0.1,
					"Controller error must be bounded");
			for (int channel = 0; channel < 4; channel++) {
				helper.assertTrue(scope.model().channelHistory(channel).size() > 0,
						"Oscilloscope must sample control channel " + (channel + 1));
			}
			MotorRigBlockEntity restored = roundTrip(helper, motor, MotorRigBlockEntity.class);
			helper.assertValueEqual(MotorRigBlockEntity.ControlMode.POSITION_90_DEGREES, restored.controlMode(),
					"Restored closed-loop mode");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 120)
	public void mcuReadsMotorPositionAndComputesPwmCommand(GameTestHelper helper) {
		BlockPos mcuPos = new BlockPos(3, 1, 1);
		BlockPos motorPos = new BlockPos(4, 1, 1);
		helper.setBlock(mcuPos, ModBlocks.MICROCONTROLLER);
		helper.setBlock(motorPos, ModBlocks.MOTOR_RIG);
		MicrocontrollerBlockEntity mcu = helper.getBlockEntity(mcuPos, MicrocontrollerBlockEntity.class);
		MotorRigBlockEntity motor = helper.getBlockEntity(motorPos, MotorRigBlockEntity.class);
		String source = """
			LOAD R1, 191
			LOAD R4, 128
			LOAD R5, 1
			OUT 0x21, R5
			loop:
			IN R0, 0x02
			CMP R0, R1
			JE stopped
			JL forward
			SUB R3, R0, R1
			SHR R3, R3
			SUB R2, R4, R3
			OUT 0x20, R2
			JMP loop
			forward:
			SUB R3, R1, R0
			SHR R3, R3
			ADD R2, R4, R3
			OUT 0x20, R2
			JMP loop
			stopped:
			OUT 0x20, R4
			JMP loop
			""";
		helper.assertTrue(mcu.assembleAndLoad(source).successful(), "Demo B MCU program must assemble");

		DigitalWorldNetwork network = EngineeringSimulation.digitalNetwork(helper.getLevel().getServer());
		UUID player = UUID.randomUUID();
		network.selectFirstOutput(player, motor);
		network.selectNextOutput(player, motor);
		helper.assertValueEqual("gpio_in", network.connectSelected(player, mcu), "Motor position feedback to MCU");
		network.selectFirstOutput(player, mcu);
		network.selectNextOutput(player, mcu);
		helper.assertValueEqual("pwm_command", network.connectSelected(player, motor), "MCU PWM command to motor");
		mcu.toggleRunPause();

		helper.runAfterDelay(100, () -> {
			helper.assertTrue(mcu.mcu().cpu().totalInstructions() > 100,
					"MCU must execute the feedback algorithm");
			helper.assertTrue(Math.abs(motor.assembly().outputAngle() - Math.PI / 2) < 0.15,
					"MCU-computed command must bring the motor near 90 degrees; angle="
							+ motor.assembly().outputAngle() + ", command=" + motor.commandCode()
							+ ", input=" + mcu.mcu().peripherals().gpio().externalInputs());
			helper.assertTrue(motor.commandCode() > 0 && motor.commandCode() < 255,
					"Motor command must come from the MCU PWM duty register");
			helper.succeed();
		});
	}

	@GameTest
	public void mathematicsWorkstationCalculatesAndPersists(GameTestHelper helper) {
		BlockPos workstationPos = new BlockPos(3, 1, 1);
		helper.setBlock(workstationPos, ModBlocks.MATHEMATICS_WORKSTATION);
		MathematicsWorkstationBlockEntity workstation = helper.getBlockEntity(
				workstationPos, MathematicsWorkstationBlockEntity.class);
		helper.assertTrue(workstation.calculate("solve 0,2;1,3 | 4,7"),
				"Workstation must solve a valid matrix command");
		helper.assertValueEqual("[1.000000000, 2.000000000]", workstation.result(), "Matrix solution");
		MathematicsWorkstationBlockEntity restored = roundTrip(
				helper, workstation, MathematicsWorkstationBlockEntity.class);
		helper.assertValueEqual(workstation.expression(), restored.expression(), "Restored math expression");
		helper.assertValueEqual(workstation.result(), restored.result(), "Restored math result");
		helper.assertTrue(!workstation.calculate("inv 1,2;2,4"), "Singular input must report an error");
		helper.assertTrue(workstation.result().contains("MATRIX SINGULAR"), "Singular diagnostic must be useful");
		helper.succeed();
	}

	@GameTest
	public void milestoneSixteenEditorsApplyServerValuesAndPersist(GameTestHelper helper) {
		BlockPos mathPos=new BlockPos(3,1,1),factoryPos=new BlockPos(4,1,1),consolePos=new BlockPos(5,1,1);
		helper.setBlock(mathPos,ModBlocks.MATHEMATICS_WORKSTATION);helper.setBlock(factoryPos,ModBlocks.FACTORY_CELL);helper.setBlock(consolePos,ModBlocks.ADVANCED_ENGINEERING_CONSOLE);
		var player=helper.makeMockServerPlayer(GameType.CREATIVE);
		MathematicsWorkstationBlockEntity math=helper.getBlockEntity(mathPos,MathematicsWorkstationBlockEntity.class);movePlayer(helper,player,mathPos);MathematicsWorkstationMenu mathMenu=new MathematicsWorkstationMenu(20,math);
		helper.assertTrue(mathMenu.clickMenuButton(player,1),"Matrix cell increment");helper.assertTrue(mathMenu.clickMenuButton(player,MathematicsWorkstationMenu.BUTTON_CALCULATE),"Matrix calculation");helper.assertValueEqual(2.0,math.gridCell(0),"Edited matrix cell");
		FactoryCellBlockEntity factory=helper.getBlockEntity(factoryPos,FactoryCellBlockEntity.class);movePlayer(helper,player,factoryPos);FactoryCellMenu factoryMenu=new FactoryCellMenu(21,factory);helper.assertTrue(factoryMenu.clickMenuButton(player,FactoryCellMenu.BUTTON_SCAN),"PLC scan setting");helper.assertValueEqual(50_000L,factory.factory().plc().scanPeriodMicros(),"Edited PLC scan period");
		AdvancedEngineeringConsoleBlockEntity console=helper.getBlockEntity(consolePos,AdvancedEngineeringConsoleBlockEntity.class);movePlayer(helper,player,consolePos);AdvancedConsoleMenu consoleMenu=new AdvancedConsoleMenu(22,console);helper.assertTrue(consoleMenu.clickMenuButton(player,2),"CAN bitrate setting");helper.assertValueEqual(1_000_000,console.bitrate(),"Edited console bitrate");
		helper.assertValueEqual(2.0,roundTrip(helper,math,MathematicsWorkstationBlockEntity.class).gridCell(0),"Persisted matrix cell");helper.assertValueEqual(50_000L,roundTrip(helper,factory,FactoryCellBlockEntity.class).factory().plc().scanPeriodMicros(),"Persisted PLC scan");helper.assertValueEqual(1_000_000,roundTrip(helper,console,AdvancedEngineeringConsoleBlockEntity.class).bitrate(),"Persisted bitrate");helper.discard(player);helper.succeed();
	}

	@GameTest
	public void communicationHubExecutesAllTimedBusesAndPersists(GameTestHelper helper) {
		BlockPos hubPos = new BlockPos(3, 1, 1);
		helper.setBlock(hubPos, ModBlocks.COMMUNICATION_HUB);
		CommunicationHubBlockEntity hub = helper.getBlockEntity(hubPos, CommunicationHubBlockEntity.class);
		helper.assertTrue(hub.startDiagnostic(), "UART diagnostic must start");
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(hub.result().contains("UART RX 0x55"), "UART must complete after frame time");
			hub.nextProtocol();
			helper.assertTrue(hub.startDiagnostic(), "I2C diagnostic must start");
			helper.runAfterDelay(2, () -> {
				helper.assertTrue(hub.result().contains("I2C ACK"), "Addressed I2C peripheral must acknowledge");
				hub.nextProtocol();
				helper.assertTrue(hub.startDiagnostic(), "SPI diagnostic must start");
				helper.runAfterDelay(2, () -> {
					helper.assertTrue(hub.result().contains("SPI MISO 0xF0"), "Selected SPI peripheral must respond");
					CommunicationHubBlockEntity restored = roundTrip(helper, hub, CommunicationHubBlockEntity.class);
					helper.assertValueEqual(CommunicationHubBlockEntity.Protocol.SPI, restored.selectedProtocol(),
							"Restored selected protocol");
					helper.assertValueEqual(hub.result(), restored.result(), "Restored communication result");
					helper.succeed();
				});
			});
		});
	}

	@GameTest(maxTicks = 80)
	public void planarRobotComputesIkAndMovesToCartesianTarget(GameTestHelper helper) {
		BlockPos robotPos = new BlockPos(3, 1, 1);
		helper.setBlock(robotPos, ModBlocks.ROBOT_ARM);
		RobotArmBlockEntity robot = helper.getBlockEntity(robotPos, RobotArmBlockEntity.class);
		helper.assertTrue(robot.commandTarget(1, 1), "Demo D target must be reachable");
		helper.runAfterDelay(50, () -> {
			var pose = robot.arm().endEffector();
			helper.assertTrue(Math.abs(pose.xMeters() - 1) < 0.01 && Math.abs(pose.yMeters() - 1) < 0.01,
					"Robot trajectory must reach IK target");
			helper.assertTrue(robot.arm().manipulability() > 0.5,
					"Target configuration must remain away from singularity");
			RobotArmBlockEntity restored = roundTrip(helper, robot, RobotArmBlockEntity.class);
			helper.assertValueEqual(robot.arm().joint1(), restored.arm().joint1(), "Restored shoulder angle");
			helper.assertValueEqual(robot.arm().joint2(), restored.arm().joint2(), "Restored elbow angle");
			helper.assertTrue(!robot.commandTarget(3, 0), "Unreachable target must be rejected without crashing");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 80)
	public void plcFactoryRoutesItemsThroughDemoE(GameTestHelper helper) {
		BlockPos factoryPos = new BlockPos(3, 1, 1);
		helper.setBlock(factoryPos, ModBlocks.FACTORY_CELL);
		FactoryCellBlockEntity factory = helper.getBlockEntity(factoryPos, FactoryCellBlockEntity.class);
		helper.assertTrue(factory.loadProgram(dev.eigenworks.automation.FactoryCell.DEFAULT_PROGRAM),
				"Demo E Structured Text must compile");
		helper.assertTrue(factory.spawnItem(true), "Metal workpiece must enter conveyor");
		helper.runAfterDelay(30, () -> {
			helper.assertValueEqual(RouteOutcome.DIVERTED, factory.factory().conveyor().lastOutcome(),
					"PLC must divert metallic workpiece");
			helper.assertTrue(factory.spawnItem(false), "Non-metal workpiece must enter conveyor");
			helper.runAfterDelay(30, () -> {
				helper.assertValueEqual(RouteOutcome.STRAIGHT, factory.factory().conveyor().lastOutcome(),
					"PLC must route non-metal workpiece straight");
				helper.assertTrue(factory.factory().plc().scanCount() > 20, "PLC must execute repeated scan cycles");
				FactoryCellBlockEntity restored = roundTrip(helper, factory, FactoryCellBlockEntity.class);
				helper.assertValueEqual(factory.factory().plc().source(), restored.factory().plc().source(), "Restored PLC source");
				restored.toggleEmergencyStop();
				helper.assertTrue(restored.factory().emergencyStop(), "Emergency stop state must be controllable");
				helper.succeed();
			});
		});
	}

	@GameTest
	public void advancedConsoleRunsEveryMilestoneThirteenSubsystemAndPersists(GameTestHelper helper) {
		BlockPos consolePos = new BlockPos(3, 1, 1);
		helper.setBlock(consolePos, ModBlocks.ADVANCED_ENGINEERING_CONSOLE);
		AdvancedEngineeringConsoleBlockEntity console = helper.getBlockEntity(
				consolePos, AdvancedEngineeringConsoleBlockEntity.class);
		for (AdvancedEngineeringConsoleBlockEntity.Module module : AdvancedEngineeringConsoleBlockEntity.Module.values()) {
			helper.assertValueEqual(module, console.selectedModule(), "Selected advanced module");
			helper.assertTrue(console.runDiagnostic(), module + " diagnostic must succeed");
			helper.assertTrue(!console.result().contains("FAULT"), module + " must provide a valid result");
			if (module != AdvancedEngineeringConsoleBlockEntity.Module.THREE_LINK_ROBOT) console.nextModule();
		}
		AdvancedEngineeringConsoleBlockEntity restored = roundTrip(
				helper, console, AdvancedEngineeringConsoleBlockEntity.class);
		helper.assertValueEqual(AdvancedEngineeringConsoleBlockEntity.Module.THREE_LINK_ROBOT,
				restored.selectedModule(), "Restored advanced module");
		helper.assertValueEqual(console.result(), restored.result(), "Restored advanced diagnostic");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void physicalCanCableConnectsNodesTransmitsAndPersists(GameTestHelper helper) {
		BlockPos senderPos = new BlockPos(3, 1, 1), cablePos = new BlockPos(4, 1, 1), receiverPos = new BlockPos(5, 1, 1);
		helper.setBlock(senderPos, ModBlocks.CAN_NODE); helper.setBlock(cablePos, ModBlocks.CAN_CABLE); helper.setBlock(receiverPos, ModBlocks.CAN_NODE);
		CanNodeBlockEntity sender = helper.getBlockEntity(senderPos, CanNodeBlockEntity.class);
		CanNodeBlockEntity receiver = helper.getBlockEntity(receiverPos, CanNodeBlockEntity.class);
		helper.runAfterDelay(1, () -> {
			helper.assertTrue(sender.connected() && receiver.connected(), "Cable topology must connect both loaded CAN nodes");
			helper.assertTrue(sender.transmit(), "Connected CAN node must queue a frame");
			helper.runAfterDelay(2, () -> {
				helper.assertValueEqual(1, receiver.receiveCount(), "Receiver frame count");
				helper.assertValueEqual(0x100, receiver.lastReceivedIdentifier(), "Received CAN identifier");
				helper.assertValueEqual(1, receiver.lastReceivedValue(), "Received CAN payload");
				CanNodeBlockEntity restored = roundTrip(helper, receiver, CanNodeBlockEntity.class);
				helper.assertValueEqual(1, restored.receiveCount(), "Persisted CAN receive count");
				helper.assertValueEqual(0x100, restored.lastReceivedIdentifier(), "Persisted CAN identifier");
				helper.succeed();
			});
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
	private static void movePlayer(GameTestHelper helper,net.minecraft.world.entity.player.Player player,BlockPos relative){BlockPos p=helper.absolutePos(relative);player.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);}
}
