package dev.eigenworks.block.entity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.DigitalDeviceAddress;
import dev.eigenworks.digital.world.DigitalInputBinding;
import dev.eigenworks.digital.world.DigitalPortSpec;
import dev.eigenworks.digital.world.DigitalSourceEndpoint;
import dev.eigenworks.digital.world.DigitalWorldNetwork;
import dev.eigenworks.digital.world.WorldDigitalDevice;
import dev.eigenworks.instrumentation.CsvDataLogger;
import dev.eigenworks.instrumentation.InstrumentSample;
import dev.eigenworks.instrumentation.OscilloscopeMenu;
import dev.eigenworks.instrumentation.OscilloscopeModel;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.LoadedSimulationDevice;
import dev.eigenworks.simulation.SimulationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Four-channel server-owned digital oscilloscope and CSV source. */
public final class OscilloscopeBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice, MenuProvider {
	private static final long UPDATE_PERIOD_MICROS = 10_000L;
	private static final List<DigitalPortSpec> INPUT_PORTS = List.of(
			new DigitalPortSpec("ch1", 8, DigitalPortDirection.INPUT),
			new DigitalPortSpec("ch2", 8, DigitalPortDirection.INPUT),
			new DigitalPortSpec("ch3", 8, DigitalPortDirection.INPUT),
			new DigitalPortSpec("ch4", 8, DigitalPortDirection.INPUT));

	private final OscilloscopeModel model = new OscilloscopeModel(OscilloscopeMenu.HISTORY);
	private final DigitalSourceEndpoint[] sources = new DigitalSourceEndpoint[OscilloscopeModel.CHANNEL_COUNT];
	private String simulationId;
	private DigitalDeviceAddress digitalAddress;
	private int timeScaleIndex = 2;
	private int verticalScaleIndex = 2;

	public OscilloscopeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.OSCILLOSCOPE, pos, state);
	}

	@Override public void bindToLevel(ServerLevel level) { simulationId = "oscilloscope:" + level.dimension().identifier() + ":" + worldPosition.asLong(); }
	@Override public void unbindFromLevel() { simulationId = null; }
	@Override public String simulationId() {
		if (simulationId == null) throw new IllegalStateException("Oscilloscope is not bound to a server level");
		return simulationId;
	}
	@Override public long updatePeriodMicros() { return UPDATE_PERIOD_MICROS; }
	@Override public void simulate(SimulationContext context) { model.sample(context.simulationTimeMicros()); }

	@Override public void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network) {
		digitalAddress = new DigitalDeviceAddress(level.dimension().identifier().toString(), worldPosition.asLong());
	}
	@Override public void unbindDigitalNetwork() { }
	@Override public DigitalDeviceAddress digitalAddress() {
		if (digitalAddress == null) throw new IllegalStateException("Oscilloscope is not bound to a digital network");
		return digitalAddress;
	}
	@Override public List<DigitalPortSpec> outputPorts() { return List.of(); }
	@Override public List<DigitalInputBinding> inputBindings() {
		List<DigitalInputBinding> bindings = new ArrayList<>(OscilloscopeModel.CHANNEL_COUNT);
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) bindings.add(new DigitalInputBinding(INPUT_PORTS.get(channel), sources[channel]));
		return List.copyOf(bindings);
	}
	@Override public DigitalWord outputValue(String port) { throw new IllegalArgumentException("Oscilloscope has no outputs"); }
	@Override public void connectInput(String inputPort, DigitalSourceEndpoint source) {
		int channel = channel(inputPort);
		if (source.width() != 8) throw new IllegalArgumentException("Oscilloscope channels require eight bits");
		sources[channel] = source;
		setChanged();
	}
	@Override public void disconnectInput(String inputPort) { sources[channel(inputPort)] = null; setChanged(); }
	@Override public void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros) {
		if (value.width() != 8) throw new IllegalArgumentException("Oscilloscope channels require eight bits");
		model.setInput(channel(inputPort), value.value(), true);
	}

	public void togglePaused() { model.setPaused(!model.paused()); setChanged(); }
	public void nextTimeScale() { timeScaleIndex = (timeScaleIndex + 1) % 3; setChanged(); }
	public void nextVerticalScale() { verticalScaleIndex = (verticalScaleIndex + 1) % 3; setChanged(); }
	public void toggleChannel(int channel) { model.setChannelEnabled(channel, !model.channelEnabled(channel)); setChanged(); }
	public OscilloscopeModel model() { return model; }

	public Path exportCsv(Path directory, String stem) throws IOException {
		List<List<InstrumentSample>> channels = new ArrayList<>();
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) channels.add(model.channelSnapshot(channel));
		return CsvDataLogger.export(directory, stem, channels);
	}

	public boolean stillValid(Player player) {
		return level != null && level.getBlockEntity(worldPosition) == this
				&& player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
	}
	@Override public Component getDisplayName() { return Component.translatable("screen.eigenworks.oscilloscope"); }
	@Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new OscilloscopeMenu(id, this); }

	public ContainerData menuData() {
		return new ContainerData() {
			@Override public int get(int index) {
				if (index == 0) return model.paused() ? 1 : 0;
				if (index == 1) return timeScaleIndex;
				if (index == 2) return verticalScaleIndex;
				if (index == 3) return model.channelHistory(0).size();
				if (index >= 4 && index < 8) return model.channelEnabled(index - 4) ? 1 : 0;
				if (index >= 8 && index < 12) return (int) Math.round(model.currentValue(index - 8));
				if (index >= OscilloscopeMenu.HEADER_DATA && index < OscilloscopeMenu.DATA_COUNT) {
					int offset = index - OscilloscopeMenu.HEADER_DATA;
					int channel = offset / OscilloscopeMenu.HISTORY;
					int sampleIndex = offset % OscilloscopeMenu.HISTORY;
					var history = model.channelHistory(channel);
					if (sampleIndex >= history.size()) return -1;
					InstrumentSample sample = history.get(sampleIndex);
					return sample.valid() ? (int) Math.round(sample.value()) : -1;
				}
				return 0;
			}
			@Override public void set(int index, int value) { }
			@Override public int getCount() { return OscilloscopeMenu.DATA_COUNT; }
		};
	}

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		model.setPaused(input.getBooleanOr("paused", false));
		timeScaleIndex = Math.clamp(input.getIntOr("time_scale", 2), 0, 2);
		verticalScaleIndex = Math.clamp(input.getIntOr("vertical_scale", 2), 0, 2);
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) {
			model.setChannelEnabled(channel, input.getBooleanOr("channel_" + channel + "_enabled", true));
			sources[channel] = DigitalLinkStorage.read(input, "channel_" + channel + "_source");
		}
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("paused", model.paused());
		output.putInt("time_scale", timeScaleIndex);
		output.putInt("vertical_scale", verticalScaleIndex);
		for (int channel = 0; channel < OscilloscopeModel.CHANNEL_COUNT; channel++) {
			output.putBoolean("channel_" + channel + "_enabled", model.channelEnabled(channel));
			DigitalLinkStorage.write(output, "channel_" + channel + "_source", sources[channel]);
		}
	}

	private static int channel(String port) {
		for (int index = 0; index < INPUT_PORTS.size(); index++) if (INPUT_PORTS.get(index).name().equals(port)) return index;
		throw new IllegalArgumentException("Unknown oscilloscope input: " + port);
	}
}
