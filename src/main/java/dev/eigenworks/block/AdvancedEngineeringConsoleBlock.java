package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.AdvancedEngineeringConsoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Playable server-authoritative entry point for advanced engineering diagnostics. */
public final class AdvancedEngineeringConsoleBlock extends BaseEntityBlock {
	public static final MapCodec<AdvancedEngineeringConsoleBlock> CODEC = simpleCodec(AdvancedEngineeringConsoleBlock::new);
	public AdvancedEngineeringConsoleBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AdvancedEngineeringConsoleBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AdvancedEngineeringConsoleBlockEntity console)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) {console.nextModule();player.sendSystemMessage(Component.literal("Advanced Console [" + console.selectedModule() + "]: " + console.result()));} else if(player instanceof ServerPlayer serverPlayer)serverPlayer.openMenu(console);
		return InteractionResult.SUCCESS_SERVER;
	}
}
