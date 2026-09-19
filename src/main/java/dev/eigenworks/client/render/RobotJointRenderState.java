package dev.eigenworks.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/** Render-thread snapshot for one articulated joint arm. */
public final class RobotJointRenderState extends BlockEntityRenderState {
	public final ItemStackRenderState arm = new ItemStackRenderState();
	public float angleRadians;
}
