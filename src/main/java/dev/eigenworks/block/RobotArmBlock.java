package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.RobotArmBlockEntity;
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

/** Playable two-DOF planar arm with IK target presets. */
public final class RobotArmBlock extends BaseEntityBlock {
	public static final MapCodec<RobotArmBlock> CODEC = simpleCodec(RobotArmBlock::new);
	public RobotArmBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RobotArmBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof RobotArmBlockEntity robot)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) robot.resetArm(); else robot.nextPreset();
		var pose = robot.arm().endEffector();
		player.sendSystemMessage(Component.literal("Robot target=(%.2f, %.2f) m pose=(%.2f, %.2f) m q=(%.3f, %.3f) rad manipulability=%.4f %s".formatted(
				robot.arm().targetX(), robot.arm().targetY(), pose.xMeters(), pose.yMeters(), robot.arm().joint1(), robot.arm().joint2(), robot.arm().manipulability(), robot.arm().diagnostic())));
		return InteractionResult.SUCCESS_SERVER;
	}
}
