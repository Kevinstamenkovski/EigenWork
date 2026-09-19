package dev.eigenworks.mechanical;

import java.util.EnumSet;

/** Coupled armature/electromechanical DC motor integrated with guarded RK4. */
public final class DcMotorModel {
	public enum Fault { OVERCURRENT, STALL, OVERSPEED, INVALID_SUPPLY, DRIVER_SATURATION }
	private final double resistance, inductance, backEmfConstant, torqueConstant, inertia, damping, maxVoltage, maxCurrent, maxSpeed;
	private double current, angularVelocity, angularPosition;
	private final EnumSet<Fault> faults = EnumSet.noneOf(Fault.class);

	public DcMotorModel(double resistance, double inductance, double backEmfConstant, double torqueConstant,
			double inertia, double damping, double maxVoltage, double maxCurrent, double maxSpeed) {
		if (!positive(resistance) || !positive(inductance) || !positive(backEmfConstant) || !positive(torqueConstant)
				|| !positive(inertia) || damping < 0 || !Double.isFinite(damping) || !positive(maxVoltage)
				|| !positive(maxCurrent) || !positive(maxSpeed)) throw new IllegalArgumentException("DC motor parameters are invalid");
		this.resistance=resistance; this.inductance=inductance; this.backEmfConstant=backEmfConstant;
		this.torqueConstant=torqueConstant; this.inertia=inertia; this.damping=damping;
		this.maxVoltage=maxVoltage; this.maxCurrent=maxCurrent; this.maxSpeed=maxSpeed;
	}

	public void step(double appliedVoltage, double loadTorque, double dt) {
		faults.clear();
		if (!Double.isFinite(appliedVoltage) || !Double.isFinite(loadTorque)) { faults.add(Fault.INVALID_SUPPLY); appliedVoltage = 0; loadTorque = 0; }
		if (Math.abs(appliedVoltage) > maxVoltage) { faults.add(Fault.DRIVER_SATURATION); appliedVoltage = Math.clamp(appliedVoltage, -maxVoltage, maxVoltage); }
		if (!Double.isFinite(dt) || dt <= 0 || dt > 0.05) throw new IllegalArgumentException("Motor timestep must be in (0, 0.05] seconds");
		double[] state = {current, angularVelocity, angularPosition};
		double[] k1 = derivative(state, appliedVoltage, loadTorque);
		double[] k2 = derivative(add(state, k1, dt/2), appliedVoltage, loadTorque);
		double[] k3 = derivative(add(state, k2, dt/2), appliedVoltage, loadTorque);
		double[] k4 = derivative(add(state, k3, dt), appliedVoltage, loadTorque);
		for (int i=0;i<3;i++) state[i] += dt*(k1[i]+2*k2[i]+2*k3[i]+k4[i])/6;
		if (!Double.isFinite(state[0]) || !Double.isFinite(state[1]) || !Double.isFinite(state[2]) || Math.abs(state[2]) > 1e12) throw new IllegalStateException("MOTOR NUMERICAL INSTABILITY");
		current=state[0]; angularVelocity=state[1]; angularPosition=state[2];
		if (Math.abs(current)>maxCurrent) faults.add(Fault.OVERCURRENT);
		if (Math.abs(angularVelocity)>maxSpeed) faults.add(Fault.OVERSPEED);
		if (Math.abs(appliedVoltage)>0.2*maxVoltage && Math.abs(angularVelocity)<0.01 && Math.abs(current)>0.5*maxCurrent) faults.add(Fault.STALL);
	}
	private double[] derivative(double[] s,double v,double load) { return new double[]{(v-resistance*s[0]-backEmfConstant*s[1])/inductance,(torqueConstant*s[0]-damping*s[1]-load)/inertia,s[1]}; }
	private static double[] add(double[] s,double[] k,double scale){return new double[]{s[0]+k[0]*scale,s[1]+k[1]*scale,s[2]+k[2]*scale};}
	private static boolean positive(double v){return Double.isFinite(v)&&v>0;}
	public double currentAmperes(){return current;} public double angularVelocityRadPerSec(){return angularVelocity;} public double angularPositionRadians(){return angularPosition;}
	public double electromagneticTorqueNm(){return torqueConstant*current;} public EnumSet<Fault> faults(){return faults.clone();}
	public void restore(double i,double w,double theta){if(!Double.isFinite(i)||!Double.isFinite(w)||!Double.isFinite(theta))throw new IllegalArgumentException("Motor state must be finite");current=i;angularVelocity=w;angularPosition=theta;}
}
