package dev.eigenworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.eigenworks.block.entity.RobotJointModuleBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Renders the copper link arm rotated by authoritative joint state. */
public final class RobotJointModuleRenderer implements BlockEntityRenderer<RobotJointModuleBlockEntity, RobotJointRenderState> {
	private final ItemModelResolver itemResolver;
	public RobotJointModuleRenderer(BlockEntityRendererProvider.Context context) { itemResolver = context.itemModelResolver(); }
	@Override public RobotJointRenderState createRenderState() { return new RobotJointRenderState(); }
	@Override public void extractRenderState(RobotJointModuleBlockEntity joint, RobotJointRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(joint, state, partialTick, cameraPosition, breakProgress);
		state.angleRadians = (float) joint.angleRadians();
		ItemOwner owner = new ItemOwner() {
			@Override public net.minecraft.world.level.Level level() { return joint.getLevel(); }
			@Override public Vec3 position() { return Vec3.atCenterOf(joint.getBlockPos()); }
			@Override public float getVisualRotationYInDegrees() { return 0; }
		};
		itemResolver.updateForTopItem(state.arm, new ItemStack(Items.IRON_BLOCK), ItemDisplayContext.FIXED, joint.getLevel(), owner, 0);
	}
	@Override public void submit(RobotJointRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.translate(0.5F, 0.62F, 0.5F);
		pose.mulPose(Axis.YP.rotation(state.angleRadians));
		pose.translate(0.5F, 0, 0);
		pose.scale(1.0F, 0.18F, 0.18F);
		state.arm.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}
