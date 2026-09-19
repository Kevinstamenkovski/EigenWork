package dev.eigenworks.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.eigenworks.block.entity.ComputerBlockEntity;
import dev.eigenworks.block.entity.DigitalClockBlockEntity;
import dev.eigenworks.block.entity.DigitalCounterBlockEntity;
import dev.eigenworks.block.entity.DigitalGateBlockEntity;
import dev.eigenworks.block.entity.DigitalRegisterBlockEntity;
import dev.eigenworks.block.entity.MicrocontrollerBlockEntity;
import dev.eigenworks.block.entity.OscilloscopeBlockEntity;
import dev.eigenworks.block.entity.MotorRigBlockEntity;
import dev.eigenworks.block.entity.MathematicsWorkstationBlockEntity;
import dev.eigenworks.block.entity.CommunicationHubBlockEntity;
import dev.eigenworks.block.entity.RobotArmBlockEntity;
import dev.eigenworks.block.entity.FactoryCellBlockEntity;
import dev.eigenworks.block.entity.AdvancedEngineeringConsoleBlockEntity;
import dev.eigenworks.block.entity.RobotJointModuleBlockEntity;
import dev.eigenworks.block.entity.CanCableBlockEntity;
import dev.eigenworks.block.entity.CanNodeBlockEntity;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Handheld contextual engineering inspector; all values are read from server-owned state. */
public final class EngineeringInspectorItem extends Item {
	public EngineeringInspectorItem(Properties properties) { super(properties); }

	@Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> output, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, output, flag);
		EngineeringTooltips.append("engineering_inspector", output);
	}

	@Override public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
		List<Component> lines = describe(blockEntity);
		if (lines.isEmpty()) lines = List.of(Component.literal("No engineering diagnostics available."));
		if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.connection != null) {
			for (Component line : lines) player.sendSystemMessage(line);
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	private static List<Component> describe(BlockEntity blockEntity) {
		List<Component> lines = new ArrayList<>();
		if (blockEntity instanceof ComputerBlockEntity computer) {
			lines.add(Component.literal("Eigen-8 Computer"));
			lines.add(Component.literal("CPU: %s  PC: 0x%04X  SP: 0x%04X".formatted(
					computer.cpu().status(), computer.cpu().programCounter(), computer.cpu().stackPointer())));
			lines.add(Component.literal("Cycles: %d  Demo[0x0020]: %d".formatted(
					computer.cpu().totalCycles(), computer.memoryValue(ComputerBlockEntity.DEMO_RESULT_ADDRESS))));
		} else if (blockEntity instanceof MicrocontrollerBlockEntity mcu) {
			lines.add(Component.literal("Eigen-MCU"));
			lines.add(Component.literal("CPU: %s  GPIO out: 0x%02X  deadlines: %d".formatted(
					mcu.mcu().cpu().status(), mcu.gpioOutput(), mcu.mcu().missedDeadlines())));
			lines.add(Component.literal("ADC: %d/1023  Vin: %.3f V  PWM: %.1f%% @ %.0f Hz".formatted(
					mcu.mcu().peripherals().adc().result(), mcu.mcu().peripherals().analogInputVolts(),
					mcu.mcu().peripherals().pwm().dutyFraction() * 100.0,
					mcu.mcu().peripherals().pwm().frequencyHertz())));
		} else if (blockEntity instanceof OscilloscopeBlockEntity scope) {
			lines.add(Component.literal("4-channel Oscilloscope"));
			for (int channel = 0; channel < 4; channel++) {
				lines.add(Component.literal("CH%d: %.0f (%d samples)".formatted(channel + 1,
						scope.model().currentValue(channel), scope.model().channelHistory(channel).size())));
			}
		} else if (blockEntity instanceof MotorRigBlockEntity rig) {
			lines.add(Component.literal("DC Motor Test Rig"));
			lines.add(Component.literal("Mode: %s  target: %.3f rad  error: %.3f rad".formatted(
					rig.controlMode(), rig.controllerSnapshot().reference(), rig.controllerSnapshot().error())));
			lines.add(Component.literal("V: %.2f V  I: %.2f A  speed: %.2f rad/s".formatted(rig.assembly().driver().outputVoltage(),rig.assembly().motor().currentAmperes(),rig.assembly().outputSpeed())));
			lines.add(Component.literal("angle: %.3f rad  encoder: %d  load: %.2f N m".formatted(rig.assembly().outputAngle(),rig.encoderCounts(),rig.assembly().loadTorque())));
			lines.add(Component.literal("PID: Kp=%.2f Ki=%.2f Kd=%.2f target=%.3f rad".formatted(
					rig.positionController().kp(), rig.positionController().ki(), rig.positionController().kd(), rig.positionController().targetRadians())));
			if(!rig.assembly().motor().faults().isEmpty())lines.add(Component.literal("Faults: "+rig.assembly().motor().faults()));
		} else if (blockEntity instanceof MathematicsWorkstationBlockEntity workstation) {
			lines.add(Component.literal("Engineering Mathematics Workstation"));
			lines.add(Component.literal("Expression: " + workstation.expression()));
			lines.add(Component.literal("Result: " + workstation.result()));
		} else if (blockEntity instanceof CommunicationHubBlockEntity hub) {
			lines.add(Component.literal("Communication Hub: " + hub.selectedProtocol()));
			lines.add(Component.literal("Status: " + hub.result()));
			lines.add(Component.literal("UART selector: %d  I2C: 0x%02X  SPI data: 0x%02X".formatted(
					hub.communication().uartBaudIndex(), hub.communication().i2cAddress(), hub.communication().spiData())));
		} else if (blockEntity instanceof RobotArmBlockEntity robot) {
			var pose = robot.arm().endEffector();
			lines.add(Component.literal("2-DOF Planar Robot Arm"));
			lines.add(Component.literal("q1: %.3f rad  q2: %.3f rad".formatted(robot.arm().joint1(), robot.arm().joint2())));
			lines.add(Component.literal("end: (%.3f, %.3f) m  target: (%.3f, %.3f) m".formatted(
					pose.xMeters(), pose.yMeters(), robot.arm().targetX(), robot.arm().targetY())));
			lines.add(Component.literal("manipulability: %.5f  %s".formatted(robot.arm().manipulability(), robot.arm().diagnostic())));
		} else if (blockEntity instanceof FactoryCellBlockEntity factory) {
			lines.add(Component.literal("PLC Factory Cell"));
			lines.add(Component.literal("item: %s  position: %.3f m  outcome: %s".formatted(
					factory.factory().conveyor().item(), factory.factory().conveyor().positionMeters(), factory.factory().conveyor().lastOutcome())));
			lines.add(Component.literal("photo: %s  proximity: %s  conveyor: %s  diverter: %s".formatted(
					factory.factory().conveyor().photoelectricSensor(), factory.factory().conveyor().proximitySensor(),
					factory.factory().conveyorOutput(), factory.factory().diverterOutput())));
			lines.add(Component.literal("scans: %d  sensed: %d  e-stop: %s  %s".formatted(
					factory.factory().plc().scanCount(), factory.factory().itemsSensed(), factory.factory().emergencyStop(), factory.programDiagnostic())));
		} else if (blockEntity instanceof AdvancedEngineeringConsoleBlockEntity console) {
			lines.add(Component.literal("Advanced Engineering Console: " + console.selectedModule()));
			lines.add(Component.literal("Status: " + console.result()));
			lines.add(Component.literal("Logical cycles/iterations: %d  successful: %s".formatted(console.lastCycles(), console.successful())));
		} else if (blockEntity instanceof CanNodeBlockEntity node) {
			lines.add(Component.literal("Physical CAN Node"));
			lines.add(Component.literal("Connected: %s  TX identifier: 0x%03X".formatted(node.connected(), node.identifier())));
			lines.add(Component.literal("Frames TX: %d  RX: %d  %s".formatted(node.transmitCount(), node.receiveCount(), node.diagnostic())));
		} else if (blockEntity instanceof CanCableBlockEntity) {
			lines.add(Component.literal("Physical CAN Cable (500 kbit/s component)"));
		} else if (blockEntity instanceof RobotJointModuleBlockEntity joint) {
			lines.add(Component.literal("Articulated Robot Joint Module"));
			lines.add(Component.literal(joint.status()));
		} else if (blockEntity instanceof DigitalClockBlockEntity clock) {
			lines.add(Component.literal("Digital Clock: %.1f Hz, output=%s, enabled=%s".formatted(
					clock.frequencyHertz(), clock.levelHigh(), clock.enabled())));
		} else if (blockEntity instanceof DigitalCounterBlockEntity counter) {
			lines.add(Component.literal("8-bit Counter: %d (0x%02X)".formatted(counter.value(), counter.value())));
		} else if (blockEntity instanceof DigitalGateBlockEntity gate) {
			lines.add(Component.literal("8-bit Gate: %s, output=0x%02X".formatted(gate.operation(), gate.outputValue())));
		} else if (blockEntity instanceof DigitalRegisterBlockEntity register) {
			lines.add(Component.literal("8-bit Register: %d (0x%02X)".formatted(register.value(), register.value())));
		}
		if (blockEntity instanceof WorldDigitalDevice device) {
			long connected = device.inputBindings().stream().filter(binding -> binding.connected()).count();
			lines.add(Component.literal("Digital ports: %d out, %d/%d inputs connected".formatted(
					device.outputPorts().size(), connected, device.inputBindings().size())));
		}
		return List.copyOf(lines);
	}
}
