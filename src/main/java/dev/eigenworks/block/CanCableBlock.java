package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.CanCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin physical six-way CAN conductor registered into cached world topology. */
public final class CanCableBlock extends BaseEntityBlock {
	public static final MapCodec<CanCableBlock> CODEC = simpleCodec(CanCableBlock::new);
	private static final VoxelShape SHAPE = Shapes.or(
			box(6, 6, 6, 10, 10, 10), box(0, 7, 7, 16, 9, 9),
			box(7, 0, 7, 9, 16, 9), box(7, 7, 0, 9, 9, 16));
	public CanCableBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CanCableBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
