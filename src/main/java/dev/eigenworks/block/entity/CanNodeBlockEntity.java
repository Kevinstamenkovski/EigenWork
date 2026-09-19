package dev.eigenworks.block.entity;

import dev.eigenworks.networking.CanFrame;
import dev.eigenworks.networking.world.CanWorldAddress;
import dev.eigenworks.networking.world.CanWorldNetwork;
import dev.eigenworks.networking.world.WorldCanNode;
import dev.eigenworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-authoritative CAN endpoint with player-triggered frames. */
public final class CanNodeBlockEntity extends BlockEntity implements WorldCanNode {
	private static final int[] IDENTIFIERS = {0x100, 0x200, 0x300, 0x7ff};
	private CanWorldAddress address;
	private CanWorldNetwork network;
	private int identifier = 0x100;
	private int transmitValue = 1;
	private int transmitCount;
	private int receiveCount;
	private int lastReceivedIdentifier = -1;
	private int lastReceivedValue;
	private String diagnostic = "CAN READY";
	public CanNodeBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CAN_NODE, pos, state); }
	@Override public void bindCanNetwork(ServerLevel level, CanWorldNetwork network) { address = new CanWorldAddress(level.dimension().identifier().toString(), worldPosition.asLong()); this.network = network; }
	@Override public void unbindCanNetwork() { network = null; }
	@Override public CanWorldAddress canAddress() { if (address == null) throw new IllegalStateException("CAN node is unloaded"); return address; }
	@Override public String canNodeName() { return "node:" + canAddress().dimension() + ":" + canAddress().packedPosition(); }
	@Override public void receiveCanFrame(CanFrame frame) { lastReceivedIdentifier = frame.identifier(); lastReceivedValue = frame.payload().length == 0 ? 0 : Byte.toUnsignedInt(frame.payload()[0]); receiveCount++; diagnostic = "RX 0x%03X DATA 0x%02X".formatted(lastReceivedIdentifier, lastReceivedValue); setChanged(); }
	public boolean transmit() {
		if (network == null) { diagnostic = "CAN NODE UNLOADED"; return false; }
		try { network.transmit(this, new CanFrame(identifier, new byte[] {(byte) transmitValue})); transmitValue = (transmitValue + 1) & 0xff; transmitCount++; diagnostic = "TX 0x%03X QUEUED".formatted(identifier); setChanged(); return true; }
		catch (IllegalStateException exception) { diagnostic = exception.getMessage(); setChanged(); return false; }
	}
	public void selectNextIdentifier() { int index = 0; for (int i = 0; i < IDENTIFIERS.length; i++) if (IDENTIFIERS[i] == identifier) index = i; identifier = IDENTIFIERS[(index + 1) % IDENTIFIERS.length]; diagnostic = "SELECTED 0x%03X".formatted(identifier); setChanged(); }
	public boolean connected() { return network != null && network.connected(this); }
	public int identifier() { return identifier; }
	public int transmitCount() { return transmitCount; }
	public int receiveCount() { return receiveCount; }
	public int lastReceivedIdentifier() { return lastReceivedIdentifier; }
	public int lastReceivedValue() { return lastReceivedValue; }
	public String diagnostic() { return diagnostic; }
	public String status() { return "connected=%s id=0x%03X tx=%d rx=%d %s".formatted(connected(), identifier, transmitCount, receiveCount, diagnostic); }
	@Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); identifier = validIdentifier(input.getIntOr("identifier", 0x100)); transmitValue = Math.clamp(input.getIntOr("transmit_value", 1), 0, 255); transmitCount = Math.max(0, input.getIntOr("transmit_count", 0)); receiveCount = Math.max(0, input.getIntOr("receive_count", 0)); lastReceivedIdentifier = input.getIntOr("last_receive_id", -1); lastReceivedValue = Math.clamp(input.getIntOr("last_receive_value", 0), 0, 255); diagnostic = input.getStringOr("diagnostic", "CAN READY"); }
	@Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putInt("identifier", identifier); output.putInt("transmit_value", transmitValue); output.putInt("transmit_count", transmitCount); output.putInt("receive_count", receiveCount); output.putInt("last_receive_id", lastReceivedIdentifier); output.putInt("last_receive_value", lastReceivedValue); output.putString("diagnostic", diagnostic); }
	private static int validIdentifier(int value) { return value >= 0 && value <= 0x7ff ? value : 0x100; }
}
