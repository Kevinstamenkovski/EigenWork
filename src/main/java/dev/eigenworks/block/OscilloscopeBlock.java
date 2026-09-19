package dev.eigenworks.block;

import java.io.IOException;
import java.nio.file.Path;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.OscilloscopeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Four-channel oscilloscope; sneak-use exports its bounded history as CSV. */
public final class OscilloscopeBlock extends BaseEntityBlock {
	public static final MapCodec<OscilloscopeBlock> CODEC = simpleCodec(OscilloscopeBlock::new);
	public OscilloscopeBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OscilloscopeBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof OscilloscopeBlockEntity scope)) return InteractionResult.PASS;
		if (player.isShiftKeyDown() && level instanceof ServerLevel serverLevel) {
			try {
				Path directory = serverLevel.getServer().getFile("eigenworks/exports");
				Path file = scope.exportCsv(directory,
						"scope_%d_%d_%d_%d".formatted(pos.getX(), pos.getY(), pos.getZ(), System.currentTimeMillis()));
				player.sendSystemMessage(Component.translatable("message.eigenworks.scope_exported", file.toString()));
			} catch (IOException exception) {
				player.sendSystemMessage(Component.translatable("message.eigenworks.scope_export_failed", exception.getMessage()));
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(scope);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.PASS;
	}
}
