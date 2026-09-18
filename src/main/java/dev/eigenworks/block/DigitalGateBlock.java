package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.DigitalGateBlockEntity;
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

/** Placeable configurable eight-bit combinational gate. */
public final class DigitalGateBlock extends BaseEntityBlock {
	public static final MapCodec<DigitalGateBlock> CODEC = simpleCodec(DigitalGateBlock::new);

	public DigitalGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DigitalGateBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(level.getBlockEntity(pos) instanceof DigitalGateBlockEntity gate)) {
			return InteractionResult.PASS;
		}
		if (player.isShiftKeyDown()) {
			gate.selectNextOperation();
		} else {
			gate.toggleManualInputA();
		}
		player.sendSystemMessage(Component.translatable(
				"message.eigenworks.digital_gate_status", gate.operation().name(), gate.outputValue()));
		return InteractionResult.SUCCESS_SERVER;
	}
}
