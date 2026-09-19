package dev.eigenworks.mechanical;

/** Composed driver, motor, gearbox, and output encoder used by the first playable rig. */
public final class MotorAssembly {
	private final HBridgeMotorDriver driver = new HBridgeMotorDriver(24,0.95);
	private final DcMotorModel motor = new DcMotorModel(1.2,0.02,0.08,0.08,0.01,0.002,24,25,600);
	private final Gearbox gearbox = new Gearbox(20,0.9);
	private final RotaryEncoder encoder = new RotaryEncoder(4096,0);
	private double loadTorque;
	public void step(double dt){motor.step(driver.outputVoltage(),gearbox.reflectedInputLoad(loadTorque),dt);}
	public double outputAngle(){return motor.angularPositionRadians()/gearbox.ratio();}
	public double outputSpeed(){return gearbox.outputSpeed(motor.angularVelocityRadPerSec());}
	public long encoderCounts(){return encoder.incrementalCounts(outputAngle());}
	public HBridgeMotorDriver driver(){return driver;} public DcMotorModel motor(){return motor;} public Gearbox gearbox(){return gearbox;} public RotaryEncoder encoder(){return encoder;}
	public void setLoadTorque(double value){if(!Double.isFinite(value))throw new IllegalArgumentException("Load torque must be finite");loadTorque=value;} public double loadTorque(){return loadTorque;}
}
