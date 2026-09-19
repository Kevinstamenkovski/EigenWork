package dev.eigenworks.block;

import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.FactoryCellBlockEntity;
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

/** Programmable integrated first factory cell. */
public final class FactoryCellBlock extends BaseEntityBlock {
	public static final MapCodec<FactoryCellBlock> CODEC = simpleCodec(FactoryCellBlock::new);
	public FactoryCellBlock(Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FactoryCellBlockEntity(pos, state); }
	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		String source = bookSource(stack, player.isTextFilteringEnabled()); if (source == null) return InteractionResult.PASS;
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof FactoryCellBlockEntity factory)) return InteractionResult.PASS;
		factory.loadProgram(source); player.sendSystemMessage(Component.literal(factory.programDiagnostic())); return InteractionResult.SUCCESS_SERVER;
	}
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof FactoryCellBlockEntity factory)) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) factory.toggleEmergencyStop(); else factory.spawnNextItem();
		player.sendSystemMessage(Component.literal("Factory: item=" + factory.factory().conveyor().item() + " position=%.2f m photo=%s proximity=%s conveyor=%s diverter=%s outcome=%s e-stop=%s scans=%d".formatted(
				factory.factory().conveyor().positionMeters(), factory.factory().conveyor().photoelectricSensor(), factory.factory().conveyor().proximitySensor(),
				factory.factory().conveyorOutput(), factory.factory().diverterOutput(), factory.factory().conveyor().lastOutcome(), factory.factory().emergencyStop(), factory.factory().plc().scanCount())));
		return InteractionResult.SUCCESS_SERVER;
	}
	private static String bookSource(ItemStack stack, boolean filter) {
		WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT); if (writable != null) return writable.getPages(filter).collect(Collectors.joining("\n"));
		WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT); return written == null ? null : written.getPages(filter).stream().map(Component::getString).collect(Collectors.joining("\n"));
	}
}
