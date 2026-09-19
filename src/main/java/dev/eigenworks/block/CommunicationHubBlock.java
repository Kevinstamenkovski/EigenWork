package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.CommunicationHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** In-world endpoint for timed communication-bus diagnostics. */
public final class CommunicationHubBlock extends BaseEntityBlock {
	public static final MapCodec<CommunicationHubBlock> CODEC = simpleCodec(CommunicationHubBlock::new);
	public CommunicationHubBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CommunicationHubBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof CommunicationHubBlockEntity hub)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) hub.nextProtocol(); else hub.startDiagnostic();
		player.sendSystemMessage(Component.literal("Communication Hub [" + hub.selectedProtocol() + "]: " + hub.result()));
		return InteractionResult.SUCCESS_SERVER;
	}
}
