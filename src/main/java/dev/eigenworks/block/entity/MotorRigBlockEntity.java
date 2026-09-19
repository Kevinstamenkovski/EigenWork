package dev.eigenworks.block.entity;

import java.util.List;

import dev.eigenworks.digital.DigitalPortDirection;
import dev.eigenworks.digital.world.*;
import dev.eigenworks.mechanical.MotorAssembly;
import dev.eigenworks.registry.ModBlockEntities;
import dev.eigenworks.signal.DigitalWord;
import dev.eigenworks.simulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Playable PWM-to-driver-to-motor-to-gearbox-to-encoder vertical slice. */
public final class MotorRigBlockEntity extends BlockEntity implements LoadedSimulationDevice, WorldDigitalDevice {
	private static final long PERIOD_MICROS=5_000;
	private static final DigitalPortSpec COMMAND=new DigitalPortSpec("pwm_command",8,DigitalPortDirection.INPUT);
	private static final DigitalPortSpec ENCODER_LOW=new DigitalPortSpec("encoder_low",8,DigitalPortDirection.OUTPUT);
	private static final DigitalPortSpec ENCODER_COUNTS=new DigitalPortSpec("encoder_counts",16,DigitalPortDirection.OUTPUT);
	private final MotorAssembly assembly=new MotorAssembly();
	private DigitalSourceEndpoint commandSource; private DigitalDeviceAddress address; private DigitalWorldNetwork network;
	private String simulationId; private int commandCode=128; private long publishedCounts;

	public MotorRigBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.MOTOR_RIG,pos,state);}
	@Override public void bindToLevel(ServerLevel level){simulationId="motor_rig:"+level.dimension().identifier()+":"+worldPosition.asLong();}
	@Override public void unbindFromLevel(){simulationId=null;}
	@Override public String simulationId(){if(simulationId==null)throw new IllegalStateException("Motor rig is unloaded");return simulationId;}
	@Override public long updatePeriodMicros(){return PERIOD_MICROS;}
	@Override public void simulate(SimulationContext context){
		assembly.driver().setCommand(Math.clamp((commandCode-128)/127.0,-1,1));
		assembly.step(context.deltaMicros()/1_000_000.0);
		long counts=assembly.encoderCounts();
		if(counts!=publishedCounts){publishedCounts=counts;setChanged();if(network!=null){network.publish(this,"encoder_low",new DigitalWord(8,counts),context.simulationTimeMicros(),context.deltaMicros());network.publish(this,"encoder_counts",new DigitalWord(16,counts),context.simulationTimeMicros(),context.deltaMicros());}}
	}
	public MotorAssembly assembly(){return assembly;} public int commandCode(){return commandCode;} public long encoderCounts(){return assembly.encoderCounts();}
	public void nextLoad(){double next=assembly.loadTorque()<0.1?0.5:assembly.loadTorque()<1?2.0:0;assembly.setLoadTorque(next);setChanged();}
	public void resetRig(){assembly.motor().restore(0,0,0);commandCode=128;publishedCounts=0;setChanged();}
	@Override public void bindDigitalNetwork(ServerLevel level,DigitalWorldNetwork network){address=new DigitalDeviceAddress(level.dimension().identifier().toString(),worldPosition.asLong());this.network=network;}
	@Override public void unbindDigitalNetwork(){network=null;}
	@Override public DigitalDeviceAddress digitalAddress(){if(address==null)throw new IllegalStateException("Motor rig is not network-bound");return address;}
	@Override public List<DigitalPortSpec> outputPorts(){return List.of(ENCODER_LOW,ENCODER_COUNTS);}
	@Override public List<DigitalInputBinding> inputBindings(){return List.of(new DigitalInputBinding(COMMAND,commandSource));}
	@Override public DigitalWord outputValue(String port){if(port.equals("encoder_low"))return new DigitalWord(8,encoderCounts());if(port.equals("encoder_counts"))return new DigitalWord(16,encoderCounts());throw new IllegalArgumentException("Unknown motor output: "+port);}
	@Override public void connectInput(String port,DigitalSourceEndpoint source){requireCommand(port);if(source.width()!=8)throw new IllegalArgumentException("Motor PWM command requires eight bits");commandSource=source;setChanged();}
	@Override public void disconnectInput(String port){requireCommand(port);commandSource=null;commandCode=128;setChanged();}
	@Override public void acceptInput(String port,DigitalWord value,long timestamp,long period){requireCommand(port);if(value.width()!=8)throw new IllegalArgumentException("Motor PWM command requires eight bits");commandCode=(int)value.value();setChanged();}
	@Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);commandCode=Math.clamp(input.getIntOr("command",128),0,255);assembly.setLoadTorque(input.getDoubleOr("load_torque",0));assembly.motor().restore(input.getDoubleOr("current",0),input.getDoubleOr("velocity",0),input.getDoubleOr("position",0));publishedCounts=input.getLongOr("counts",0);commandSource=DigitalLinkStorage.read(input,"command_source");}
	@Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);output.putInt("command",commandCode);output.putDouble("load_torque",assembly.loadTorque());output.putDouble("current",assembly.motor().currentAmperes());output.putDouble("velocity",assembly.motor().angularVelocityRadPerSec());output.putDouble("position",assembly.motor().angularPositionRadians());output.putLong("counts",publishedCounts);DigitalLinkStorage.write(output,"command_source",commandSource);}
	private static void requireCommand(String port){if(!COMMAND.name().equals(port))throw new IllegalArgumentException("Unknown motor input: "+port);}
}
