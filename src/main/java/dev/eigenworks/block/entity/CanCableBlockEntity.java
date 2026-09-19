package dev.eigenworks.block.entity;

import dev.eigenworks.networking.world.CanWorldAddress;
import dev.eigenworks.networking.world.CanWorldNetwork;
import dev.eigenworks.networking.world.WorldCanElement;
import dev.eigenworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Lifecycle adapter for one loaded physical CAN cable segment. */
public final class CanCableBlockEntity extends BlockEntity implements WorldCanElement {
	private CanWorldAddress address;
	public CanCableBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CAN_CABLE, pos, state); }
	@Override public void bindCanNetwork(ServerLevel level, CanWorldNetwork network) { address = new CanWorldAddress(level.dimension().identifier().toString(), worldPosition.asLong()); }
	@Override public void unbindCanNetwork() { }
	@Override public CanWorldAddress canAddress() { if (address == null) throw new IllegalStateException("CAN cable is unloaded"); return address; }
}
