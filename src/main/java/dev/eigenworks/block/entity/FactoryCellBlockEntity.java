package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.automation.FactoryCell;
import dev.eigenworks.automation.FactoryItem;
import dev.eigenworks.automation.RouteOutcome;
import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Integrated conveyor/sensors/diverter/PLC factory cell for playable Demo E. */
public final class FactoryCellBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	private static final DigitalPortSpec EMERGENCY_STOP = port("emergency_stop", 1, DigitalPortDirection.INPUT);
	private static final List<DigitalPortSpec> OUTPUTS = List.of(port("photo_sensor", 1, DigitalPortDirection.OUTPUT),
			port("proximity_sensor", 1, DigitalPortDirection.OUTPUT), port("conveyor", 1, DigitalPortDirection.OUTPUT),
			port("diverter", 1, DigitalPortDirection.OUTPUT), port("item_count", 8, DigitalPortDirection.OUTPUT));
	private final FactoryCell factory = new FactoryCell();
	private DigitalSourceEndpoint emergencyStopSource;
	private DigitalDeviceAddress address;
	private DigitalWorldNetwork network;
	private String simulationId;
	private long nextItemId = 1;
	private boolean nextMetallic = true;
	private String programDiagnostic = "DEFAULT PLC PROGRAM LOADED";

	public FactoryCellBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.FACTORY_CELL, pos, state); }
	@Override public void bindToLevel(ServerLevel level) { simulationId = "factory_cell:" + level.dimension().identifier() + ":" + worldPosition.asLong(); }
	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() { if (simulationId == null) throw new IllegalStateException("Factory Cell is unloaded"); return simulationId; }
	@Override public long updatePeriodMicros() { return 10_000; }
	@Override public void simulate(SimulationContext context) {
		factory.step(context.deltaMicros());
		if (network != null) for (DigitalPortSpec output : OUTPUTS) network.publish(this, output.name(), outputValue(output.name()), context.simulationTimeMicros(), context.deltaMicros());
		setChanged();
	}
	public boolean loadProgram(String source) {
		try { factory.loadProgram(source); programDiagnostic = "PLC PROGRAM LOADED"; setChanged(); return true; }
		catch (IllegalArgumentException exception) { programDiagnostic = "PLC ERROR: " + exception.getMessage(); return false; }
	}
	public boolean spawnNextItem() {
		boolean spawned = factory.spawn(new FactoryItem(nextItemId, nextMetallic));
		if (spawned) { nextItemId++; nextMetallic = !nextMetallic; setChanged(); }
		return spawned;
	}
	public boolean spawnItem(boolean metallic) { boolean spawned = factory.spawn(new FactoryItem(nextItemId++, metallic)); if (spawned) setChanged(); return spawned; }
	public void toggleEmergencyStop() { factory.setEmergencyStop(!factory.emergencyStop()); setChanged(); }
	public FactoryCell factory() { return factory; } public String programDiagnostic() { return programDiagnostic; }

	@Override public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) { address = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong()); this.network = network; }
	@Override public void unbindDigitalNetwork() { network = null; }
	@Override public DigitalDeviceAddress digitalAddress() { if (address == null) throw new IllegalStateException("Factory Cell is not network-bound"); return address; }
	@Override public List<DigitalPortSpec> outputPorts() { return OUTPUTS; }
	@Override public List<DigitalInputBinding> inputBindings() { return List.of(new DigitalInputBinding(EMERGENCY_STOP, emergencyStopSource)); }
	@Override public DigitalWord outputValue(String port) {
		return switch (port) {
			case "photo_sensor" -> bit(factory.conveyor().photoelectricSensor());
			case "proximity_sensor" -> bit(factory.conveyor().proximitySensor());
			case "conveyor" -> bit(factory.conveyorOutput());
			case "diverter" -> bit(factory.diverterOutput());
			case "item_count" -> new DigitalWord(8, factory.itemsSensed());
			default -> throw new IllegalArgumentException("Unknown Factory Cell output: " + port);
		};
	}
	@Override public void connectInput(String inputPort, DigitalSourceEndpoint source) { requireEmergency(inputPort); if (source.width() != 1) throw new IllegalArgumentException("Emergency stop requires one bit"); emergencyStopSource = source; setChanged(); }
	@Override public void disconnectInput(String inputPort) { requireEmergency(inputPort); emergencyStopSource = null; factory.setEmergencyStop(false); setChanged(); }
	@Override public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) { requireEmergency(inputPort); if (value.width() != 1) throw new IllegalArgumentException("Emergency stop requires one bit"); factory.setEmergencyStop(value.value() != 0); setChanged(); }

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String source = input.getStringOr("plc_source", FactoryCell.DEFAULT_PROGRAM);
		try { factory.loadProgram(source); } catch (IllegalArgumentException ignored) { factory.loadProgram(FactoryCell.DEFAULT_PROGRAM); }
		programDiagnostic = input.getStringOr("program_diagnostic", "DEFAULT PLC PROGRAM LOADED");
		nextItemId = Math.max(1, input.getLongOr("next_item_id", 1)); nextMetallic = input.getBooleanOr("next_metallic", true);
		boolean hasItem = input.getBooleanOr("has_item", false);
		factory.restore(input.getLongOr("item_id", 0), input.getBooleanOr("item_metallic", false), hasItem,
				input.getDoubleOr("item_position", 0), enumValue(RouteOutcome.values(), input.getIntOr("outcome", 0), RouteOutcome.NONE),
				input.getBooleanOr("emergency_stop", false), input.getBooleanOr("route_latch", false));
		emergencyStopSource = DigitalLinkStorage.read(input, "emergency_source");
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("plc_source", factory.plc().source()); output.putString("program_diagnostic", programDiagnostic);
		output.putLong("next_item_id", nextItemId); output.putBoolean("next_metallic", nextMetallic);
		FactoryItem item = factory.conveyor().item(); output.putBoolean("has_item", item != null);
		if (item != null) { output.putLong("item_id", item.id()); output.putBoolean("item_metallic", item.metallic()); }
		output.putDouble("item_position", factory.conveyor().positionMeters()); output.putInt("outcome", factory.conveyor().lastOutcome().ordinal());
		output.putBoolean("emergency_stop", factory.emergencyStop()); output.putBoolean("route_latch", factory.plc().variable("RouteLatch"));
		DigitalLinkStorage.write(output, "emergency_source", emergencyStopSource);
	}
	private static DigitalPortSpec port(String name, int width, DigitalPortDirection direction) { return new DigitalPortSpec(name, width, direction); }
	private static DigitalWord bit(boolean value) { return new DigitalWord(1, value ? 1 : 0); }
	private static void requireEmergency(String port) { if (!EMERGENCY_STOP.name().equals(port)) throw new IllegalArgumentException("Unknown Factory Cell input: " + port); }
	private static <T> T enumValue(T[] values, int ordinal, T fallback) { return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback; }
}
