package dev.eigenworks.block;

import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.MathematicsWorkstationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Book-programmed safe numerical calculator with no dynamic code execution. */
public final class MathematicsWorkstationBlock extends BaseEntityBlock {
	public static final MapCodec<MathematicsWorkstationBlock> CODEC = simpleCodec(MathematicsWorkstationBlock::new);
	public MathematicsWorkstationBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MathematicsWorkstationBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		String source = bookSource(stack, player.isTextFilteringEnabled());
		if (source == null) return InteractionResult.PASS;
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof MathematicsWorkstationBlockEntity workstation)) return InteractionResult.PASS;
		workstation.calculate(source);
		player.sendSystemMessage(Component.literal("Math Workstation: " + workstation.result()));
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof MathematicsWorkstationBlockEntity workstation)) return InteractionResult.PASS;
		if(player instanceof ServerPlayer serverPlayer)serverPlayer.openMenu(workstation);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static String bookSource(ItemStack stack, boolean filterText) {
		WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (writable != null) return writable.getPages(filterText).collect(Collectors.joining(" "));
		WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (written != null) return written.getPages(filterText).stream().map(Component::getString).collect(Collectors.joining(" "));
		return null;
	}
}
