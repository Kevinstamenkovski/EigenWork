package dev.eigenworks.block.entity;

import dev.eigenworks.embedded.McuCommunicationController;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Server-scheduled playable UART/I2C/SPI diagnostic endpoint. */
public final class CommunicationHubBlockEntity extends BlockEntity implements LoadedSimulationDevice {
	public enum Protocol { UART, I2C, SPI }
	private final McuCommunicationController communication = new McuCommunicationController();
	private String simulationId;
	private Protocol selected = Protocol.UART;
	private Protocol active;
	private String result = "COMMUNICATION READY";
	private long currentTimeMicros;

	public CommunicationHubBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.COMMUNICATION_HUB, pos, state); }
	@Override public void bindToLevel(ServerLevel level) { simulationId = "communication_hub:" + level.dimension().identifier() + ":" + worldPosition.asLong(); }
	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() { if (simulationId == null) throw new IllegalStateException("Communication Hub is unloaded"); return simulationId; }
	@Override public long updatePeriodMicros() { return 1_000; }
	@Override public void simulate(SimulationContext context) {
		currentTimeMicros = context.simulationTimeMicros();
		communication.advance(currentTimeMicros);
		if (active == null) return;
		switch (active) {
			case UART -> { if ((communication.uartStatus() & 2) != 0) finish("UART RX 0x%02X".formatted(communication.readUartData())); }
			case I2C -> { if ((communication.i2cStatus() & 2) != 0) finish("I2C ACK DATA 0x%02X".formatted(communication.i2cData())); }
			case SPI -> { if ((communication.spiStatus() & 2) != 0) finish("SPI MISO 0x%02X".formatted(communication.spiData())); }
		}
	}
	public boolean startDiagnostic() {
		if (active != null) return false;
		communication.advance(currentTimeMicros);
		active = selected;
		result = selected + " ACTIVE";
		switch (selected) {
			case UART -> communication.transmitUart(0x55);
			case I2C -> { communication.setI2cAddress(0x48); communication.setI2cData(0x3C); communication.startI2c(false); }
			case SPI -> { communication.setSpiData(0x0F); communication.startSpi(); }
		}
		setChanged();
		return true;
	}
	public void nextProtocol() { if (active == null) { selected = Protocol.values()[(selected.ordinal() + 1) % Protocol.values().length]; result = "SELECTED " + selected; setChanged(); } }
	public Protocol selectedProtocol() { return selected; }
	public boolean busy() { return active != null; }
	public String result() { return result; }
	public McuCommunicationController communication() { return communication; }
	private void finish(String value) { result = value; active = null; setChanged(); }

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		selected = Protocol.values()[Math.clamp(input.getIntOr("protocol", 0), 0, Protocol.values().length - 1)];
		result = input.getStringOr("result", "COMMUNICATION READY");
		communication.restore(input.getIntOr("uart_baud", 0), input.getIntOr("i2c_address", 0x48),
				input.getIntOr("i2c_data", 0), input.getIntOr("spi_data", 0), input.getIntOr("uart_receive", 0));
		active = null;
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("protocol", selected.ordinal());
		output.putString("result", result);
		output.putInt("uart_baud", communication.uartBaudIndex());
		output.putInt("i2c_address", communication.i2cAddress());
		output.putInt("i2c_data", communication.i2cData());
		output.putInt("spi_data", communication.spiData());
		output.putInt("uart_receive", 0);
	}
}
