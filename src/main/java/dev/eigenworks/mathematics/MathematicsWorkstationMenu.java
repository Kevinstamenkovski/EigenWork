package dev.eigenworks.mathematics;
import dev.eigenworks.block.entity.MathematicsWorkstationBlockEntity;
import dev.eigenworks.registry.ModMenus;
import net.minecraft.world.entity.player.*; import net.minecraft.world.inventory.*; import net.minecraft.world.item.ItemStack;
public final class MathematicsWorkstationMenu extends AbstractContainerMenu {
	public static final int DATA_COUNT=7, BUTTON_OPERATION=20, BUTTON_CALCULATE=21, BUTTON_RESET=22;
	private final ContainerData data; private final MathematicsWorkstationBlockEntity workstation;
	public MathematicsWorkstationMenu(int id,Inventory inv){this(id,new SimpleContainerData(DATA_COUNT),null);} public MathematicsWorkstationMenu(int id,MathematicsWorkstationBlockEntity w){this(id,w.menuData(),w);}
	private MathematicsWorkstationMenu(int id,ContainerData data,MathematicsWorkstationBlockEntity w){super(ModMenus.MATHEMATICS_WORKSTATION,id);checkContainerDataCount(data,DATA_COUNT);this.data=data;workstation=w;addDataSlots(data);}
	@Override public ItemStack quickMoveStack(Player p,int s){return ItemStack.EMPTY;} @Override public boolean stillValid(Player p){return workstation==null||workstation.stillValid(p);}
	@Override public boolean clickMenuButton(Player p,int id){if(workstation==null||!stillValid(p))return false;if(id>=0&&id<8)workstation.adjustGrid(id/2,id%2==0?-1:1);else if(id==BUTTON_OPERATION)workstation.nextGridOperation();else if(id==BUTTON_CALCULATE)workstation.calculateGrid();else if(id==BUTTON_RESET)workstation.resetGrid();else return false;return true;}
	public double cell(int i){return data.get(i)/1000.0;} public int operation(){return data.get(4);} public double result(){return data.get(5)/1000.0;} public boolean successful(){return data.get(6)!=0;}
}
