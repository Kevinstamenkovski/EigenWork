package dev.eigenworks.mechanical;

/** Ideal bounded H-bridge mapping signed normalized PWM command to motor voltage. */
public final class HBridgeMotorDriver {
	private final double supplyVolts;
	private final double efficiency;
	private double command;
	private boolean enabled = true;
	public HBridgeMotorDriver(double supplyVolts,double efficiency){if(!Double.isFinite(supplyVolts)||supplyVolts<=0||!Double.isFinite(efficiency)||efficiency<=0||efficiency>1)throw new IllegalArgumentException("Driver parameters invalid");this.supplyVolts=supplyVolts;this.efficiency=efficiency;}
	public void setCommand(double command){if(!Double.isFinite(command))throw new IllegalArgumentException("Driver command must be finite");this.command=Math.clamp(command,-1,1);}
	public double outputVoltage(){return enabled?command*supplyVolts*efficiency:0;}
	public void setEnabled(boolean enabled){this.enabled=enabled;} public double command(){return command;} public boolean enabled(){return enabled;} public double supplyVolts(){return supplyVolts;}
}
