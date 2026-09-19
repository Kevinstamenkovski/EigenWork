package dev.eigenworks.block;

import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.MicrocontrollerBlockEntity;
import dev.eigenworks.computer.assembly.AssemblyResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Programmable MCU block integrating Eigen-8 with embedded peripherals. */
public final class MicrocontrollerBlock extends BaseEntityBlock {
	public static final MapCodec<MicrocontrollerBlock> CODEC = simpleCodec(MicrocontrollerBlock::new);
	public MicrocontrollerBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MicrocontrollerBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		String source = bookSource(stack, player.isTextFilteringEnabled());
		if (source == null) return InteractionResult.PASS;
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof MicrocontrollerBlockEntity mcu)) return InteractionResult.PASS;
		AssemblyResult result = mcu.assembleAndLoad(source);
		if (result.successful()) {
			player.sendSystemMessage(Component.translatable("message.eigenworks.mcu_programmed", result.program().bytecode().length));
		} else {
			player.sendSystemMessage(Component.literal(result.diagnostics().getFirst().displayMessage()));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof MicrocontrollerBlockEntity mcu)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) mcu.resetMcu(); else mcu.toggleRunPause();
		player.sendSystemMessage(Component.translatable("message.eigenworks.mcu_status",
				mcu.mcu().cpu().status().name(), mcu.gpioOutput(), mcu.mcu().peripherals().adc().result(),
				mcu.mcu().peripherals().pwm().dutyCode(), mcu.mcu().missedDeadlines()));
		return InteractionResult.SUCCESS_SERVER;
	}

	private static String bookSource(ItemStack stack, boolean filterText) {
		WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (writable != null) return writable.getPages(filterText).collect(Collectors.joining("\n"));
		WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (written != null) return written.getPages(filterText).stream().map(Component::getString).collect(Collectors.joining("\n"));
		return null;
	}
}
