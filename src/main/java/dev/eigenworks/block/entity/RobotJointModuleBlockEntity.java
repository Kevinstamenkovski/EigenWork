package dev.eigenworks.block.entity;

import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.robotics.world.RobotAssemblySnapshot;
import dev.eigenworks.robotics.world.RobotWorldAddress;
import dev.eigenworks.robotics.world.RobotWorldNetwork;
import dev.eigenworks.robotics.world.WorldRobotJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

/** Persistent lifecycle adapter for one loaded articulated robot joint. */
public final class RobotJointModuleBlockEntity extends BlockEntity implements WorldRobotJoint {
	private RobotWorldAddress address;
	private RobotWorldNetwork network;
	private int axisIndex;
	private double angleRadians;
	private double linkLengthMeters = 1.0;
	public RobotJointModuleBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ROBOT_JOINT_MODULE, pos, state); }
	@Override public void bindRobotNetwork(ServerLevel level, RobotWorldNetwork network) {
		address = new RobotWorldAddress(level.dimension().identifier().toString(), worldPosition.asLong()); this.network = network;
	}
	@Override public void unbindRobotNetwork() { network = null; }
	@Override public RobotWorldAddress robotAddress() { if (address == null) throw new IllegalStateException("Robot joint is unloaded"); return address; }
	@Override public int axisIndex() { return axisIndex; }
	@Override public double angleRadians() { return angleRadians; }
	@Override public double linkLengthMeters() { return linkLengthMeters; }
	public void nextAxis() { axisIndex = (axisIndex + 1) % 6; changedConfiguration(); }
	public void rotateQuarterTurn() { angleRadians += Math.PI / 2; if (angleRadians > Math.PI) angleRadians -= Math.PI * 2; changedConfiguration(); }
	public RobotAssemblySnapshot assembly() { if (network == null) throw new IllegalStateException("ROBOT NETWORK UNAVAILABLE"); return network.assembly(this); }
	public String status() {
		try {
			var snapshot = assembly(); var matrix = snapshot.endEffector().matrix();
			return "Robot joint J%d: q=%.3f rad, chain=%d axes, end=(%.2f, %.2f, %.2f) m".formatted(axisIndex + 1, angleRadians, snapshot.axes(), matrix.get(0,3), matrix.get(1,3), matrix.get(2,3));
		} catch (RuntimeException exception) { return "Robot joint J%d: q=%.3f rad, %s".formatted(axisIndex + 1, angleRadians, exception.getMessage()); }
	}
	private void changedConfiguration() {
		if (network != null) network.configurationChanged(); setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}
	@Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
	@Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) { return saveWithoutMetadata(provider); }
	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input); axisIndex = Math.clamp(input.getIntOr("axis", 0), 0, 5);
		angleRadians = input.getDoubleOr("angle", 0); if (!Double.isFinite(angleRadians)) angleRadians = 0;
		linkLengthMeters = Math.clamp(input.getDoubleOr("link_length", 1), 0.1, 8); if (!Double.isFinite(linkLengthMeters)) linkLengthMeters = 1;
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output); output.putInt("axis", axisIndex); output.putDouble("angle", angleRadians); output.putDouble("link_length", linkLengthMeters);
	}
}
