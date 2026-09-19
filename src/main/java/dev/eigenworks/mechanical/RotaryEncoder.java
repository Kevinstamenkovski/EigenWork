package dev.eigenworks.mechanical;

/** Absolute angle and incremental-count rotary encoder model. */
public final class RotaryEncoder {
	private final int countsPerRevolution; private final double offset;
	public RotaryEncoder(int cpr,double offset){if(cpr<1||cpr>1_000_000||!Double.isFinite(offset))throw new IllegalArgumentException("Encoder parameters invalid");countsPerRevolution=cpr;this.offset=offset;}
	public long incrementalCounts(double angle){if(!Double.isFinite(angle))throw new IllegalArgumentException("Angle must be finite");return Math.round((angle-offset)/(2*Math.PI)*countsPerRevolution);}
	public int absoluteCode(double angle){return Math.floorMod(incrementalCounts(angle),countsPerRevolution);}
	public int countsPerRevolution(){return countsPerRevolution;}
}
