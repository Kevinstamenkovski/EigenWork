package dev.eigenworks.block.entity;

import dev.eigenworks.mathematics.MathWorkstationEngine;
import dev.eigenworks.mathematics.MathematicsWorkstationMenu;
import dev.eigenworks.mathematics.Matrix;
import dev.eigenworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component; import net.minecraft.world.MenuProvider; import net.minecraft.world.entity.player.*; import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persistent server-side calculation state for the Engineering Mathematics Workstation. */
public final class MathematicsWorkstationBlockEntity extends BlockEntity implements MenuProvider {
	public enum GridOperation { DETERMINANT, INVERSE }
	private final MathWorkstationEngine engine = new MathWorkstationEngine();
	private String expression = "det 1,2;3,4";
	private String result = "-2.000000000";
	private boolean successful = true;
	private final double[] grid={1,2,3,4}; private GridOperation gridOperation=GridOperation.DETERMINANT; private double gridResult=-2;

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
	public void adjustGrid(int index,int direction){if(index<0||index>=4||(direction!=-1&&direction!=1))throw new IllegalArgumentException("Invalid grid edit");grid[index]=Math.clamp(grid[index]+direction,-99,99);setChanged();}
	public void nextGridOperation(){gridOperation=GridOperation.values()[(gridOperation.ordinal()+1)%GridOperation.values().length];setChanged();}
	public boolean calculateGrid(){String source=(gridOperation==GridOperation.DETERMINANT?"det ":"inv ")+grid[0]+","+grid[1]+";"+grid[2]+","+grid[3];boolean ok=calculate(source);if(ok&&gridOperation==GridOperation.DETERMINANT)gridResult=Double.parseDouble(result);else if(ok)gridResult=new Matrix(new double[][]{{grid[0],grid[1]},{grid[2],grid[3]}}).determinant();return ok;}
	public void resetGrid(){double[] defaults={1,2,3,4};System.arraycopy(defaults,0,grid,0,4);gridOperation=GridOperation.DETERMINANT;calculateGrid();}
	public double gridCell(int i){return grid[i];} public GridOperation gridOperation(){return gridOperation;} public double gridResult(){return gridResult;}
	public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
	@Override public Component getDisplayName(){return Component.translatable("screen.eigenworks.mathematics_workstation");} @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new MathematicsWorkstationMenu(id,this);}
	public ContainerData menuData(){return new ContainerData(){public int get(int i){return switch(i){case 0,1,2,3->(int)Math.round(grid[i]*1000);case 4->gridOperation.ordinal();case 5->(int)Math.clamp(Math.round(gridResult*1000),Integer.MIN_VALUE,Integer.MAX_VALUE);case 6->successful?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return MathematicsWorkstationMenu.DATA_COUNT;}};}

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		expression = input.getStringOr("expression", "det 1,2;3,4");
		result = input.getStringOr("result", "-2.000000000");
		successful = input.getBooleanOr("successful", true);
		for(int i=0;i<4;i++)grid[i]=Math.clamp(input.getDoubleOr("grid_"+i,i+1),-99,99);gridOperation=GridOperation.values()[Math.clamp(input.getIntOr("grid_operation",0),0,1)];gridResult=input.getDoubleOr("grid_result",-2);
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("expression", expression);
		output.putString("result", result);
		output.putBoolean("successful", successful);
		for(int i=0;i<4;i++)output.putDouble("grid_"+i,grid[i]);output.putInt("grid_operation",gridOperation.ordinal());output.putDouble("grid_result",gridResult);
	}
}
