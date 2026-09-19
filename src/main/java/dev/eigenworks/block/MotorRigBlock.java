package dev.eigenworks.block;

import com.mojang.serialization.MapCodec;
import dev.eigenworks.block.entity.MotorRigBlockEntity;
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

/** Integrated first motor test rig with configurable load. */
public final class MotorRigBlock extends BaseEntityBlock {
	public static final MapCodec<MotorRigBlock> CODEC=simpleCodec(MotorRigBlock::new);
	public MotorRigBlock(Properties properties){super(properties);}
	@Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
	@Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new MotorRigBlockEntity(pos,state);}
	@Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
	@Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
		if(level.isClientSide())return InteractionResult.SUCCESS;
		if(!(level.getBlockEntity(pos) instanceof MotorRigBlockEntity rig))return InteractionResult.PASS;
		if(player.isShiftKeyDown())rig.resetRig();else rig.nextLoad();
		player.sendSystemMessage(Component.translatable("message.eigenworks.motor_status",rig.assembly().driver().outputVoltage(),rig.assembly().motor().currentAmperes(),rig.assembly().outputSpeed(),rig.assembly().outputAngle(),rig.encoderCounts(),rig.assembly().loadTorque()));
		return InteractionResult.SUCCESS_SERVER;
	}
}
