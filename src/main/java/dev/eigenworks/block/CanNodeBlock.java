package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.CanNodeBlockEntity;
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

/** Player-controlled endpoint for a physical CAN cable component. */
public final class CanNodeBlock extends BaseEntityBlock {
	public static final MapCodec<CanNodeBlock> CODEC = simpleCodec(CanNodeBlock::new);
	public CanNodeBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CanNodeBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof CanNodeBlockEntity node)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) node.selectNextIdentifier(); else node.transmit();
		player.sendSystemMessage(Component.literal("CAN Node: " + node.status()));
		return InteractionResult.SUCCESS_SERVER;
	}
}
