package dev.eigenworks.block.entity;

import dev.eigenworks.mathematics.MathWorkstationEngine;
import dev.eigenworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-side calculation state for the Engineering Mathematics Workstation. */
public final class MathematicsWorkstationBlockEntity extends BlockEntity {
	private final MathWorkstationEngine engine = new MathWorkstationEngine();
	private String expression = "det 1,2;3,4";
	private String result = "-2.000000000";
	private boolean successful = true;

	public MathematicsWorkstationBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MATHEMATICS_WORKSTATION, pos, state);
	}

	public boolean calculate(String source) {
		expression = source == null ? "" : source.strip();
		try {
			result = engine.evaluate(expression);
			successful = true;
		} catch (IllegalArgumentException exception) {
			result = "ERROR: " + exception.getMessage();
			successful = false;
		}
		setChanged();
		return successful;
	}
	public String expression() { return expression; }
	public String result() { return result; }
	public boolean successful() { return successful; }

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		expression = input.getStringOr("expression", "det 1,2;3,4");
		result = input.getStringOr("result", "-2.000000000");
		successful = input.getBooleanOr("successful", true);
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("expression", expression);
		output.putString("result", result);
		output.putBoolean("successful", successful);
	}
}
