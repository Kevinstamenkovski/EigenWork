package dev.eigenworks.robotics;

/** Configured one-DOF joint with bounded state and engineering limits. */
public final class RobotJoint {
	private final String name;
	private final JointType type;
	private final double minimum;
	private final double maximum;
	private final double maximumVelocity;
	private final double maximumEffort;
	private final double inertia;
	private final double damping;
	private double position;
	private double velocity;

	public RobotJoint(String name, JointType type, double minimum, double maximum,
			double maximumVelocity, double maximumEffort, double inertia, double damping) {
		if (name == null || name.isBlank() || type == null) throw new IllegalArgumentException("Joint name and type are required");
		if (!finite(minimum) || !finite(maximum) || minimum >= maximum) throw new IllegalArgumentException("Joint limits are invalid");
		if (!positive(maximumVelocity) || !positive(maximumEffort) || !positive(inertia) || !finite(damping) || damping < 0) throw new IllegalArgumentException("Joint dynamics are invalid");
		this.name = name; this.type = type; this.minimum = minimum; this.maximum = maximum;
		this.maximumVelocity = maximumVelocity; this.maximumEffort = maximumEffort; this.inertia = inertia; this.damping = damping;
		position = Math.clamp(0, minimum, maximum);
	}
	public void setState(double position, double velocity) {
		if (!finite(position) || !finite(velocity)) throw new IllegalArgumentException("Joint state must be finite");
		this.position = Math.clamp(position, minimum, maximum);
		this.velocity = Math.clamp(velocity, -maximumVelocity, maximumVelocity);
	}
	public String name() { return name; } public JointType type() { return type; }
	public double minimum() { return minimum; } public double maximum() { return maximum; }
	public double maximumVelocity() { return maximumVelocity; } public double maximumEffort() { return maximumEffort; }
	public double inertia() { return inertia; } public double damping() { return damping; }
	public double position() { return position; } public double velocity() { return velocity; }
	private static boolean finite(double value) { return Double.isFinite(value); }
	private static boolean positive(double value) { return finite(value) && value > 0; }
}
