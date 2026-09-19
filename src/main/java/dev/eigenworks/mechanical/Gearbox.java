package dev.eigenworks.mechanical;

/** Ideal ratio with explicit efficiency for rotational speed and torque. */
public record Gearbox(double ratio, double efficiency) {
	public Gearbox { if(!Double.isFinite(ratio)||ratio<=0||!Double.isFinite(efficiency)||efficiency<=0||efficiency>1)throw new IllegalArgumentException("Gearbox parameters invalid"); }
	public double outputSpeed(double input){return input/ratio;}
	public double outputTorque(double input){return input*ratio*efficiency;}
	public double reflectedInputLoad(double outputTorque){return outputTorque/(ratio*efficiency);}
}
